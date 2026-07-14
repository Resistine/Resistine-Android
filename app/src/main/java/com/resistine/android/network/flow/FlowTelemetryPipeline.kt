package com.resistine.android.network.flow

import com.resistine.android.network.forwarding.PacketPipelineStats
import java.util.concurrent.atomic.AtomicBoolean

class FlowTelemetryPipeline(
    private val writer: AsyncFlowSegmentWriter,
    private val stats: PacketPipelineStats,
    private val parser: TunPacketParser = TunPacketParser(),
    private val aggregator: FlowAggregator = FlowAggregator(),
    private val ingestContext: FlowIngestContext = FlowIngestContext(),
    private val onFlowFlushed: (FlowRecord) -> Unit = {}
) : PacketTelemetrySink {
    private val closed = AtomicBoolean(false)
    private val lock = Any()

    init {
        writer.start()
    }

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
        persist(aggregator.ingest(parsed, ingestContext).flushed)
        PacketTelemetryResult.ACCEPTED
    }

    fun shutdown(timestampMillis: Long = System.currentTimeMillis()): Int = synchronized(lock) {
        if (!closed.compareAndSet(false, true)) return@synchronized 0
        val pending = aggregator.flushAll(timestampMillis)
        persist(pending)
        writer.close()
        pending.size
    }

    fun flushExpired(timestampMillis: Long = System.currentTimeMillis()): Int = synchronized(lock) {
        if (closed.get()) return@synchronized 0
        val expired = aggregator.flushExpired(timestampMillis)
        persist(expired)
        expired.size
    }

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
