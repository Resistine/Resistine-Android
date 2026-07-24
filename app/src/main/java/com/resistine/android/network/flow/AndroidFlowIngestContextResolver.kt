package com.resistine.android.network.flow

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.system.OsConstants
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.LinkedHashMap

fun interface FlowIngestContextResolver {
    fun resolve(packet: PacketMetadata): FlowIngestContext
}

class AndroidFlowIngestContextResolver(
    context: Context,
    private val cacheCapacity: Int = DEFAULT_CACHE_CAPACITY,
    private val networkCacheMillis: Long = NETWORK_CACHE_MILLIS
) : FlowIngestContextResolver {
    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)
    private val packageManager = context.applicationContext.packageManager
    private val attributionCache =
        object : LinkedHashMap<String, AppAttribution>(cacheCapacity, 0.75f, true) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<String, AppAttribution>?
            ): Boolean = size > cacheCapacity
        }

    private var cachedNetworkType = FlowNetworkType.UNKNOWN
    private var networkCacheExpiresAt = 0L

    init {
        require(cacheCapacity > 0)
        require(networkCacheMillis >= 0L)
        runCatching { resolveNetworkType() }
    }

    @Synchronized
    override fun resolve(packet: PacketMetadata): FlowIngestContext {
        val cacheKey = packet.cacheKey()
        val attribution = attributionCache[cacheKey] ?: runCatching {
            resolveAttribution(packet)
        }.getOrDefault(AppAttribution()).also {
            if (it.uid != null) attributionCache[cacheKey] = it
        }
        return FlowIngestContext(
            networkType = runCatching { resolveNetworkType() }.getOrDefault(FlowNetworkType.UNKNOWN),
            appUid = attribution.uid,
            appPackage = attribution.packageName,
            vpnActive = true
        )
    }

    private fun resolveAttribution(packet: PacketMetadata): AppAttribution {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return AppAttribution()
        val protocol = when (packet.protocolCode) {
            FlowProtocol.TCP.code -> OsConstants.IPPROTO_TCP
            FlowProtocol.UDP.code -> OsConstants.IPPROTO_UDP
            else -> return AppAttribution()
        }
        val local = if (packet.outbound) {
            InetSocketAddress(numericAddress(packet.srcIp), packet.srcPort)
        } else {
            InetSocketAddress(numericAddress(packet.dstIp), packet.dstPort)
        }
        val remote = if (packet.outbound) {
            InetSocketAddress(numericAddress(packet.dstIp), packet.dstPort)
        } else {
            InetSocketAddress(numericAddress(packet.srcIp), packet.srcPort)
        }
        val uid = runCatching {
            connectivityManager?.getConnectionOwnerUid(protocol, local, remote)
        }.getOrNull()?.takeIf { it >= 0 } ?: return AppAttribution()
        val packages = packageManager.getPackagesForUid(uid)
            ?.filter(String::isNotBlank)
            ?.distinct()
            .orEmpty()
        return AppAttribution(
            uid = uid,
            packageName = packages.singleOrNull()
        )
    }

    @Suppress("DEPRECATION")
    private fun resolveNetworkType(): FlowNetworkType {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now < networkCacheExpiresAt) return cachedNetworkType
        val manager = connectivityManager
        val activeCapabilities = manager?.activeNetwork?.let(manager::getNetworkCapabilities)
        val activeType = networkType(activeCapabilities)
        val resolved = activeType.takeUnless {
            it == FlowNetworkType.VPN || it == FlowNetworkType.UNKNOWN
        } ?: manager?.allNetworks
            ?.asSequence()
            ?.mapNotNull(manager::getNetworkCapabilities)
            ?.filterNot { it.hasTransport(NetworkCapabilities.TRANSPORT_VPN) }
            ?.sortedByDescending(::networkScore)
            ?.map(::networkType)
            ?.firstOrNull { it != FlowNetworkType.UNKNOWN }
            ?: activeType
        cachedNetworkType = resolved
        networkCacheExpiresAt = now + networkCacheMillis
        return resolved
    }

    private fun networkType(capabilities: NetworkCapabilities?): FlowNetworkType = when {
        capabilities == null -> FlowNetworkType.UNKNOWN
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> FlowNetworkType.WIFI
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> FlowNetworkType.CELLULAR
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> FlowNetworkType.ETHERNET
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> FlowNetworkType.VPN
        else -> FlowNetworkType.UNKNOWN
    }

    private fun networkScore(capabilities: NetworkCapabilities): Int {
        var score = 0
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) score += 4
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) score += 2
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)) score += 1
        return score
    }

    private fun numericAddress(value: String): InetAddress = InetAddress.getByName(value)

    private fun PacketMetadata.cacheKey(): String = FlowIdentity.canonical(
        ipVersion = ipVersion,
        protocolCode = protocolCode,
        srcIp = srcIp,
        srcPort = srcPort,
        dstIp = dstIp,
        dstPort = dstPort
    ).shardKey()

    private data class AppAttribution(
        val uid: Int? = null,
        val packageName: String? = null
    )

    private companion object {
        private const val DEFAULT_CACHE_CAPACITY = 8_192
        private const val NETWORK_CACHE_MILLIS = 2_000L
    }
}
