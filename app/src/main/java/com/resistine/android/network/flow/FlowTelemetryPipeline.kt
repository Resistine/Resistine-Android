package com.resistine.android.network.flow

import com.resistine.android.network.forwarding.PacketPipelineStats
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Pipeline coordinating packet ingestion, parsing, flow aggregation, and asynchronous persistence.
 *
 * @param writer [AsyncFlowSegmentWriter] for disk persistence.
 * @param stats [PacketPipelineStats] tracking pipeline metrics.
 * @param parser [TunPacketParser] for parsing IP packets.
 * @param aggregator [FlowAggregator] for aggregating packets into flows.
 * @param contextResolver [FlowIngestContextResolver] for resolving app attribution and network type.
 * @param onFlowFlushed Callback invoked when a flow record is flushed.
 */
class FlowTelemetryPipeline(
    private val writer: AsyncFlowSegmentWriter,
    private val stats: PacketPipelineStats,
    private val parser: TunPacketParser = TunPacketParser(),
    private val aggregator: FlowAggregator = FlowAggregator(),
    private val contextResolver: FlowIngestContextResolver =
        FlowIngestContextResolver { FlowIngestContext() },
    private val onFlowFlushed: (FlowRecord) -> Unit = {}
) : PacketTelemetrySink {
    private val closed = AtomicBoolean(false)
    private val lock = Any()

    init {
        writer.start()
    }

    /**
     * Ingests a raw packet into the pipeline.
     *
     * @param packet Raw packet byte array.
     * @param length Number of valid bytes in the packet.
     * @param direction [PacketDirection] (outbound or inbound).
     * @param timestampMillis Timestamp in milliseconds.
     * @return [PacketTelemetryResult] indicating acceptance or rejection status.
     */
    override fun ingest(
        packet: ByteArray,
        length: Int,
        direction: PacketDirection,
        timestampMillis: Long
    ): PacketTelemetryResult = synchronized(lock) {
        if (closed.get()) {
            stats.recordPacketRejectedAfterClose()
            return@synchronized PacketTelemetryResult.PIPELINE_CLOSED
        }

        stats.recordRead(length)
        val parsed = parser.parse(packet, length, timestampMillis, direction)
        if (parsed == null) {
            stats.recordParseFailure()
            return@synchronized PacketTelemetryResult.PARSE_REJECTED
        }

        stats.recordParseSuccess()
        persist(aggregator.ingest(parsed, contextResolver.resolve(parsed)).flushed)
        PacketTelemetryResult.ACCEPTED
    }

    /**
     * Shuts down the telemetry pipeline, flushing all active flows and closing the writer.
     *
     * @param timestampMillis Shutdown timestamp in milliseconds.
     * @return Number of flushed flows.
     */
    fun shutdown(timestampMillis: Long = System.currentTimeMillis()): Int = synchronized(lock) {
        if (!closed.compareAndSet(false, true)) return@synchronized 0
        val pending = aggregator.flushAll(timestampMillis)
        persist(pending)
        writer.close()
        pending.size
    }

    /**
     * Flushes expired flows based on idle or hard timeouts.
     *
     * @param timestampMillis Current timestamp in milliseconds.
     * @return Number of flushed expired flows.
     */
    fun flushExpired(timestampMillis: Long = System.currentTimeMillis()): Int = synchronized(lock) {
        if (closed.get()) return@synchronized 0
        val expired = aggregator.flushExpired(timestampMillis)
        persist(expired)
        expired.size
    }

    /**
     * Persists a list of flow records through the writer and notifies listeners.
     *
     * @param flows List of [FlowRecord] items.
     */
    private fun persist(flows: List<FlowRecord>) {
        if (flows.isEmpty()) return
        stats.recordFlowsFlushed(flows.size)
        flows.forEach { flow ->
            if (!writer.offer(flow)) {
                stats.recordSegmentQueueDropped()
            }
            onFlowFlushed(flow)
        }
    }
}
