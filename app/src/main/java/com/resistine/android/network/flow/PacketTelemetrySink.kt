package com.resistine.android.network.flow

/**
 * Enumeration of packet telemetry ingestion results.
 */
enum class PacketTelemetryResult {
    ACCEPTED,
    PARSE_REJECTED,
    PIPELINE_CLOSED
}

/**
 * Receives plaintext IP packets at a VPN capture boundary.
 *
 * A WireGuard integration must call this before outbound encryption and after inbound decryption.
 * Buffers may be reused by the caller after this method returns.
 */
fun interface PacketTelemetrySink {
    /**
     * Ingests a packet buffer.
     *
     * @param packet Packet byte array.
     * @param length Length of the packet in bytes.
     * @param direction [PacketDirection] (outbound or inbound).
     * @param timestampMillis Timestamp in milliseconds.
     * @return [PacketTelemetryResult].
     */
    fun ingest(
        packet: ByteArray,
        length: Int,
        direction: PacketDirection,
        timestampMillis: Long
    ): PacketTelemetryResult
}
