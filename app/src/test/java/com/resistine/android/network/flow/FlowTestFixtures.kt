package com.resistine.android.network.flow

fun flowRecord(
    id: String = "flow-1",
    dstIp: String = "203.0.113.10",
    appPackage: String? = "com.example.app",
    evidence: ProtocolEvidence = ProtocolEvidence(tlsSni = "api.example.com", tlsAlpn = "h2")
): FlowRecord {
    return FlowRecord(
        id = id,
        timestampStartMillis = 1_700_000_000_000L,
        timestampEndMillis = 1_700_000_001_234L,
        ipVersion = 4,
        protocol = FlowProtocol.TCP,
        srcIp = "10.0.0.2",
        srcPort = 51234,
        dstIp = dstIp,
        dstPort = 443,
        bytesOut = 512,
        bytesIn = 4096,
        packetsOut = 4,
        packetsIn = 8,
        networkType = FlowNetworkType.WIFI,
        appUid = 10123,
        appPackage = appPackage,
        vpnActive = true,
        protocolEvidence = evidence
    )
}

object PacketFixtures {
    fun ipv4TcpPacket(
        src: ByteArray,
        dst: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val packet = ByteArray(20 + 20 + payload.size)
        packet[0] = 0x45
        packet[2] = ((packet.size shr 8) and 0xFF).toByte()
        packet[3] = (packet.size and 0xFF).toByte()
        packet[8] = 64
        packet[9] = 6
        src.copyInto(packet, destinationOffset = 12)
        dst.copyInto(packet, destinationOffset = 16)
        writePort(packet, 20, srcPort)
        writePort(packet, 22, dstPort)
        packet[32] = 0x50
        packet[33] = 0x18
        packet[34] = 0x10
        packet[35] = 0x00
        payload.copyInto(packet, destinationOffset = 40)
        return packet
    }

    fun ipv4UdpPacket(
        src: ByteArray,
        dst: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val packet = ByteArray(20 + 8 + payload.size)
        packet[0] = 0x45
        packet[2] = ((packet.size shr 8) and 0xFF).toByte()
        packet[3] = (packet.size and 0xFF).toByte()
        packet[8] = 64
        packet[9] = 17
        src.copyInto(packet, destinationOffset = 12)
        dst.copyInto(packet, destinationOffset = 16)
        writePort(packet, 20, srcPort)
        writePort(packet, 22, dstPort)
        val udpLength = 8 + payload.size
        packet[24] = ((udpLength shr 8) and 0xFF).toByte()
        packet[25] = (udpLength and 0xFF).toByte()
        payload.copyInto(packet, destinationOffset = 28)
        return packet
    }

    fun ipv6UdpPacket(
        src: ByteArray,
        dst: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val packet = ByteArray(40 + 8 + payload.size)
        packet[0] = 0x60
        val payloadLength = 8 + payload.size
        packet[4] = ((payloadLength shr 8) and 0xFF).toByte()
        packet[5] = (payloadLength and 0xFF).toByte()
        packet[6] = 17
        packet[7] = 64
        src.copyInto(packet, destinationOffset = 8)
        dst.copyInto(packet, destinationOffset = 24)
        writePort(packet, 40, srcPort)
        writePort(packet, 42, dstPort)
        packet[44] = ((payloadLength shr 8) and 0xFF).toByte()
        packet[45] = (payloadLength and 0xFF).toByte()
        payload.copyInto(packet, destinationOffset = 48)
        return packet
    }

    fun ipv4IcmpPacket(src: ByteArray, dst: ByteArray, type: Int = 8, code: Int = 0): ByteArray {
        val packet = ByteArray(28)
        packet[0] = 0x45
        packet[2] = 0
        packet[3] = packet.size.toByte()
        packet[8] = 64
        packet[9] = 1
        src.copyInto(packet, destinationOffset = 12)
        dst.copyInto(packet, destinationOffset = 16)
        packet[20] = type.toByte()
        packet[21] = code.toByte()
        return packet
    }

    fun ipv6IcmpWithHopByHopPacket(src: ByteArray, dst: ByteArray): ByteArray {
        val packet = ByteArray(40 + 8 + 8)
        packet[0] = 0x60
        packet[4] = 0
        packet[5] = 16
        packet[6] = 0
        packet[7] = 64
        src.copyInto(packet, destinationOffset = 8)
        dst.copyInto(packet, destinationOffset = 24)
        packet[40] = 58
        packet[41] = 0
        packet[48] = 128.toByte()
        return packet
    }

    fun dnsQuery(host: String, qtype: Int = 1): ByteArray {
        val labels = host.split(".")
        val questionSize = labels.sumOf { 1 + it.length } + 1 + 4
        val packet = ByteArray(12 + questionSize)
        packet[0] = 0x12
        packet[1] = 0x34
        packet[2] = 0x01
        packet[5] = 0x01
        var offset = 12
        labels.forEach { label ->
            packet[offset++] = label.length.toByte()
            label.toByteArray(Charsets.US_ASCII).copyInto(packet, destinationOffset = offset)
            offset += label.length
        }
        packet[offset++] = 0
        packet[offset++] = ((qtype shr 8) and 0xFF).toByte()
        packet[offset++] = (qtype and 0xFF).toByte()
        packet[offset++] = 0
        packet[offset] = 1
        return packet
    }

    fun ipv6Address(vararg groups: Int): ByteArray {
        require(groups.size == 8)
        val bytes = ByteArray(16)
        groups.forEachIndexed { index, group ->
            bytes[index * 2] = ((group shr 8) and 0xFF).toByte()
            bytes[index * 2 + 1] = (group and 0xFF).toByte()
        }
        return bytes
    }

    private fun writePort(packet: ByteArray, offset: Int, port: Int) {
        packet[offset] = ((port shr 8) and 0xFF).toByte()
        packet[offset + 1] = (port and 0xFF).toByte()
    }
}
