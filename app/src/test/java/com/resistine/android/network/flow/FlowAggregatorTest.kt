package com.resistine.android.network.flow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowAggregatorTest {
    @Test
    fun `aggregates bidirectional packets into one flow`() {
        val aggregator = FlowAggregator(tcpIdleTimeoutMillis = 60_000L)
        val start = 1_000L
        val context = FlowIngestContext(
            networkType = FlowNetworkType.WIFI,
            appUid = 10123,
            appPackage = "com.example.app"
        )

        aggregator.ingest(
            PacketMetadata(
                timestampMillis = start,
                ipVersion = 4,
                protocolCode = 6,
                srcIp = "10.0.0.2",
                srcPort = 50000,
                dstIp = "1.1.1.1",
                dstPort = 443,
                bytes = 120,
                outbound = true
            ),
            context
        )
        aggregator.ingest(
            PacketMetadata(
                timestampMillis = start + 10,
                ipVersion = 4,
                protocolCode = 6,
                srcIp = "1.1.1.1",
                srcPort = 443,
                dstIp = "10.0.0.2",
                dstPort = 50000,
                bytes = 240,
                outbound = false
            ),
            context
        )

        val flushed = aggregator.flushAll(start + 20)

        assertEquals(1, flushed.size)
        val record = flushed.single()
        assertEquals(120L, record.bytesOut)
        assertEquals(240L, record.bytesIn)
        assertEquals(1, record.packetsOut)
        assertEquals(1, record.packetsIn)
        assertEquals(start, record.timestampStartMillis)
        assertEquals(start + 10, record.timestampEndMillis)
        assertEquals(FlowNetworkType.WIFI, record.networkType)
        assertEquals(10123, record.appUid)
        assertEquals("com.example.app", record.appPackage)
    }

    @Test
    fun `flushes udp flow after udp idle timeout`() {
        val aggregator = FlowAggregator(udpIdleTimeoutMillis = 30_000L)

        aggregator.ingest(
            PacketMetadata(
                timestampMillis = 1_000L,
                ipVersion = 4,
                protocolCode = 17,
                srcIp = "10.0.0.2",
                srcPort = 40000,
                dstIp = "8.8.8.8",
                dstPort = 53,
                bytes = 80,
                outbound = true
            )
        )

        val expiredFlows = aggregator.flushExpired(31_001L)

        val expired = expiredFlows.single { it.dstIp == "8.8.8.8" }
        assertEquals(1_000L, expired.timestampStartMillis)
        assertEquals(1_000L, expired.timestampEndMillis)
    }

    @Test
    fun `flushes hard timeout even when flow is active`() {
        val aggregator = FlowAggregator(
            tcpIdleTimeoutMillis = 60_000L,
            hardTimeoutMillis = 5_000L
        )

        aggregator.ingest(
            PacketMetadata(
                timestampMillis = 10_000L,
                ipVersion = 4,
                protocolCode = 6,
                srcIp = "10.0.0.2",
                srcPort = 50000,
                dstIp = "9.9.9.9",
                dstPort = 443,
                bytes = 100,
                outbound = true
            )
        )

        aggregator.ingest(
            PacketMetadata(
                timestampMillis = 15_001L,
                ipVersion = 4,
                protocolCode = 6,
                srcIp = "9.9.9.9",
                srcPort = 443,
                dstIp = "10.0.0.2",
                dstPort = 50000,
                bytes = 100,
                outbound = false
            )
        )
        val expiredFlows = aggregator.flushExpired(15_001L)

        assertTrue(expiredFlows.isNotEmpty())
    }

    @Test
    fun `evicts oldest flow when active flow capacity is reached`() {
        val aggregator = FlowAggregator(maxActiveFlows = 1)
        val first = PacketMetadata(
            timestampMillis = 1_000L,
            ipVersion = 4,
            protocolCode = 17,
            srcIp = "10.0.0.2",
            srcPort = 40000,
            dstIp = "8.8.8.8",
            dstPort = 53,
            bytes = 80,
            outbound = true
        )
        val second = first.copy(
            timestampMillis = 1_001L,
            srcPort = 40001,
            dstIp = "1.1.1.1"
        )

        aggregator.ingest(first)
        val result = aggregator.ingest(second)

        assertEquals(1, result.activeFlowCount)
        assertEquals("8.8.8.8", result.flushed.single().dstIp)
    }

    @Test
    fun `network handoff creates separate flow records`() {
        val aggregator = FlowAggregator()
        val packet = PacketMetadata(
            timestampMillis = 1_000L,
            ipVersion = 4,
            protocolCode = 6,
            srcIp = "10.0.0.2",
            srcPort = 50000,
            dstIp = "1.1.1.1",
            dstPort = 443,
            bytes = 100,
            outbound = true
        )

        aggregator.ingest(packet, FlowIngestContext(networkType = FlowNetworkType.WIFI))
        aggregator.ingest(
            packet.copy(timestampMillis = 2_000L),
            FlowIngestContext(networkType = FlowNetworkType.CELLULAR)
        )

        val records = aggregator.flushAll(3_000L)

        assertEquals(2, records.size)
        assertEquals(
            setOf(FlowNetworkType.WIFI, FlowNetworkType.CELLULAR),
            records.map(FlowRecord::networkType).toSet()
        )
    }
}
