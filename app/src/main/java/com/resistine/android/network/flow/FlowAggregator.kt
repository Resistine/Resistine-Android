package com.resistine.android.network.flow

import java.util.UUID

/**
 * Key identifying a unique flow by transport protocol, canonical endpoint identity, network type, and app UID/package.
 */
private data class FlowKey(
    val protocol: FlowProtocol,
    val identity: TransportFlowIdentity,
    val networkType: FlowNetworkType,
    val appUid: Int?,
    val appPackage: String?
)

/**
 * Accumulator tracking metrics and timing for an active network flow.
 */
private data class FlowAccumulator(
    val startMillis: Long,
    val localIp: String,
    val localPort: Int,
    val remoteIp: String,
    val remotePort: Int,
    val networkType: FlowNetworkType,
    val appUid: Int?,
    val appPackage: String?,
    val vpnActive: Boolean,
    var endMillis: Long,
    var bytesOut: Long = 0L,
    var bytesIn: Long = 0L,
    var packetsOut: Int = 0,
    var packetsIn: Int = 0,
    var protocolEvidence: ProtocolEvidence = ProtocolEvidence()
)

/**
 * Context provided when ingesting packets into the flow aggregator.
 *
 * @property networkType Active [FlowNetworkType].
 * @property appUid Application UID associated with the flow.
 * @property appPackage Application package name.
 * @property vpnActive Whether VPN is active.
 */
data class FlowIngestContext(
    val networkType: FlowNetworkType = FlowNetworkType.UNKNOWN,
    val appUid: Int? = null,
    val appPackage: String? = null,
    val vpnActive: Boolean = true
)

/**
 * Result of ingesting a packet into the flow aggregator.
 *
 * @property flushed List of flow records flushed due to capacity or eviction.
 * @property createdNewFlow True if a new active flow was created.
 * @property activeFlowCount Total number of currently active flows.
 */
data class FlowIngestResult(
    val flushed: List<FlowRecord>,
    val createdNewFlow: Boolean,
    val activeFlowCount: Int
)

/**
 * Aggregates individual packet metadata into comprehensive network flow records.
 *
 * @param tcpIdleTimeoutMillis Idle timeout for TCP flows in milliseconds.
 * @param udpIdleTimeoutMillis Idle timeout for UDP flows in milliseconds.
 * @param hardTimeoutMillis Maximum hard lifetime for active flows in milliseconds.
 * @param maxActiveFlows Maximum number of concurrent active flows before LRU/capacity flushing.
 */
class FlowAggregator(
    private val tcpIdleTimeoutMillis: Long = 60_000L,
    private val udpIdleTimeoutMillis: Long = 30_000L,
    private val hardTimeoutMillis: Long = 10 * 60_000L,
    private val maxActiveFlows: Int = 16_384
) {
    private val activeFlows = LinkedHashMap<FlowKey, FlowAccumulator>()

    init {
        require(maxActiveFlows > 0)
    }

    /**
     * Ingests a packet into the aggregator, updating or creating active flow accumulators.
     *
     * @param packet [PacketMetadata] of the parsed packet.
     * @param context [FlowIngestContext] for the packet.
     * @return [FlowIngestResult] detailing flushed records and flow status.
     */
    fun ingest(packet: PacketMetadata, context: FlowIngestContext = FlowIngestContext()): FlowIngestResult {
        val protocol = FlowProtocol.fromCode(packet.protocolCode)
        val key = FlowKey(
            protocol = protocol,
            identity = FlowIdentity.canonical(
                ipVersion = packet.ipVersion,
                protocolCode = packet.protocolCode,
                srcIp = packet.srcIp,
                srcPort = packet.srcPort,
                dstIp = packet.dstIp,
                dstPort = packet.dstPort
            ),
            networkType = context.networkType,
            appUid = context.appUid,
            appPackage = context.appPackage
        )
        val existed = activeFlows.containsKey(key)
        val capacityFlush = if (!existed && activeFlows.size >= maxActiveFlows) {
            flushInternal(listOf(activeFlows.keys.first()), packet.timestampMillis)
        } else {
            emptyList()
        }
        val accumulator = activeFlows[key] ?: FlowAccumulator(
            startMillis = packet.timestampMillis,
            localIp = if (packet.outbound) packet.srcIp else packet.dstIp,
            localPort = if (packet.outbound) packet.srcPort else packet.dstPort,
            remoteIp = if (packet.outbound) packet.dstIp else packet.srcIp,
            remotePort = if (packet.outbound) packet.dstPort else packet.srcPort,
            networkType = context.networkType,
            appUid = context.appUid,
            appPackage = context.appPackage,
            vpnActive = context.vpnActive,
            endMillis = packet.timestampMillis
        ).also { activeFlows[key] = it }

        accumulator.endMillis = packet.timestampMillis
        if (packet.outbound) {
            accumulator.bytesOut += packet.bytes.toLong()
            accumulator.packetsOut += 1
        } else {
            accumulator.bytesIn += packet.bytes.toLong()
            accumulator.packetsIn += 1
        }
        accumulator.protocolEvidence = accumulator.protocolEvidence.merge(packet.protocolEvidence)

        return FlowIngestResult(
            flushed = capacityFlush,
            createdNewFlow = !existed,
            activeFlowCount = activeFlows.size
        )
    }

    /**
     * Returns the number of currently active flows.
     *
     * @return Active flow count.
     */
    fun activeFlowCount(): Int = activeFlows.size

    /**
     * Flushes all active flows into completed [FlowRecord] objects.
     *
     * @param nowMillis Current timestamp in milliseconds.
     * @return List of flushed [FlowRecord] items.
     */
    fun flushAll(nowMillis: Long): List<FlowRecord> = flushInternal(activeFlows.keys.toList(), nowMillis)

    /**
     * Flushes flows that have exceeded their idle or hard timeouts.
     *
     * @param nowMillis Current timestamp in milliseconds.
     * @return List of expired [FlowRecord] items.
     */
    fun flushExpired(nowMillis: Long): List<FlowRecord> {
        val expiredKeys = activeFlows.entries
            .filter { (key, value) ->
                val idleTimeout = if (key.protocol == FlowProtocol.UDP) udpIdleTimeoutMillis else tcpIdleTimeoutMillis
                nowMillis - value.endMillis >= idleTimeout || nowMillis - value.startMillis >= hardTimeoutMillis
            }
            .map { it.key }
        return flushInternal(expiredKeys, nowMillis)
    }

    /**
     * Internal method to remove and convert specified flow keys into finalized [FlowRecord] objects.
     *
     * @param keys List of [FlowKey] to flush.
     * @param nowMillis Current timestamp.
     * @return List of [FlowRecord].
     */
    private fun flushInternal(keys: List<FlowKey>, nowMillis: Long): List<FlowRecord> {
        return keys.mapNotNull { key ->
            val accumulator = activeFlows.remove(key) ?: return@mapNotNull null
            FlowRecord(
                id = UUID.randomUUID().toString(),
                timestampStartMillis = accumulator.startMillis,
                timestampEndMillis = accumulator.endMillis.coerceAtLeast(accumulator.startMillis),
                ipVersion = key.identity.ipVersion,
                protocol = key.protocol,
                srcIp = accumulator.localIp,
                srcPort = accumulator.localPort,
                dstIp = accumulator.remoteIp,
                dstPort = accumulator.remotePort,
                bytesOut = accumulator.bytesOut,
                bytesIn = accumulator.bytesIn,
                packetsOut = accumulator.packetsOut,
                packetsIn = accumulator.packetsIn,
                networkType = accumulator.networkType,
                appUid = accumulator.appUid,
                appPackage = accumulator.appPackage,
                vpnActive = accumulator.vpnActive,
                protocolEvidence = accumulator.protocolEvidence
            )
        }
    }
}
