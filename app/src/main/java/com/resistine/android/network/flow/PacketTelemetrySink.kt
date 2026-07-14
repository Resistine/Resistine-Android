package com.resistine.android.network.flow

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
    fun ingest(
        packet: ByteArray,
        length: Int,
        direction: PacketDirection,
        timestampMillis: Long
    ): PacketTelemetryResult
}
