package com.resistine.android.ui.wifi

import android.content.Context

data class WifiScanEffectivenessMetrics(
    val activeAttempts: Int = 0,
    val updatedResults: Int = 0,
    val rejectedRequests: Int = 0,
    val timedOutRequests: Int = 0,
    val cooldownDeferrals: Int = 0,
    val lastRequestDurationMillis: Long? = null
) {
    fun record(status: WifiScanRequestStatus, durationMillis: Long): WifiScanEffectivenessMetrics {
        return when (status) {
            WifiScanRequestStatus.UPDATED -> copy(
                activeAttempts = activeAttempts + 1,
                updatedResults = updatedResults + 1,
                lastRequestDurationMillis = durationMillis
            )
            WifiScanRequestStatus.REJECTED -> copy(
                activeAttempts = activeAttempts + 1,
                rejectedRequests = rejectedRequests + 1,
                lastRequestDurationMillis = durationMillis
            )
            WifiScanRequestStatus.TIMED_OUT -> copy(
                activeAttempts = activeAttempts + 1,
                timedOutRequests = timedOutRequests + 1,
                lastRequestDurationMillis = durationMillis
            )
            WifiScanRequestStatus.COOLDOWN -> copy(
                cooldownDeferrals = cooldownDeferrals + 1
            )
            WifiScanRequestStatus.NOT_REQUESTED -> this
        }
    }
}

internal class WifiScanMetricsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun record(status: WifiScanRequestStatus, durationMillis: Long): WifiScanEffectivenessMetrics {
        val updated = snapshot().record(status, durationMillis)
        prefs.edit()
            .putInt(KEY_ATTEMPTS, updated.activeAttempts)
            .putInt(KEY_UPDATED, updated.updatedResults)
            .putInt(KEY_REJECTED, updated.rejectedRequests)
            .putInt(KEY_TIMED_OUT, updated.timedOutRequests)
            .putInt(KEY_COOLDOWN, updated.cooldownDeferrals)
            .putLong(KEY_LAST_DURATION, updated.lastRequestDurationMillis ?: -1L)
            .apply()
        return updated
    }

    @Synchronized
    fun snapshot() = WifiScanEffectivenessMetrics(
        activeAttempts = prefs.getInt(KEY_ATTEMPTS, 0),
        updatedResults = prefs.getInt(KEY_UPDATED, 0),
        rejectedRequests = prefs.getInt(KEY_REJECTED, 0),
        timedOutRequests = prefs.getInt(KEY_TIMED_OUT, 0),
        cooldownDeferrals = prefs.getInt(KEY_COOLDOWN, 0),
        lastRequestDurationMillis = prefs.getLong(KEY_LAST_DURATION, -1L).takeIf { it >= 0L }
    )

    private companion object {
        private const val PREFS_NAME = "wifi_scan_effectiveness"
        private const val KEY_ATTEMPTS = "active_attempts"
        private const val KEY_UPDATED = "updated_results"
        private const val KEY_REJECTED = "rejected_requests"
        private const val KEY_TIMED_OUT = "timed_out_requests"
        private const val KEY_COOLDOWN = "cooldown_deferrals"
        private const val KEY_LAST_DURATION = "last_request_duration"
    }
}
