package com.resistine.android.network.flow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FlowPipelineIntegrationTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `packets flow through parse aggregate segment and wazuh export`() {
        val parser = TunPacketParser()
        val aggregator = FlowAggregator(tcpIdleTimeoutMillis = 1L)
        val store = FlowSegmentStore(temporaryFolder.newFolder("pipeline-flows"))
        val context = FlowIngestContext(
            networkType = FlowNetworkType.WIFI,
            appUid = 10444,
            appPackage = "com.example.browser"
        )

        val httpPacket = PacketFixtures.ipv4TcpPacket(
            src = byteArrayOf(10, 0, 0, 2),
            dst = byteArrayOf(93, 184.toByte(), 216.toByte(), 34),
            srcPort = 50000,
            dstPort = 80,
            payload = "GET / HTTP/1.1\r\nHost: example.com\r\n\r\n".toByteArray(Charsets.ISO_8859_1)
        )
        val flushTriggerPacket = PacketFixtures.ipv4UdpPacket(
            src = byteArrayOf(10, 0, 0, 2),
            dst = byteArrayOf(1, 1, 1, 1),
            srcPort = 41000,
            dstPort = 53,
            payload = PacketFixtures.dnsQuery("one.one.one.one")
        )

        val first = parser.parse(httpPacket, httpPacket.size, 1_000L)!!
        aggregator.ingest(first, context).flushed.forEach(store::append)
        val second = parser.parse(flushTriggerPacket, flushTriggerPacket.size, 1_010L)!!
        aggregator.ingest(second, context).flushed.forEach(store::append)
        aggregator.flushExpired(1_010L).forEach(store::append)

        val events = store.segmentFiles()
            .flatMap(store::readRecords)
            .map(WazuhFlowEventFormatter::format)

        assertEquals(1, events.size)
        assertTrue(events.single().contains("\"event_type\":\"resistine_flow\""))
        assertTrue(events.single().contains("\"http_host\":\"example.com\""))
        assertTrue(events.single().contains("\"app_uid\":10444"))
    }
}
