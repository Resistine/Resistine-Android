package com.resistine.android.network.wireguard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.resistine.android.database.AppDatabase
import com.resistine.android.database.LogEntry
import com.resistine.android.network.WazuhConfigManager
import com.resistine.android.network.WazuhConnectionMonitor
import com.resistine.android.network.WazuhConnectionState
import com.resistine.android.network.WazuhRemoteReadinessValidator
import com.resistine.android.network.flow.AndroidFlowIngestContextResolver
import com.resistine.android.network.flow.AsyncFlowSegmentWriter
import com.resistine.android.network.flow.FlowSegmentStore
import com.resistine.android.network.flow.FlowTelemetryPipeline
import com.resistine.android.network.flow.FlowWazuhDeliveryMode
import com.resistine.android.network.flow.FlowWazuhDeliveryStore
import com.resistine.android.network.flow.PacketDirection
import com.resistine.android.network.flow.PacketTelemetryResult
import com.resistine.android.network.flow.PacketTelemetrySink
import com.resistine.android.network.flow.WazuhFlowEventFormatter
import com.resistine.android.network.forwarding.PacketPipelineSnapshot
import com.resistine.android.network.forwarding.PacketPipelineStats
import com.resistine.android.network.forwarding.FlowTelemetryDiagnosticsMonitor
import com.resistine.android.service.WazuhService
import com.wireguard.android.backend.TelemetryGoBackend
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WireGuardFlowTelemetryCoordinator(
    context: Context,
    private val onHealthMessage: (String) -> Unit = {}
) : PacketTelemetrySink, TelemetryGoBackend.TelemetryHealthListener {
    private val appContext = context.applicationContext
    private val activePipeline = AtomicReference<FlowTelemetryPipeline?>(null)
    private val stopMutex = Mutex()
    private val logDao by lazy { AppDatabase.getDatabase(appContext).logDao() }

    @Volatile
    private var sessionScope: CoroutineScope? = null

    @Volatile
    private var sessionStats: PacketPipelineStats? = null

    @Volatile
    private var wazuhQueue: Channel<String>? = null

    @Volatile
    private var wazuhQueueWriter: Job? = null

    @Volatile
    private var expiryFlusher: Job? = null

    @Volatile
    private var diagnosticsPublisher: Job? = null

    @Volatile
    private var sessionWriter: AsyncFlowSegmentWriter? = null

    @Volatile
    private var lastSnapshot: PacketPipelineSnapshot? = null

    @Synchronized
    fun start() {
        if (activePipeline.get() != null) return
        FlowTelemetryDiagnosticsMonitor.beginSession(appContext)
        WazuhConnectionMonitor.beginTelemetrySession()
        val stats = PacketPipelineStats()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val queue = Channel<String>(WAZUH_QUEUE_CAPACITY)
        val queueWriter = scope.launch { persistWazuhQueue(queue) }
        val store = FlowSegmentStore(File(appContext.filesDir, FLOW_SEGMENT_DIR))
        val writer = AsyncFlowSegmentWriter(store) { error ->
            stats.recordSegmentWriteFailure()
            Log.e(TAG, "Flow segment writer failed", error)
            onHealthMessage("Flow segment persistence failed: ${error.message}")
        }
        val pipeline = FlowTelemetryPipeline(
            writer = writer,
            stats = stats,
            contextResolver = AndroidFlowIngestContextResolver(appContext),
            onFlowFlushed = { flow ->
                queueFlow(queue, stats, WazuhFlowEventFormatter.format(flow))
            }
        )
        sessionStats = stats
        sessionWriter = writer
        sessionScope = scope
        wazuhQueue = queue
        wazuhQueueWriter = queueWriter
        activePipeline.set(pipeline)
        expiryFlusher = scope.launch {
            while (isActive) {
                delay(FLOW_EXPIRY_INTERVAL_MS)
                pipeline.flushExpired(System.currentTimeMillis())
            }
        }
        diagnosticsPublisher = scope.launch {
            while (isActive) {
                publishDiagnostics(stats, writer)
                delay(DIAGNOSTICS_INTERVAL_MS)
            }
        }
        prepareWazuhDeliveryForHandshake()
    }

    @Synchronized
    fun onTunnelConfirmed() {
        if (activePipeline.get() == null) return
        runCatching { configureWazuhDelivery() }.onFailure { error ->
            reportWazuhConfigurationError(error.message ?: "Could not configure Wazuh delivery")
        }
    }

    suspend fun stop(): PacketPipelineSnapshot? = stopMutex.withLock {
        val pipeline = activePipeline.getAndSet(null)
        if (pipeline == null && sessionScope == null) {
            return@withLock lastSnapshot
        }
        expiryFlusher?.cancelAndJoin()
        expiryFlusher = null
        diagnosticsPublisher?.cancelAndJoin()
        diagnosticsPublisher = null
        pipeline?.shutdown(System.currentTimeMillis())
        wazuhQueue?.close()
        wazuhQueueWriter?.join()
        sessionStats?.updateSegmentQueueDepth(sessionWriter?.queueDepth() ?: 0)
        val snapshot = sessionStats?.snapshot() ?: lastSnapshot
        lastSnapshot = snapshot
        snapshot?.let { FlowTelemetryDiagnosticsMonitor.publish(appContext, it) }
        sessionScope?.cancel()
        sessionScope = null
        sessionStats = null
        sessionWriter = null
        wazuhQueue = null
        wazuhQueueWriter = null
        stopWazuhUploader()
        snapshot?.let { Log.i(TAG, "WireGuard flow telemetry stopped: $it") }
        snapshot
    }

    fun snapshot(): PacketPipelineSnapshot? {
        sessionStats?.updateSegmentQueueDepth(sessionWriter?.queueDepth() ?: 0)
        return sessionStats?.snapshot() ?: lastSnapshot
    }

    override fun ingest(
        packet: ByteArray,
        length: Int,
        direction: PacketDirection,
        timestampMillis: Long
    ): PacketTelemetryResult {
        val pipeline = activePipeline.get() ?: return PacketTelemetryResult.PIPELINE_CLOSED
        return pipeline.ingest(packet, length, direction, timestampMillis)
    }

    override fun onNativePacketDrops(count: Long) {
        if (count <= 0L) return
        sessionStats?.updateNativeTelemetryDropped(count)
        Log.w(TAG, "Native WireGuard telemetry dropped $count packets")
        onHealthMessage("Telemetry dropped $count packets under load")
    }

    override fun onTelemetryReaderFailure(error: Throwable) {
        sessionStats?.recordTelemetryReaderFailure()
        Log.e(TAG, "WireGuard telemetry reader failed", error)
        onHealthMessage("Telemetry reader failure: ${error.message ?: error.javaClass.simpleName}")
    }

    override fun onNativeQueueStats(depth: Long, highWater: Long) {
        sessionStats?.updateNativeQueueStats(depth, highWater)
    }

    fun updateNativeStats(depth: Long, highWater: Long, dropped: Long) {
        onNativeQueueStats(depth, highWater)
        sessionStats?.updateNativeTelemetryDropped(dropped)
    }

    private fun queueFlow(queue: Channel<String>, stats: PacketPipelineStats, message: String) {
        stats.recordWazuhQueued()
        if (queue.trySend(message).isFailure) {
            stats.recordWazuhDequeued()
            stats.recordWazuhQueueDropped()
            Log.w(TAG, "Wazuh persistence queue is full; local segment remains available")
        }
    }

    private suspend fun persistWazuhQueue(queue: Channel<String>) {
        for (message in queue) {
            sessionStats?.recordWazuhDequeued()
            runCatching {
                val trimmed = logDao.insertBounded(
                    LogEntry(timestamp = System.currentTimeMillis(), message = message)
                )
                if (trimmed > 0) {
                    sessionStats?.recordWazuhQueueDropped(trimmed)
                    onHealthMessage("Wazuh local queue reached its retention limit")
                }
            }.onFailure { error ->
                sessionStats?.recordWazuhQueueDropped()
                Log.w(TAG, "Failed to persist WireGuard flow for Wazuh", error)
                onHealthMessage("Failed to queue flow event: ${error.message}")
            }
        }
    }

    private fun publishDiagnostics(stats: PacketPipelineStats, writer: AsyncFlowSegmentWriter) {
        stats.updateSegmentQueueDepth(writer.queueDepth())
        FlowTelemetryDiagnosticsMonitor.publish(appContext, stats.snapshot())
    }

    private fun configureWazuhDelivery() {
        when (FlowWazuhDeliveryStore.fromContext(appContext).load()) {
            FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY -> {
                stopWazuhUploader()
                WazuhConnectionMonitor.update(
                    WazuhConnectionState.LOCAL_ONLY,
                    "Flow records remain on this device"
                )
            }

            FlowWazuhDeliveryMode.REMOTE_MANAGER -> {
                val config = WazuhConfigManager.getInstance(appContext)
                val readiness = runCatching {
                    WazuhRemoteReadinessValidator.validate(config.endpoint())
                }.getOrElse { error ->
                    reportWazuhConfigurationError(
                        error.message ?: "Invalid Wazuh manager configuration"
                    )
                    return
                }
                if (!readiness.ready) {
                    reportWazuhConfigurationError(readiness.issues.joinToString("; "))
                    return
                }
                val intent = Intent(appContext, WazuhService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            }
        }
    }

    private fun prepareWazuhDeliveryForHandshake() {
        when (FlowWazuhDeliveryStore.fromContext(appContext).load()) {
            FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY -> configureWazuhDelivery()
            FlowWazuhDeliveryMode.REMOTE_MANAGER -> {
                appContext.stopService(Intent(appContext, WazuhService::class.java))
                WazuhConnectionMonitor.update(
                    WazuhConnectionState.WAITING_FOR_VPN,
                    "Waiting for confirmed WireGuard handshake"
                )
            }
        }
    }

    private fun reportWazuhConfigurationError(detail: String) {
        WazuhConnectionMonitor.update(WazuhConnectionState.ERROR, detail)
        onHealthMessage(detail)
    }

    private fun stopWazuhUploader() {
        appContext.stopService(Intent(appContext, WazuhService::class.java))
        WazuhConnectionMonitor.update(WazuhConnectionState.STOPPED, "Uploader stopped")
    }

    private companion object {
        private const val TAG = "WireGuardFlowTelemetry"
        private const val FLOW_SEGMENT_DIR = "flow-segments"
        private const val FLOW_EXPIRY_INTERVAL_MS = 5_000L
        private const val DIAGNOSTICS_INTERVAL_MS = 1_000L
        private const val WAZUH_QUEUE_CAPACITY = 2_048
    }
}
