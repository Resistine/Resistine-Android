package com.resistine.android.ui.vpn.runtime

internal object WireGuardHandshakePolicy {
    private const val CLOCK_TOLERANCE_MS = 5_000L

    fun isConfirmed(
        latestHandshakeEpochMillis: Long,
        attemptStartedEpochMillis: Long
    ): Boolean {
        if (latestHandshakeEpochMillis <= 0L) return false
        return latestHandshakeEpochMillis >= attemptStartedEpochMillis - CLOCK_TOLERANCE_MS
    }
}
