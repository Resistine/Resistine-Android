package com.resistine.android.ui.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

enum class WifiScanFreshness {
    FRESH,
    CACHED,
    STALE,
    UNAVAILABLE
}

enum class WifiScanRequestStatus {
    NOT_REQUESTED,
    UPDATED,
    COOLDOWN,
    REJECTED,
    TIMED_OUT
}

data class WifiScanSnapshot(
    val results: List<ScanResult>,
    val freshness: WifiScanFreshness,
    val ageMillis: Long?,
    val freshScanRequested: Boolean,
    val freshScanAccepted: Boolean,
    val resultsUpdated: Boolean,
    val requestStatus: WifiScanRequestStatus = WifiScanRequestStatus.NOT_REQUESTED,
    val cooldownRemainingMillis: Long = 0L,
    val metrics: WifiScanEffectivenessMetrics = WifiScanEffectivenessMetrics()
)

internal object WifiScanRequestPolicy {
    fun cooldownRemainingMillis(
        nowElapsedMillis: Long,
        lastRequestElapsedMillis: Long?,
        cooldownMillis: Long
    ): Long {
        if (lastRequestElapsedMillis == null) return 0L
        val elapsed = (nowElapsedMillis - lastRequestElapsedMillis).coerceAtLeast(0L)
        return (cooldownMillis - elapsed).coerceAtLeast(0L)
    }
}

internal class WifiScanCompletionGate {
    private val completed = AtomicBoolean(false)

    fun tryComplete(): Boolean = completed.compareAndSet(false, true)
}

internal class WifiScanRepository(context: Context) {

    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)
    private val requestMutex = Mutex()
    private var lastRequestElapsedMillis: Long? = null
    private var passiveReceiver: BroadcastReceiver? = null
    private val metricsStore = WifiScanMetricsStore(appContext)

    fun startMonitoring(onResultsAvailable: () -> Unit) {
        if (passiveReceiver != null) return
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) == true) {
                    onResultsAvailable()
                }
            }
        }
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                appContext.registerReceiver(receiver, filter)
            }
            passiveReceiver = receiver
        }
    }

    fun stopMonitoring() {
        val receiver = passiveReceiver ?: return
        passiveReceiver = null
        runCatching { appContext.unregisterReceiver(receiver) }
    }

    @SuppressLint("MissingPermission")
    suspend fun snapshot(requestFreshScan: Boolean): WifiScanSnapshot {
        val requestStartedAt = SystemClock.elapsedRealtime()
        val manager = wifiManager ?: run {
            if (requestFreshScan) {
                metricsStore.record(
                    WifiScanRequestStatus.REJECTED,
                    SystemClock.elapsedRealtime() - requestStartedAt
                )
            }
            return unavailableSnapshot(requestFreshScan)
        }
        if (!requestFreshScan) {
            return buildSnapshot(
                results = readResults(manager),
                freshScanRequested = false,
                freshScanAccepted = false,
                resultsUpdated = false
            )
        }

        return requestMutex.withLock {
            val now = SystemClock.elapsedRealtime()
            val cooldownRemaining = WifiScanRequestPolicy.cooldownRemainingMillis(
                nowElapsedMillis = now,
                lastRequestElapsedMillis = lastRequestElapsedMillis,
                cooldownMillis = REQUEST_COOLDOWN_MILLIS
            )
            if (cooldownRemaining > 0L) {
                val metrics = metricsStore.record(
                    WifiScanRequestStatus.COOLDOWN,
                    SystemClock.elapsedRealtime() - requestStartedAt
                )
                return@withLock buildSnapshot(
                    results = readResults(manager),
                    freshScanRequested = true,
                    freshScanAccepted = false,
                    resultsUpdated = false,
                    requestStatus = WifiScanRequestStatus.COOLDOWN,
                    cooldownRemainingMillis = cooldownRemaining,
                    metrics = metrics
                )
            }

            lastRequestElapsedMillis = now
            val scanOutcome = awaitScanOutcome(manager)
            val requestStatus = when {
                scanOutcome.updated -> WifiScanRequestStatus.UPDATED
                !scanOutcome.accepted -> WifiScanRequestStatus.REJECTED
                else -> WifiScanRequestStatus.TIMED_OUT
            }
            val metrics = metricsStore.record(
                requestStatus,
                SystemClock.elapsedRealtime() - requestStartedAt
            )
            buildSnapshot(
                results = readResults(manager),
                freshScanRequested = true,
                freshScanAccepted = scanOutcome.accepted,
                resultsUpdated = scanOutcome.updated,
                requestStatus = requestStatus,
                metrics = metrics
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitScanOutcome(manager: WifiManager): ScanOutcome {
        return withTimeoutOrNull(SCAN_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val completionGate = WifiScanCompletionGate()
                val registered = AtomicBoolean(false)
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val updated = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) == true
                        if (!completionGate.tryComplete()) return
                        if (registered.compareAndSet(true, false)) {
                            runCatching { appContext.unregisterReceiver(this) }
                        }
                        continuation.resume(ScanOutcome(accepted = true, updated = updated))
                    }
                }
                try {
                    val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                    registered.set(true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                    } else {
                        @Suppress("DEPRECATION")
                        appContext.registerReceiver(receiver, filter)
                    }
                    val accepted = runCatching { manager.startScan() }.getOrDefault(false)
                    if (!accepted && completionGate.tryComplete()) {
                        if (registered.compareAndSet(true, false)) {
                            runCatching { appContext.unregisterReceiver(receiver) }
                        }
                        continuation.resume(ScanOutcome(accepted = false, updated = false))
                    }
                } catch (_: Exception) {
                    if (completionGate.tryComplete()) {
                        if (registered.compareAndSet(true, false)) {
                            runCatching { appContext.unregisterReceiver(receiver) }
                        }
                        continuation.resume(ScanOutcome(accepted = false, updated = false))
                    }
                }
                continuation.invokeOnCancellation {
                    if (completionGate.tryComplete() && registered.compareAndSet(true, false)) {
                        runCatching { appContext.unregisterReceiver(receiver) }
                    }
                }
            }
        } ?: ScanOutcome(accepted = true, updated = false)
    }

    @SuppressLint("MissingPermission")
    private fun readResults(manager: WifiManager): List<ScanResult> {
        return runCatching { manager.scanResults.orEmpty() }.getOrDefault(emptyList())
    }

    private fun buildSnapshot(
        results: List<ScanResult>,
        freshScanRequested: Boolean,
        freshScanAccepted: Boolean,
        resultsUpdated: Boolean,
        requestStatus: WifiScanRequestStatus = WifiScanRequestStatus.NOT_REQUESTED,
        cooldownRemainingMillis: Long = 0L,
        metrics: WifiScanEffectivenessMetrics = metricsStore.snapshot()
    ): WifiScanSnapshot {
        val ageMillis = newestResultAgeMillis(results)
        val freshness = when {
            results.isEmpty() -> WifiScanFreshness.UNAVAILABLE
            resultsUpdated && ageMillis != null && ageMillis <= FRESH_RESULT_MAX_AGE_MILLIS -> WifiScanFreshness.FRESH
            ageMillis != null && ageMillis <= CACHED_RESULT_MAX_AGE_MILLIS -> WifiScanFreshness.CACHED
            else -> WifiScanFreshness.STALE
        }
        return WifiScanSnapshot(
            results = results,
            freshness = freshness,
            ageMillis = ageMillis,
            freshScanRequested = freshScanRequested,
            freshScanAccepted = freshScanAccepted,
            resultsUpdated = resultsUpdated,
            requestStatus = requestStatus,
            cooldownRemainingMillis = cooldownRemainingMillis,
            metrics = metrics
        )
    }

    private fun newestResultAgeMillis(results: List<ScanResult>): Long? {
        val newestTimestampMicros = results.maxOfOrNull { it.timestamp }?.takeIf { it > 0L } ?: return null
        val elapsedMicros = SystemClock.elapsedRealtime() * 1_000L
        return ((elapsedMicros - newestTimestampMicros) / 1_000L).coerceAtLeast(0L)
    }

    private fun unavailableSnapshot(requested: Boolean) = WifiScanSnapshot(
        results = emptyList(),
        freshness = WifiScanFreshness.UNAVAILABLE,
        ageMillis = null,
        freshScanRequested = requested,
        freshScanAccepted = false,
        resultsUpdated = false,
        requestStatus = if (requested) WifiScanRequestStatus.REJECTED else WifiScanRequestStatus.NOT_REQUESTED,
        metrics = metricsStore.snapshot()
    )

    private data class ScanOutcome(val accepted: Boolean, val updated: Boolean)

    private companion object {
        const val SCAN_TIMEOUT_MILLIS = 5_000L
        const val FRESH_RESULT_MAX_AGE_MILLIS = 15_000L
        const val CACHED_RESULT_MAX_AGE_MILLIS = 120_000L
        const val REQUEST_COOLDOWN_MILLIS = 30_000L
    }
}
