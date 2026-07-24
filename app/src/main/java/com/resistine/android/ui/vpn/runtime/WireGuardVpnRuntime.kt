package com.resistine.android.ui.vpn.runtime

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.resistine.android.network.wireguard.WireGuardFlowTelemetryCoordinator
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.TelemetryGoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.Tunnel.State
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class WireGuardVpnRuntime(
    context: Context,
    private val onStatusMessage: (String) -> Unit,
    private val onTunnelStarted: () -> Unit,
    private val onConnectionConfirmed: () -> Unit,
    private val onTunnelDown: () -> Unit
) : VpnRuntime {
    override val mode: VpnRuntimeMode = VpnRuntimeMode.WIREGUARD_TELEMETRY

    private val appContext = context.applicationContext
    private val runtimeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lifecycleMutex = Mutex()
    private val telemetryCoordinator = WireGuardFlowTelemetryCoordinator(appContext) { message ->
        onStatusMessage(message)
    }
    private val backendSelection = createBackend()
    private val backend: Backend? = backendSelection.backend
    private val tunnelName = "MyWireGuardTunnel"
    private var nativeDiagnosticsJob: Job? = null
    private val _status = MutableLiveData(
        VpnRuntimeStatus(
            mode = mode,
            isRunning = false,
            label = RUNTIME_LABEL,
            detail = "Disconnected"
        )
    )
    private val tunnel = object : Tunnel {
        override fun getName() = tunnelName

        override fun onStateChange(state: State) {
            onStatusMessage("WireGuard state: $state")
            if (state == State.UP) {
                // Generate outbound traffic so the peer has a reason to handshake.
                // Starting the local tunnel is not treated as a successful connection.
                onTunnelStarted()
            } else {
                onTunnelDown()
                runtimeScope.launch {
                    lifecycleMutex.withLock { finalizeTelemetryStop() }
                }
            }
        }
    }

    override suspend fun start() = lifecycleMutex.withLock {
        if (backend == null) {
            val detail = backendSelection.unavailableReason ?: "Telemetry backend unavailable"
            update(false, detail)
            onStatusMessage("Error connecting WireGuard: $detail")
            return@withLock
        }
        val config = WireGuardConfigReader.loadConfig(appContext) ?: run {
            update(false, "Configuration not found")
            onStatusMessage("Error: Configuration not found")
            return@withLock
        }
        update(
            isRunning = false,
            detail = "Starting secure tunnel…",
            isConnecting = true
        )
        telemetryCoordinator.start()
        try {
            val attemptStartedAt = System.currentTimeMillis()
            startWithRetry(config)
            startNativeDiagnostics()
            update(
                isRunning = false,
                detail = "Waiting for WireGuard peer handshake…",
                isConnecting = true
            )
            onStatusMessage("Waiting for WireGuard peer handshake")
            if (!awaitConfirmedHandshake(attemptStartedAt)) {
                stopNativeDiagnostics()
                setWireGuardState(State.DOWN, null)
                finalizeTelemetryStop()
                val detail = "Connection failed: WireGuard server did not respond"
                update(isRunning = false, detail = detail)
                onStatusMessage(detail)
                return@withLock
            }

            telemetryCoordinator.onTunnelConfirmed()
            val detail = "Connected; WireGuard handshake confirmed"
            update(isRunning = true, detail = detail)
            onStatusMessage(detail)
            onConnectionConfirmed()
        } catch (error: Exception) {
            stopNativeDiagnostics()
            telemetryCoordinator.stop()
            val reason = error.extractWireGuardReason() ?: error.message ?: "Unknown error"
            update(isRunning = false, detail = reason)
            onStatusMessage("Error connecting WireGuard: $reason")
        }
    }

    override suspend fun stop() = lifecycleMutex.withLock {
        try {
            stopNativeDiagnostics()
            setWireGuardState(State.DOWN, null)
            val snapshot = finalizeTelemetryStop()
            val dropped = snapshot?.nativeTelemetryDropped ?: 0L
            val detail = if (dropped == 0L) {
                "Disconnected"
            } else {
                "Disconnected; telemetry dropped $dropped packets"
            }
            update(false, detail)
            onStatusMessage("WireGuard tunnel disconnected")
        } catch (error: Exception) {
            val reason = error.message ?: "Unknown error"
            update(false, reason)
            onStatusMessage("Error disconnecting WireGuard: $reason")
        }
    }

    override fun status(): LiveData<VpnRuntimeStatus> = _status

    override fun close() {
        runtimeScope.launch {
            runCatching { stop() }
            runtimeScope.cancel()
        }
    }

    private suspend fun startWithRetry(config: Config) {
        try {
            setWireGuardState(State.UP, config)
        } catch (error: Exception) {
            if (!error.extractWireGuardReason().orEmpty().contains("UNABLE_TO_START_VPN")) {
                throw error
            }
            delay(500L)
            setWireGuardState(State.UP, config)
        }
    }

    private suspend fun awaitConfirmedHandshake(attemptStartedAtMillis: Long): Boolean {
        val deadline = SystemClock.elapsedRealtime() + HANDSHAKE_TIMEOUT_MS
        while (SystemClock.elapsedRealtime() < deadline) {
            val statistics = runCatching { backend?.getStatistics(tunnel) }.getOrNull()
            val confirmed = statistics?.peers()?.any { peer ->
                val latestHandshake = statistics.peer(peer)?.latestHandshakeEpochMillis() ?: 0L
                WireGuardHandshakePolicy.isConfirmed(
                    latestHandshakeEpochMillis = latestHandshake,
                    attemptStartedEpochMillis = attemptStartedAtMillis
                )
            } == true
            if (confirmed) return true
            delay(HANDSHAKE_POLL_INTERVAL_MS)
        }
        return false
    }

    private suspend fun setWireGuardState(state: State, config: Config?) = withContext(Dispatchers.IO) {
        checkNotNull(backend) { backendSelection.unavailableReason ?: "Telemetry backend unavailable" }
            .setState(tunnel, state, config)
    }

    private suspend fun finalizeTelemetryStop() = telemetryCoordinator.stop()

    private fun startNativeDiagnostics() {
        val telemetryBackend = backend as? TelemetryGoBackend ?: return
        nativeDiagnosticsJob?.cancel()
        nativeDiagnosticsJob = runtimeScope.launch {
            while (true) {
                telemetryCoordinator.updateNativeStats(
                    telemetryBackend.telemetryQueueDepth,
                    telemetryBackend.telemetryQueueHighWater,
                    telemetryBackend.telemetryDrops
                )
                delay(NATIVE_DIAGNOSTICS_INTERVAL_MS)
            }
        }
    }

    private suspend fun stopNativeDiagnostics() {
        nativeDiagnosticsJob?.cancelAndJoin()
        nativeDiagnosticsJob = null
    }

    private fun createBackend(): BackendSelection {
        return try {
            val telemetryBackend = TelemetryGoBackend(
                appContext,
                telemetryCoordinator,
                telemetryCoordinator
            )
            check(telemetryBackend.version == PINNED_WIREGUARD_GO_VERSION) {
                "Telemetry backend version ${telemetryBackend.version} does not match " +
                    PINNED_WIREGUARD_GO_VERSION
            }
            BackendSelection(backend = telemetryBackend)
        } catch (error: LinkageError) {
            unavailableBackend(error.message ?: error.javaClass.simpleName)
        } catch (error: IllegalStateException) {
            unavailableBackend(error.message ?: "Telemetry native library version mismatch")
        }
    }

    private fun unavailableBackend(detail: String): BackendSelection {
        val reason = "Telemetry native library unavailable: $detail"
        onStatusMessage(reason)
        return BackendSelection(backend = null, unavailableReason = reason)
    }

    private fun update(isRunning: Boolean, detail: String, isConnecting: Boolean = false) {
        _status.postValue(
            VpnRuntimeStatus(
                mode = mode,
                isRunning = isRunning,
                label = RUNTIME_LABEL,
                detail = detail,
                isConnecting = isConnecting
            )
        )
    }

    private fun Exception.extractWireGuardReason(): String? {
        return runCatching {
            javaClass.getDeclaredField("reason").apply { isAccessible = true }.get(this)?.toString()
        }.getOrNull()
    }

    private data class BackendSelection(
        val backend: Backend?,
        val unavailableReason: String? = null
    )

    private companion object {
        private const val RUNTIME_LABEL = "WireGuard with flow telemetry"
        private const val NATIVE_DIAGNOSTICS_INTERVAL_MS = 1_000L
        internal const val PINNED_WIREGUARD_GO_VERSION = "f333402"
        private const val HANDSHAKE_TIMEOUT_MS = 20_000L
        private const val HANDSHAKE_POLL_INTERVAL_MS = 500L
    }
}
