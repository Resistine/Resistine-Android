package com.resistine.android.network.flow

/**
 * Enumeration of supported network transport protocols with their protocol numbers.
 *
 * @property code Protocol number.
 */
enum class FlowProtocol(val code: Int) {
    TCP(6),
    UDP(17),
    ICMP(1),
    ICMPV6(58),
    UNKNOWN(0);

    companion object {
        /**
         * Resolves a [FlowProtocol] from its protocol number code.
         *
         * @param code Protocol number.
         * @return Matching [FlowProtocol] or [UNKNOWN].
         */
        fun fromCode(code: Int): FlowProtocol = when (code) {
            6 -> TCP
            17 -> UDP
            1 -> ICMP
            58 -> ICMPV6
            else -> UNKNOWN
        }
    }
}

/**
 * Enumeration of network transport types.
 */
enum class FlowNetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    UNKNOWN
}

/**
 * Enumeration of packet directions.
 *
 * @property outbound True if outbound; false if inbound.
 */
enum class PacketDirection(val outbound: Boolean) {
    OUTBOUND(true),
    INBOUND(false)
}

/**
 * Protocol inspection evidence extracted from packet payloads (DNS, TLS SNI, HTTP host, etc.).
 *
 * @property dnsQueryName Extracted DNS query domain name.
 * @property dnsQueryType DNS query type (e.g. A, AAAA).
 * @property dnsResponseCode DNS response return code.
 * @property dnsAnswerValue Resolved DNS answer value.
 * @property tlsSni TLS Server Name Indication (SNI).
 * @property tlsAlpn TLS ALPN protocol.
 * @property httpHost HTTP Host header value.
 * @property httpMethod HTTP request method.
 */
data class ProtocolEvidence(
    val dnsQueryName: String? = null,
    val dnsQueryType: String? = null,
    val dnsResponseCode: Int? = null,
    val dnsAnswerValue: String? = null,
    val tlsSni: String? = null,
    val tlsAlpn: String? = null,
    val httpHost: String? = null,
    val httpMethod: String? = null
) {
    /**
     * Merges another [ProtocolEvidence] into this one, preferring non-null values.
     *
     * @param other The other [ProtocolEvidence].
     * @return Merged [ProtocolEvidence].
     */
    fun merge(other: ProtocolEvidence): ProtocolEvidence {
        return ProtocolEvidence(
            dnsQueryName = dnsQueryName ?: other.dnsQueryName,
            dnsQueryType = dnsQueryType ?: other.dnsQueryType,
            dnsResponseCode = dnsResponseCode ?: other.dnsResponseCode,
            dnsAnswerValue = dnsAnswerValue ?: other.dnsAnswerValue,
            tlsSni = tlsSni ?: other.tlsSni,
            tlsAlpn = tlsAlpn ?: other.tlsAlpn,
            httpHost = httpHost ?: other.httpHost,
            httpMethod = httpMethod ?: other.httpMethod
        )
    }

    /**
     * Returns the preferred hostname from TLS SNI, HTTP host, or DNS query name.
     *
     * @return Hostname string or null.
     */
    fun preferredHost(): String? = tlsSni ?: httpHost ?: dnsQueryName
}

/**
 * Metadata parsed from an individual IP packet.
 *
 * @property timestampMillis Timestamp in milliseconds when captured.
 * @property ipVersion IP version (4 or 6).
 * @property protocolCode Protocol number.
 * @property srcIp Source IP address.
 * @property srcPort Source port.
 * @property dstIp Destination IP address.
 * @property dstPort Destination port.
 * @property bytes Total packet length in bytes.
 * @property outbound Whether the packet is outbound.
 * @property payloadBytes Payload length in bytes.
 * @property protocolEvidence Extracted protocol inspection evidence.
 */
data class PacketMetadata(
    val timestampMillis: Long,
    val ipVersion: Int,
    val protocolCode: Int,
    val srcIp: String,
    val srcPort: Int,
    val dstIp: String,
    val dstPort: Int,
    val bytes: Int,
    val outbound: Boolean,
    val payloadBytes: Int = 0,
    val protocolEvidence: ProtocolEvidence = ProtocolEvidence()
)

/**
 * Aggregated record representing a complete network connection flow.
 *
 * @property id Unique flow record UUID.
 * @property timestampStartMillis Flow start epoch timestamp in milliseconds.
 * @property timestampEndMillis Flow end epoch timestamp in milliseconds.
 * @property ipVersion IP version.
 * @property protocol Transport protocol [FlowProtocol].
 * @property srcIp Source IP address.
 * @property srcPort Source port.
 * @property dstIp Destination IP address.
 * @property dstPort Destination port.
 * @property bytesOut Total outbound bytes transferred.
 * @property bytesIn Total inbound bytes transferred.
 * @property packetsOut Total outbound packets.
 * @property packetsIn Total inbound packets.
 * @property networkType Active [FlowNetworkType].
 * @property appUid Application UID associated with the flow.
 * @property appPackage Application package name.
 * @property vpnActive Whether VPN was active.
 * @property protocolEvidence Associated [ProtocolEvidence].
 */
data class FlowRecord(
    val id: String,
    val timestampStartMillis: Long,
    val timestampEndMillis: Long,
    val ipVersion: Int,
    val protocol: FlowProtocol,
    val srcIp: String,
    val srcPort: Int,
    val dstIp: String,
    val dstPort: Int,
    val bytesOut: Long,
    val bytesIn: Long,
    val packetsOut: Int,
    val packetsIn: Int,
    val networkType: FlowNetworkType = FlowNetworkType.UNKNOWN,
    val appUid: Int? = null,
    val appPackage: String? = null,
    val vpnActive: Boolean = true,
    val protocolEvidence: ProtocolEvidence = ProtocolEvidence()
) {
    /** Total duration of the flow in milliseconds. */
    val durationMillis: Long
        get() = (timestampEndMillis - timestampStartMillis).coerceAtLeast(0L)
}
