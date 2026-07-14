package com.resistine.android.network.forwarding

import org.junit.Assert.assertEquals
import org.junit.Test

class PacketPipelineStatsTest {
    @Test
    fun `discarded shutdown packets clear queue depth and remain observable`() {
        val stats = PacketPipelineStats()
        stats.recordForwardEnqueued()
        stats.recordForwardEnqueued()
        stats.recordForwardDequeued()

        stats.recordForwardDiscardedOnStop(1)

        val snapshot = stats.snapshot()
        assertEquals(0, snapshot.forwardQueueDepth)
        assertEquals(1L, snapshot.forwardQueueDiscardedOnStop)
        assertEquals(0L, snapshot.forwardQueueDropped)
    }

    @Test
    fun `packet removed during shutdown is counted without changing zero depth`() {
        val stats = PacketPipelineStats()
        stats.recordForwardEnqueued()
        stats.recordForwardDequeued()

        stats.recordForwardDiscardedAfterDequeue()

        val snapshot = stats.snapshot()
        assertEquals(0, snapshot.forwardQueueDepth)
        assertEquals(1L, snapshot.forwardQueueDiscardedOnStop)
    }

    @Test
    fun `forwarder stop atomically accounts for in-flight queue depth`() {
        val stats = PacketPipelineStats()
        stats.recordForwardEnqueued()

        stats.recordForwarderStopped()

        val snapshot = stats.snapshot()
        assertEquals(0, snapshot.forwardQueueDepth)
        assertEquals(1L, snapshot.forwardQueueDiscardedOnStop)
    }

    @Test
    fun `native telemetry health counters remain observable`() {
        val stats = PacketPipelineStats()

        stats.recordNativeTelemetryDropped(3)
        stats.recordTelemetryReaderFailure()
        stats.recordWazuhQueueDropped()

        val snapshot = stats.snapshot()
        assertEquals(3L, snapshot.nativeTelemetryDropped)
        assertEquals(1L, snapshot.telemetryReaderFailures)
        assertEquals(1L, snapshot.wazuhQueueDropped)
    }
}
