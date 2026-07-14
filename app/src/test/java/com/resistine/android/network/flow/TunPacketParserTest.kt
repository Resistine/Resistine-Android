package com.resistine.android.network.flow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TunPacketParserTest {
    @Test
    fun `parses ipv4 tcp http request evidence`() {
        val payload = (
            "GET /login HTTP/1.1\r\n" +
                "Host: example.com\r\n" +
                "User-Agent: test\r\n\r\n"
            ).toByteArray(Charsets.ISO_8859_1)
        val packet = PacketFixtures.ipv4TcpPacket(
            src = byteArrayOf(10, 0, 0, 2),
            dst = byteArrayOf(93, 184.toByte(), 216.toByte(), 34),
            srcPort = 50000,
            dstPort = 80,
            payload = payload
        )

        val parsed = TunPacketParser().parse(packet, packet.size, 1_700_000_000_000L)

        assertNotNull(parsed)
        assertEquals(4, parsed?.ipVersion)
        assertEquals(6, parsed?.protocolCode)
        assertEquals("10.0.0.2", parsed?.srcIp)
        assertEquals("93.184.216.34", parsed?.dstIp)
        assertEquals(true, parsed?.outbound)
        assertEquals(payload.size, parsed?.payloadBytes)
        assertEquals("GET", parsed?.protocolEvidence?.httpMethod)
        assertEquals("example.com", parsed?.protocolEvidence?.httpHost)
    }

    @Test
    fun `parses ipv4 udp dns query evidence`() {
        val payload = PacketFixtures.dnsQuery("example.com", qtype = 1)
        val packet = PacketFixtures.ipv4UdpPacket(
            src = byteArrayOf(10, 0, 0, 2),
            dst = byteArrayOf(1, 1, 1, 1),
            srcPort = 41000,
            dstPort = 53,
            payload = payload
        )

        val parsed = TunPacketParser().parse(packet, packet.size, 1_700_000_000_001L)

        assertNotNull(parsed)
        assertEquals(17, parsed?.protocolCode)
        assertEquals("example.com", parsed?.protocolEvidence?.dnsQueryName)
        assertEquals("A", parsed?.protocolEvidence?.dnsQueryType)
    }

    @Test
    fun `parses ipv6 udp packet`() {
        val packet = PacketFixtures.ipv6UdpPacket(
            src = PacketFixtures.ipv6Address(0xfd00, 0, 0, 0, 0, 0, 0, 2),
            dst = PacketFixtures.ipv6Address(0x2606, 0x4700, 0x4700, 0, 0, 0, 0, 0x1111),
            srcPort = 41000,
            dstPort = 53,
            payload = PacketFixtures.dnsQuery("example.org", qtype = 28)
        )

        val parsed = TunPacketParser().parse(packet, packet.size, 1_700_000_000_002L)

        assertNotNull(parsed)
        assertEquals(6, parsed?.ipVersion)
        assertEquals(true, parsed?.outbound)
        assertEquals("AAAA", parsed?.protocolEvidence?.dnsQueryType)
    }

    @Test
    fun `parses ipv4 ICMP metadata without transport ports`() {
        val packet = PacketFixtures.ipv4IcmpPacket(
            src = byteArrayOf(10, 0, 0, 2),
            dst = byteArrayOf(1, 1, 1, 1)
        )

        val parsed = TunPacketParser().parse(packet, packet.size, 1_700_000_000_003L)

        assertNotNull(parsed)
        assertEquals(1, parsed?.protocolCode)
        assertEquals(0, parsed?.srcPort)
        assertEquals(0, parsed?.dstPort)
    }

    @Test
    fun `parses ICMPv6 after hop by hop extension`() {
        val packet = PacketFixtures.ipv6IcmpWithHopByHopPacket(
            src = PacketFixtures.ipv6Address(0xfd00, 0, 0, 0, 0, 0, 0, 2),
            dst = PacketFixtures.ipv6Address(0x2606, 0x4700, 0x4700, 0, 0, 0, 0, 0x1111)
        )

        val parsed = TunPacketParser().parse(packet, packet.size, 1_700_000_000_004L)

        assertNotNull(parsed)
        assertEquals(6, parsed?.ipVersion)
        assertEquals(58, parsed?.protocolCode)
        assertEquals(0, parsed?.payloadBytes)
    }
}
