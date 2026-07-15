package com.resistine.android.ui.wifi

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

enum class WifiScanFreshness {
    FRESH,
    CACHED,
    STALE,
    UNAVAILABLE
}

data class WifiScanSnapshot(
    val results: List<ScanResult>,
    val freshness: WifiScanFreshness,
    val ageMillis: Long?,
    val freshScanRequested: Boolean,
    val freshScanAccepted: Boolean,
    val resultsUpdated: Boolean
)

internal class WifiScanRepository(context: Context) {

    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(WifiManager::class.java)

    @SuppressLint("MissingPermission")
    suspend fun snapshot(requestFreshScan: Boolean): WifiScanSnapshot {
        val manager = wifiManager ?: return unavailableSnapshot(requestFreshScan)
        if (!requestFreshScan) {
            return buildSnapshot(
                results = readResults(manager),
                freshScanRequested = false,
                freshScanAccepted = false,
                resultsUpdated = false
            )
        }

        val scanOutcome = awaitScanOutcome(manager)
        return buildSnapshot(
            results = readResults(manager),
            freshScanRequested = true,
            freshScanAccepted = scanOutcome.accepted,
            resultsUpdated = scanOutcome.updated
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitScanOutcome(manager: WifiManager): ScanOutcome {
        return withTimeoutOrNull(SCAN_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                var registered = false
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        if (!continuation.isActive) return
                        val updated = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) == true
                        runCatching { appContext.unregisterReceiver(this) }
                        registered = false
                        continuation.resume(ScanOutcome(accepted = true, updated = updated))
                    }
                }
                try {
                    val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                    } else {
                        @Suppress("DEPRECATION")
                        appContext.registerReceiver(receiver, filter)
                    }
                    registered = true
                    val accepted = runCatching { manager.startScan() }.getOrDefault(false)
                    if (!accepted && continuation.isActive) {
                        runCatching { appContext.unregisterReceiver(receiver) }
                        registered = false
                        continuation.resume(ScanOutcome(accepted = false, updated = false))
                    }
                } catch (_: Exception) {
                    if (registered) runCatching { appContext.unregisterReceiver(receiver) }
                    registered = false
                    if (continuation.isActive) {
                        continuation.resume(ScanOutcome(accepted = false, updated = false))
                    }
                }
                continuation.invokeOnCancellation {
                    if (registered) runCatching { appContext.unregisterReceiver(receiver) }
                }
            }
        } ?: ScanOutcome(accepted = true, updated = false)
    }

    @SuppressLint("MissingPermission")
    private fun readResults(manager: WifiManager): List<ScanResult> {
        return runCatching { manager.scanResults.orEmpty() }.getOrDefault(emptyList())
    }

    private fun buildSnapshot(
        results: List<ScanResult>,
        freshScanRequested: Boolean,
        freshScanAccepted: Boolean,
        resultsUpdated: Boolean
    ): WifiScanSnapshot {
        val ageMillis = newestResultAgeMillis(results)
        val freshness = when {
            results.isEmpty() -> WifiScanFreshness.UNAVAILABLE
            resultsUpdated && ageMillis != null && ageMillis <= FRESH_RESULT_MAX_AGE_MILLIS -> WifiScanFreshness.FRESH
            ageMillis != null && ageMillis <= CACHED_RESULT_MAX_AGE_MILLIS -> WifiScanFreshness.CACHED
            else -> WifiScanFreshness.STALE
        }
        return WifiScanSnapshot(
            results = results,
            freshness = freshness,
            ageMillis = ageMillis,
            freshScanRequested = freshScanRequested,
            freshScanAccepted = freshScanAccepted,
            resultsUpdated = resultsUpdated
        )
    }

    private fun newestResultAgeMillis(results: List<ScanResult>): Long? {
        val newestTimestampMicros = results.maxOfOrNull { it.timestamp }?.takeIf { it > 0L } ?: return null
        val elapsedMicros = SystemClock.elapsedRealtime() * 1_000L
        return ((elapsedMicros - newestTimestampMicros) / 1_000L).coerceAtLeast(0L)
    }

    private fun unavailableSnapshot(requested: Boolean) = WifiScanSnapshot(
        results = emptyList(),
        freshness = WifiScanFreshness.UNAVAILABLE,
        ageMillis = null,
        freshScanRequested = requested,
        freshScanAccepted = false,
        resultsUpdated = false
    )

    private data class ScanOutcome(val accepted: Boolean, val updated: Boolean)

    private companion object {
        const val SCAN_TIMEOUT_MILLIS = 5_000L
        const val FRESH_RESULT_MAX_AGE_MILLIS = 15_000L
        const val CACHED_RESULT_MAX_AGE_MILLIS = 120_000L
    }
}
