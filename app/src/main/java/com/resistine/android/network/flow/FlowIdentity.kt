package com.resistine.android.network.flow

data class FlowEndpointIdentity(
    val ip: String,
    val port: Int
) {
    fun sortKey(): String = "$ip:$port"
}

data class TransportFlowIdentity(
    val ipVersion: Int,
    val protocolCode: Int,
    val endpointA: FlowEndpointIdentity,
    val endpointB: FlowEndpointIdentity
) {
    fun shardKey(): String = "${endpointA.sortKey()}|${endpointB.sortKey()}|$protocolCode|$ipVersion"
}

object FlowIdentity {
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

