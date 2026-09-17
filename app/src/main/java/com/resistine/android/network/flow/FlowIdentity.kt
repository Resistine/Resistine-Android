package com.resistine.android.network.flow

/**
 * Represents an IP endpoint with IP address and port.
 *
 * @property ip IP address string.
 * @property port Port number.
 */
data class FlowEndpointIdentity(
    val ip: String,
    val port: Int
) {
    /**
     * Returns a sortable string representation of the endpoint.
     *
     * @return Formatted "ip:port" string.
     */
    fun sortKey(): String = "$ip:$port"
}

/**
 * Represents a bi-directional transport flow identity between two endpoints.
 *
 * @property ipVersion IP version (4 or 6).
 * @property protocolCode Transport protocol code.
 * @property endpointA First normalized endpoint.
 * @property endpointB Second normalized endpoint.
 */
data class TransportFlowIdentity(
    val ipVersion: Int,
    val protocolCode: Int,
    val endpointA: FlowEndpointIdentity,
    val endpointB: FlowEndpointIdentity
) {
    /**
     * Returns a shard key string for the flow identity.
     *
     * @return Shard key string.
     */
    fun shardKey(): String = "${endpointA.sortKey()}|${endpointB.sortKey()}|$protocolCode|$ipVersion"
}

/**
 * Utility for constructing canonical [TransportFlowIdentity] instances regardless of packet direction.
 */
object FlowIdentity {

    /**
     * Creates a canonical [TransportFlowIdentity] by sorting endpoints to ensure consistent identification for both directions.
     *
     * @param ipVersion IP version (4 or 6).
     * @param protocolCode Protocol code.
     * @param srcIp Source IP address.
     * @param srcPort Source port.
     * @param dstIp Destination IP address.
     * @param dstPort Destination port.
     * @return Canonical [TransportFlowIdentity].
     */
    fun canonical(
        ipVersion: Int,
        protocolCode: Int,
        srcIp: String,
        srcPort: Int,
        dstIp: String,
        dstPort: Int
    ): TransportFlowIdentity {
        val first = FlowEndpointIdentity(srcIp, srcPort)
        val second = FlowEndpointIdentity(dstIp, dstPort)
        return if (first.sortKey() <= second.sortKey()) {
            TransportFlowIdentity(ipVersion, protocolCode, first, second)
        } else {
            TransportFlowIdentity(ipVersion, protocolCode, second, first)
        }
    }
}
