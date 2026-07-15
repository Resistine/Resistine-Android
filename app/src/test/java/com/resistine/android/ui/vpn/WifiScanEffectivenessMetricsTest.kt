package com.resistine.android.ui.vpn

import com.resistine.android.ui.wifi.WifiScanEffectivenessMetrics
import com.resistine.android.ui.wifi.WifiScanRequestStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiScanEffectivenessMetricsTest {

    @Test
    fun `records active outcomes separately from cooldown deferrals`() {
        val metrics = WifiScanEffectivenessMetrics()
            .record(WifiScanRequestStatus.UPDATED, 800L)
            .record(WifiScanRequestStatus.REJECTED, 20L)
            .record(WifiScanRequestStatus.TIMED_OUT, 5_000L)
            .record(WifiScanRequestStatus.COOLDOWN, 2L)

        assertEquals(3, metrics.activeAttempts)
        assertEquals(1, metrics.updatedResults)
        assertEquals(1, metrics.rejectedRequests)
        assertEquals(1, metrics.timedOutRequests)
        assertEquals(1, metrics.cooldownDeferrals)
        assertEquals(5_000L, metrics.lastRequestDurationMillis)
    }
}
