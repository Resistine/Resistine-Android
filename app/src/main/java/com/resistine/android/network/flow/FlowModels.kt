package com.resistine.android.network.flow

enum class FlowProtocol(val code: Int) {
    TCP(6),
    UDP(17),
    ICMP(1),
    ICMPV6(58),
    UNKNOWN(0);

    companion object {
        fun fromCode(code: Int): FlowProtocol = when (code) {
            6 -> TCP
            17 -> UDP
            1 -> ICMP
            58 -> ICMPV6
            else -> UNKNOWN
        }
    }
}

enum class FlowNetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    UNKNOWN
}

enum class PacketDirection(val outbound: Boolean) {
    OUTBOUND(true),
    INBOUND(false)
}

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

    fun preferredHost(): String? = tlsSni ?: httpHost ?: dnsQueryName
}

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
    val durationMillis: Long
        get() = (timestampEndMillis - timestampStartMillis).coerceAtLeast(0L)
}
