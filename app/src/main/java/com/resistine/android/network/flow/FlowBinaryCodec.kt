package com.resistine.android.network.flow

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Binary codec for efficiently encoding and decoding flow segments and records.
 */
object FlowBinaryCodec {
    /** Magic number identifying Resistine flow segment files ('RSFL'). */
    const val FILE_MAGIC: Int = 0x5253464C

    /** File format version. */
    const val FILE_VERSION: Short = 1

    /** Schema version. */
    const val SCHEMA_VERSION: Short = 1

    /** Record type code for flow records. */
    const val RECORD_TYPE_FLOW: Byte = 1

    /** Record version. */
    const val RECORD_VERSION: Byte = 1

    /** Number of bytes in a flow segment file header. */
    const val FILE_HEADER_BYTES: Int = 16

    /**
     * Writes the binary file header to the output stream.
     *
     * @param output [DataOutputStream] target.
     * @param createdAtMillis Creation timestamp in milliseconds.
     */
    fun writeFileHeader(output: DataOutputStream, createdAtMillis: Long = System.currentTimeMillis()) {
        output.writeInt(FILE_MAGIC)
        output.writeShort(FILE_VERSION.toInt())
        output.writeShort(SCHEMA_VERSION.toInt())
        output.writeLong(createdAtMillis)
    }

    /**
     * Encodes a [FlowRecord] into a binary byte array.
     *
     * @param record The [FlowRecord] to encode.
     * @return Encoded byte array.
     */
    fun encodeRecord(record: FlowRecord): ByteArray {
        val payloadBytes = ByteArrayOutputStream()
        DataOutputStream(payloadBytes).use { out ->
            out.writeUtfString(record.id)
            out.writeLong(record.timestampStartMillis)
            out.writeLong(record.timestampEndMillis)
            out.writeInt(record.ipVersion)
            out.writeInt(record.protocol.code)
            out.writeUtfString(record.srcIp)
            out.writeInt(record.srcPort)
            out.writeUtfString(record.dstIp)
            out.writeInt(record.dstPort)
            out.writeLong(record.bytesOut)
            out.writeLong(record.bytesIn)
            out.writeInt(record.packetsOut)
            out.writeInt(record.packetsIn)
            out.writeUtfString(record.networkType.name)
            out.writeInt(record.appUid ?: -1)
            out.writeNullableUtfString(record.appPackage)
            out.writeBoolean(record.vpnActive)
            out.writeNullableUtfString(record.protocolEvidence.dnsQueryName)
            out.writeNullableUtfString(record.protocolEvidence.dnsQueryType)
            out.writeInt(record.protocolEvidence.dnsResponseCode ?: -1)
            out.writeNullableUtfString(record.protocolEvidence.dnsAnswerValue)
            out.writeNullableUtfString(record.protocolEvidence.tlsSni)
            out.writeNullableUtfString(record.protocolEvidence.tlsAlpn)
            out.writeNullableUtfString(record.protocolEvidence.httpHost)
            out.writeNullableUtfString(record.protocolEvidence.httpMethod)
        }

        val payload = payloadBytes.toByteArray()
        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { out ->
                out.writeByte(RECORD_TYPE_FLOW.toInt())
                out.writeByte(RECORD_VERSION.toInt())
                out.writeInt(payload.size)
                out.write(payload)
            }
            bytes.toByteArray()
        }
    }

    /**
     * Decodes a binary payload into a [FlowRecord].
     *
     * @param bytes Binary payload byte array.
     * @return Decoded [FlowRecord].
     */
    fun decodeRecord(bytes: ByteArray): FlowRecord {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val id = input.readUtfString()
            val start = input.readLong()
            val end = input.readLong()
            val ipVersion = input.readInt()
            val protocol = FlowProtocol.fromCode(input.readInt())
            val srcIp = input.readUtfString()
            val srcPort = input.readInt()
            val dstIp = input.readUtfString()
            val dstPort = input.readInt()
            val bytesOut = input.readLong()
            val bytesIn = input.readLong()
            val packetsOut = input.readInt()
            val packetsIn = input.readInt()
            val networkType = runCatching { FlowNetworkType.valueOf(input.readUtfString()) }
                .getOrDefault(FlowNetworkType.UNKNOWN)
            val uid = input.readInt().takeIf { it >= 0 }
            val packageName = input.readNullableUtfString()
            val vpnActive = input.readBoolean()
            val evidence = ProtocolEvidence(
                dnsQueryName = input.readNullableUtfString(),
                dnsQueryType = input.readNullableUtfString(),
                dnsResponseCode = input.readInt().takeIf { it >= 0 },
                dnsAnswerValue = input.readNullableUtfString(),
                tlsSni = input.readNullableUtfString(),
                tlsAlpn = input.readNullableUtfString(),
                httpHost = input.readNullableUtfString(),
                httpMethod = input.readNullableUtfString()
            )
            return FlowRecord(
                id = id,
                timestampStartMillis = start,
                timestampEndMillis = end,
                ipVersion = ipVersion,
                protocol = protocol,
                srcIp = srcIp,
                srcPort = srcPort,
                dstIp = dstIp,
                dstPort = dstPort,
                bytesOut = bytesOut,
                bytesIn = bytesIn,
                packetsOut = packetsOut,
                packetsIn = packetsIn,
                networkType = networkType,
                appUid = uid,
                appPackage = packageName,
                vpnActive = vpnActive,
                protocolEvidence = evidence
            )
        }
    }

    /**
     * Writes a UTF-8 string to the data output stream.
     */
    private fun DataOutputStream.writeUtfString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeInt(bytes.size)
        write(bytes)
    }

    /**
     * Writes a nullable UTF-8 string to the data output stream.
     */
    private fun DataOutputStream.writeNullableUtfString(value: String?) {
        if (value == null) {
            writeInt(-1)
        } else {
            writeUtfString(value)
        }
    }

    /**
     * Reads a UTF-8 string from the data input stream.
     */
    private fun DataInputStream.readUtfString(): String {
        val length = readInt()
        require(length >= 0) { "Negative string length for non-null value" }
        val bytes = ByteArray(length)
        readFully(bytes)
        return bytes.toString(Charsets.UTF_8)
    }

    /**
     * Reads a nullable UTF-8 string from the data input stream.
     */
    private fun DataInputStream.readNullableUtfString(): String? {
        val length = readInt()
        if (length < 0) return null
        val bytes = ByteArray(length)
        readFully(bytes)
        return bytes.toString(Charsets.UTF_8)
    }
}
