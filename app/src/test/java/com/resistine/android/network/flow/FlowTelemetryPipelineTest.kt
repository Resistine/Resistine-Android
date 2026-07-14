package com.resistine.android.network.flow

import com.resistine.android.network.forwarding.PacketPipelineStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FlowTelemetryPipelineTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `explicit direction supports non-private WireGuard tunnel addresses`() {
        val store = FlowSegmentStore(temporaryFolder.newFolder("wireguard-pipeline"))
        val stats = PacketPipelineStats()
        val flushed = mutableListOf<FlowRecord>()
        val pipeline = FlowTelemetryPipeline(
            writer = AsyncFlowSegmentWriter(store),
            stats = stats,
            onFlowFlushed = flushed::add
        )
        val outbound = PacketFixtures.ipv4TcpPacket(
            src = byteArrayOf(100, 64, 0, 2),
            dst = byteArrayOf(93, 184.toByte(), 216.toByte(), 34),
            srcPort = 51000,
            dstPort = 443,
            payload = byteArrayOf(1, 2, 3)
        )
        val inbound = PacketFixtures.ipv4TcpPacket(
            src = byteArrayOf(93, 184.toByte(), 216.toByte(), 34),
            dst = byteArrayOf(100, 64, 0, 2),
            srcPort = 443,
            dstPort = 51000,
            payload = byteArrayOf(4, 5)
        )

        assertEquals(
            PacketTelemetryResult.ACCEPTED,
            pipeline.ingest(outbound, outbound.size, PacketDirection.OUTBOUND, 1_000L)
        )
        assertEquals(
            PacketTelemetryResult.ACCEPTED,
            pipeline.ingest(inbound, inbound.size, PacketDirection.INBOUND, 1_010L)
        )
        assertEquals(1, pipeline.shutdown(2_000L))

        val record = store.readAllRecords().single()
        assertEquals("100.64.0.2", record.srcIp)
        assertEquals("93.184.216.34", record.dstIp)
        assertEquals(outbound.size.toLong(), record.bytesOut)
        assertEquals(inbound.size.toLong(), record.bytesIn)
        assertEquals(1, flushed.size)
        assertEquals(1L, stats.snapshot().flowsFlushed)
        assertEquals(2L, stats.snapshot().packetsRead)
        assertEquals((outbound.size + inbound.size).toLong(), stats.snapshot().bytesRead)
    }

    @Test
    fun `shutdown is idempotent and rejects subsequent packets`() {
        val store = FlowSegmentStore(temporaryFolder.newFolder("closed-pipeline"))
        val stats = PacketPipelineStats()
        val pipeline = FlowTelemetryPipeline(
            writer = AsyncFlowSegmentWriter(store),
            stats = stats
        )
        val packet = PacketFixtures.ipv4UdpPacket(
            src = byteArrayOf(10, 0, 0, 2),
            dst = byteArrayOf(1, 1, 1, 1),
            srcPort = 42000,
            dstPort = 53,
            payload = PacketFixtures.dnsQuery("example.com")
        )

        assertEquals(0, pipeline.shutdown(1_000L))
        assertEquals(0, pipeline.shutdown(1_001L))
        assertEquals(
            PacketTelemetryResult.PIPELINE_CLOSED,
            pipeline.ingest(packet, packet.size, PacketDirection.OUTBOUND, 1_002L)
        )

        val snapshot = stats.snapshot()
        assertEquals(1L, snapshot.packetsRejectedAfterClose)
        assertFalse(store.readAllRecords().isNotEmpty())
    }

    @Test
    fun `periodic expiry flushes idle flows without another packet`() {
        val store = FlowSegmentStore(temporaryFolder.newFolder("expiry-pipeline"))
        val stats = PacketPipelineStats()
        val pipeline = FlowTelemetryPipeline(
            writer = AsyncFlowSegmentWriter(store),
            stats = stats
        )
        val packet = PacketFixtures.ipv4UdpPacket(
            src = byteArrayOf(10, 0, 0, 2),
            dst = byteArrayOf(1, 1, 1, 1),
            srcPort = 42000,
            dstPort = 53,
            payload = PacketFixtures.dnsQuery("example.com")
        )

        pipeline.ingest(packet, packet.size, PacketDirection.OUTBOUND, 1_000L)

        assertEquals(1, pipeline.flushExpired(31_000L))
        assertEquals(0, pipeline.shutdown(31_001L))
        assertEquals(1, store.readAllRecords().size)
    }

    @Test
    fun `malformed packet is rejected and counted`() {
        val store = FlowSegmentStore(temporaryFolder.newFolder("rejected-pipeline"))
        val stats = PacketPipelineStats()
        val pipeline = FlowTelemetryPipeline(
            writer = AsyncFlowSegmentWriter(store),
            stats = stats
        )

        assertEquals(
            PacketTelemetryResult.PARSE_REJECTED,
            pipeline.ingest(byteArrayOf(1, 2, 3), 3, PacketDirection.OUTBOUND, 1_000L)
        )
        pipeline.shutdown(1_001L)

        val snapshot = stats.snapshot()
        assertEquals(1L, snapshot.parserFailure)
        assertTrue(store.readAllRecords().isEmpty())
    }
}
