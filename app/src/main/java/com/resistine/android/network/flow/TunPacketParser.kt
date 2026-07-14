package com.resistine.android.network.flow

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class TunPacketParser(
    private val localIpv4Prefixes: List<String> = listOf("10.", "172.16.", "172.17.", "172.18.", "172.19.", "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.", "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.", "192.168."),
    private val localIpv6Prefixes: List<String> = listOf("fd", "fe80")
) {
    fun parse(
        packet: ByteArray,
        length: Int,
        timestampMillis: Long,
        direction: PacketDirection? = null
    ): PacketMetadata? {
        val packetLength = length.coerceAtMost(packet.size)
        if (packetLength < 20 || packet.isEmpty()) return null
        val version = (packet[0].toInt() ushr 4) and 0x0F
        return when (version) {
            4 -> parseIpv4(packet, packetLength, timestampMillis, direction)
            6 -> parseIpv6(packet, packetLength, timestampMillis, direction)
            else -> null
        }
    }

    private fun parseIpv4(
        packet: ByteArray,
        length: Int,
        timestampMillis: Long,
        direction: PacketDirection?
    ): PacketMetadata? {
        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (ihl < 20 || length < ihl + 4) return null
        val protocol = packet[9].toInt() and 0xFF
        val srcIp = ipv4String(packet, 12)
        val dstIp = ipv4String(packet, 16)
        val (srcPort, dstPort) = parsePorts(packet, ihl, protocol, length)
        if (srcPort < 0 || dstPort < 0) return null
        val payloadOffset = payloadOffset(packet, ihl, protocol, length)
        val outbound = direction?.outbound ?: localIpv4Prefixes.any { srcIp.startsWith(it) }
        val evidence = inspectProtocolEvidence(packet, payloadOffset, protocol, srcPort, dstPort, length, outbound)
        return PacketMetadata(
            timestampMillis = timestampMillis,
            ipVersion = 4,
            protocolCode = protocol,
            srcIp = srcIp,
            srcPort = srcPort,
            dstIp = dstIp,
            dstPort = dstPort,
            bytes = length,
            outbound = outbound,
            payloadBytes = payloadOffset?.let { (length - it).coerceAtLeast(0) } ?: 0,
            protocolEvidence = evidence
        )
    }

    private fun parseIpv6(
        packet: ByteArray,
        length: Int,
        timestampMillis: Long,
        direction: PacketDirection?
    ): PacketMetadata? {
        if (length < 40) return null
        val transport = resolveIpv6Transport(packet, length) ?: return null
        val protocol = transport.protocol
        val srcIp = ipv6String(packet, 8)
        val dstIp = ipv6String(packet, 24)
        val (srcPort, dstPort) = parsePorts(packet, transport.offset, protocol, length)
        if (srcPort < 0 || dstPort < 0) return null
        val payloadOffset = payloadOffset(packet, transport.offset, protocol, length)
        val outbound = direction?.outbound ?: localIpv6Prefixes.any { srcIp.startsWith(it) }
        val evidence = inspectProtocolEvidence(packet, payloadOffset, protocol, srcPort, dstPort, length, outbound)
        return PacketMetadata(
            timestampMillis = timestampMillis,
            ipVersion = 6,
            protocolCode = protocol,
            srcIp = srcIp,
            srcPort = srcPort,
            dstIp = dstIp,
            dstPort = dstPort,
            bytes = length,
            outbound = outbound,
            payloadBytes = payloadOffset?.let { (length - it).coerceAtLeast(0) } ?: 0,
            protocolEvidence = evidence
        )
    }

    private fun parsePorts(packet: ByteArray, offset: Int, protocol: Int, length: Int): Pair<Int, Int> {
        if (offset + 4 > length) return -1 to -1
        return when (protocol) {
            6, 17 -> {
                val srcPort = ((packet[offset].toInt() and 0xFF) shl 8) or (packet[offset + 1].toInt() and 0xFF)
                val dstPort = ((packet[offset + 2].toInt() and 0xFF) shl 8) or (packet[offset + 3].toInt() and 0xFF)
                srcPort to dstPort
            }
            1, 58 -> 0 to 0
            else -> -1 to -1
        }
    }

    private fun payloadOffset(packet: ByteArray, transportOffset: Int, protocol: Int, length: Int): Int? {
        if (transportOffset >= length) return null
        return when (protocol) {
            6 -> {
                if (transportOffset + 13 >= length) return null
                val tcpHeaderLength = ((packet[transportOffset + 12].toInt() ushr 4) and 0x0F) * 4
                val offset = transportOffset + tcpHeaderLength
                offset.takeIf { tcpHeaderLength >= 20 && it <= length }
            }
            17 -> (transportOffset + 8).takeIf { it <= length }
            1, 58 -> (transportOffset + 8).takeIf { it <= length }
            else -> null
        }
    }

    private fun resolveIpv6Transport(packet: ByteArray, length: Int): Ipv6Transport? {
        var nextHeader = packet[6].toInt() and 0xFF
        var offset = 40
        repeat(MAX_IPV6_EXTENSION_HEADERS) {
            when (nextHeader) {
                0, 43, 60 -> {
                    if (offset + 2 > length) return null
                    nextHeader = packet[offset].toInt() and 0xFF
                    val extensionLength = ((packet[offset + 1].toInt() and 0xFF) + 1) * 8
                    if (extensionLength < 8 || offset + extensionLength > length) return null
                    offset += extensionLength
                }
                44 -> {
                    if (offset + 8 > length) return null
                    val fragmentField = readUInt16(packet, offset + 2) ?: return null
                    if ((fragmentField and 0xFFF8) != 0) return null
                    nextHeader = packet[offset].toInt() and 0xFF
                    offset += 8
                }
                51 -> {
                    if (offset + 2 > length) return null
                    nextHeader = packet[offset].toInt() and 0xFF
                    val extensionLength = ((packet[offset + 1].toInt() and 0xFF) + 2) * 4
                    if (extensionLength < 8 || offset + extensionLength > length) return null
                    offset += extensionLength
                }
                50, 59 -> return null
                else -> return Ipv6Transport(nextHeader, offset)
            }
        }
        return null
    }

    private fun inspectProtocolEvidence(
        packet: ByteArray,
        payloadOffset: Int?,
        protocol: Int,
        srcPort: Int,
        dstPort: Int,
        length: Int,
        outbound: Boolean
    ): ProtocolEvidence {
        val offset = payloadOffset ?: return ProtocolEvidence()
        val payloadLength = length - offset
        if (payloadLength <= 0) return ProtocolEvidence()
        val slice = packet.copyOfRange(offset, min(length, offset + min(payloadLength, 4096)))

        var evidence = ProtocolEvidence()
        if (protocol == 17 && (srcPort == 53 || dstPort == 53)) {
            evidence = evidence.merge(parseDns(slice))
        }
        if (protocol == 6 && (srcPort == 53 || dstPort == 53)) {
            evidence = evidence.merge(parseTcpDns(slice))
        }
        if (protocol == 6 && outbound) {
            evidence = evidence.merge(parseHttpRequest(slice))
            evidence = evidence.merge(parseTlsClientHello(slice))
        }
        return evidence
    }

    private fun parseDns(payload: ByteArray): ProtocolEvidence {
        if (payload.size < 12) return ProtocolEvidence()
        val flags = readUInt16(payload, 2) ?: return ProtocolEvidence()
        val qdCount = readUInt16(payload, 4) ?: return ProtocolEvidence()
        val isResponse = (flags and 0x8000) != 0
        val rcode = flags and 0x0F
        if (qdCount <= 0) {
            return ProtocolEvidence(dnsResponseCode = if (isResponse) rcode else null)
        }
        val (name, offset) = parseDnsName(payload, 12) ?: return ProtocolEvidence(dnsResponseCode = if (isResponse) rcode else null)
        val qtype = if (offset + 2 <= payload.size) readUInt16(payload, offset) else null
        return ProtocolEvidence(
            dnsQueryName = name,
            dnsQueryType = dnsTypeName(qtype),
            dnsResponseCode = if (isResponse) rcode else null
        )
    }

    private fun parseTcpDns(payload: ByteArray): ProtocolEvidence {
        val messageLength = readUInt16(payload, 0) ?: return ProtocolEvidence()
        if (messageLength <= 0 || messageLength + 2 > payload.size) return ProtocolEvidence()
        return parseDns(payload.copyOfRange(2, messageLength + 2))
    }

    private fun parseHttpRequest(payload: ByteArray): ProtocolEvidence {
        val prefix = payload.copyOfRange(0, min(payload.size, 16)).toString(Charsets.ISO_8859_1)
        val method = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS").firstOrNull {
            prefix.startsWith("$it ")
        } ?: return ProtocolEvidence()
        val text = payload.copyOfRange(0, min(payload.size, 2048)).toString(Charsets.ISO_8859_1)
        val host = text.lineSequence()
            .firstOrNull { it.startsWith("Host:", ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        return ProtocolEvidence(httpMethod = method, httpHost = host)
    }

    private fun parseTlsClientHello(payload: ByteArray): ProtocolEvidence {
        if (payload.size < 5 || payload[0].toInt() != 0x16) return ProtocolEvidence()
        val recordLength = readUInt16(payload, 3) ?: return ProtocolEvidence()
        if (recordLength + 5 > payload.size || payload.size < 43) return ProtocolEvidence()
        if (payload[5].toInt() != 0x01) return ProtocolEvidence()
        var offset = 5 + 4 + 2 + 32
        if (offset >= payload.size) return ProtocolEvidence()
        val sessionIdLength = payload[offset].toInt() and 0xFF
        offset += 1 + sessionIdLength
        if (offset + 2 > payload.size) return ProtocolEvidence()
        val cipherLength = readUInt16(payload, offset) ?: return ProtocolEvidence()
        offset += 2 + cipherLength
        if (offset >= payload.size) return ProtocolEvidence()
        val compressionLength = payload[offset].toInt() and 0xFF
        offset += 1 + compressionLength
        if (offset + 2 > payload.size) return ProtocolEvidence()
        val extensionsEnd = offset + 2 + (readUInt16(payload, offset) ?: return ProtocolEvidence())
        offset += 2

        var sni: String? = null
        var alpn: String? = null
        while (offset + 4 <= extensionsEnd && offset + 4 <= payload.size) {
            val type = readUInt16(payload, offset) ?: break
            val len = readUInt16(payload, offset + 2) ?: break
            val dataStart = offset + 4
            val dataEnd = dataStart + len
            if (dataEnd > payload.size) break
            when (type) {
                0 -> sni = parseSniExtension(payload, dataStart, dataEnd)
                16 -> alpn = parseAlpnExtension(payload, dataStart, dataEnd)
            }
            offset = dataEnd
        }
        return ProtocolEvidence(tlsSni = sni, tlsAlpn = alpn)
    }

    private fun parseSniExtension(payload: ByteArray, start: Int, end: Int): String? {
        if (start + 5 > end) return null
        var offset = start + 2
        val nameType = payload[offset].toInt() and 0xFF
        offset += 1
        if (nameType != 0 || offset + 2 > end) return null
        val nameLength = readUInt16(payload, offset) ?: return null
        offset += 2
        if (offset + nameLength > end) return null
        return payload.copyOfRange(offset, offset + nameLength).toString(Charsets.US_ASCII)
    }

    private fun parseAlpnExtension(payload: ByteArray, start: Int, end: Int): String? {
        if (start + 3 > end) return null
        var offset = start + 2
        val values = mutableListOf<String>()
        while (offset < end) {
            val len = payload[offset].toInt() and 0xFF
            offset += 1
            if (offset + len > end) break
            values += payload.copyOfRange(offset, offset + len).toString(Charsets.US_ASCII)
            offset += len
        }
        return values.takeIf { it.isNotEmpty() }?.joinToString(",")
    }

    private fun parseDnsName(buffer: ByteArray, offsetStart: Int): Pair<String, Int>? {
        var offset = offsetStart
        val labels = mutableListOf<String>()
        while (offset < buffer.size) {
            val length = buffer[offset].toInt() and 0xFF
            if (length == 0) return labels.joinToString(".") to (offset + 1)
            if ((length and 0xC0) != 0 || offset + 1 + length > buffer.size) return null
            labels += buffer.copyOfRange(offset + 1, offset + 1 + length).toString(Charsets.US_ASCII)
            offset += 1 + length
        }
        return null
    }

    private fun readUInt16(buffer: ByteArray, offset: Int): Int? {
        if (offset + 2 > buffer.size) return null
        return ((buffer[offset].toInt() and 0xFF) shl 8) or (buffer[offset + 1].toInt() and 0xFF)
    }

    private fun dnsTypeName(type: Int?): String? = when (type) {
        1 -> "A"
        28 -> "AAAA"
        5 -> "CNAME"
        15 -> "MX"
        16 -> "TXT"
        65 -> "HTTPS"
        else -> type?.toString()
    }

    private fun ipv4String(packet: ByteArray, offset: Int): String {
        return "${packet[offset].toInt() and 0xFF}.${packet[offset + 1].toInt() and 0xFF}.${packet[offset + 2].toInt() and 0xFF}.${packet[offset + 3].toInt() and 0xFF}"
    }

    private fun ipv6String(packet: ByteArray, offset: Int): String {
        val buffer = ByteBuffer.wrap(packet, offset, 16).order(ByteOrder.BIG_ENDIAN)
        return (0 until 8).joinToString(":") { Integer.toHexString(buffer.short.toInt() and 0xFFFF) }
    }

    private data class Ipv6Transport(
        val protocol: Int,
        val offset: Int
    )

    companion object {
        private const val MAX_IPV6_EXTENSION_HEADERS = 8
    }
}
