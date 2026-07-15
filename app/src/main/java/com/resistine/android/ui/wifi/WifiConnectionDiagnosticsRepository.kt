package com.resistine.android.ui.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import com.resistine.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

enum class TlsProbeStatus {
    NOT_CONFIGURED,
    NOT_RUN,
    SUCCESS,
    FAILED
}

data class WifiConnectionDiagnostics(
    val privateDnsActive: Boolean? = null,
    val privateDnsServerName: String? = null,
    val dnsServers: List<String> = emptyList(),
    val hasDefaultRoute: Boolean = false,
    val activeRouteUsesVpn: Boolean = false,
    val validatedByAndroid: Boolean = false,
    val tlsProbeStatus: TlsProbeStatus = TlsProbeStatus.NOT_CONFIGURED,
    val tlsProbeLatencyMillis: Long? = null,
    val tlsProbeDetail: String? = null
)

internal class WifiConnectionDiagnosticsRepository(
    context: Context,
    private val diagnosticUrl: String = BuildConfig.NETWORK_DIAGNOSTIC_URL
) {
    private val isProbeConfigured = diagnosticUrl.startsWith("https://")
    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()
    private val probeMutex = Mutex()
    private var lastProbeElapsedMillis: Long? = null

    @Volatile
    private var lastProbe = if (!isProbeConfigured) {
        ProbeResult(TlsProbeStatus.NOT_CONFIGURED, null, null)
    } else {
        ProbeResult(TlsProbeStatus.NOT_RUN, null, null)
    }

    fun snapshot(): WifiConnectionDiagnostics {
        val manager = connectivityManager ?: return WifiConnectionDiagnostics(
            tlsProbeStatus = lastProbe.status,
            tlsProbeLatencyMillis = lastProbe.latencyMillis,
            tlsProbeDetail = lastProbe.detail
        )
        val network = manager.activeNetwork
        val capabilities = network?.let(manager::getNetworkCapabilities)
        val properties = network?.let(manager::getLinkProperties)
        val privateDnsActive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            properties?.isPrivateDnsActive
        } else {
            null
        }
        val privateDnsName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            properties?.privateDnsServerName
        } else {
            null
        }
        return WifiConnectionDiagnostics(
            privateDnsActive = privateDnsActive,
            privateDnsServerName = privateDnsName,
            dnsServers = properties?.dnsServers.orEmpty().mapNotNull { it.hostAddress },
            hasDefaultRoute = properties?.routes.orEmpty().any { it.isDefaultRoute },
            activeRouteUsesVpn = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true,
            validatedByAndroid = capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            ) == true,
            tlsProbeStatus = lastProbe.status,
            tlsProbeLatencyMillis = lastProbe.latencyMillis,
            tlsProbeDetail = lastProbe.detail
        )
    }

    suspend fun probeTls(): WifiConnectionDiagnostics {
        if (!isProbeConfigured) return snapshot()
        return probeMutex.withLock {
            val now = SystemClock.elapsedRealtime()
            val lastProbeAt = lastProbeElapsedMillis
            if (lastProbeAt != null && now - lastProbeAt in 0 until PROBE_COOLDOWN_MILLIS) {
                return@withLock snapshot()
            }
            lastProbe = withContext(Dispatchers.IO) {
                val started = SystemClock.elapsedRealtime()
                runCatching {
                    val request = Request.Builder().url(diagnosticUrl).get().build()
                    httpClient.newCall(request).execute().use { response ->
                        val latency = SystemClock.elapsedRealtime() - started
                        if (response.code in 200..399) {
                            ProbeResult(TlsProbeStatus.SUCCESS, latency, "HTTPS endpoint responded with ${response.code}.")
                        } else {
                            ProbeResult(TlsProbeStatus.FAILED, latency, "HTTPS endpoint returned ${response.code}.")
                        }
                    }
                }.getOrElse { error ->
                    ProbeResult(
                        TlsProbeStatus.FAILED,
                        SystemClock.elapsedRealtime() - started,
                        error.javaClass.simpleName
                    )
                }
            }
            lastProbeElapsedMillis = SystemClock.elapsedRealtime()
            snapshot()
        }
    }

    private data class ProbeResult(
        val status: TlsProbeStatus,
        val latencyMillis: Long?,
        val detail: String?
    )

    private companion object {
        private const val PROBE_COOLDOWN_MILLIS = 60_000L
    }
}
