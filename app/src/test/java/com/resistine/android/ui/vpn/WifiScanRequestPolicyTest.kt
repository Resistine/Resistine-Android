package com.resistine.android.ui.vpn

import com.resistine.android.ui.wifi.WifiScanRequestPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiScanRequestPolicyTest {

    @Test
    fun `first request is immediately eligible`() {
        assertEquals(
            0L,
            WifiScanRequestPolicy.cooldownRemainingMillis(
                nowElapsedMillis = 1_000L,
                lastRequestElapsedMillis = null,
                cooldownMillis = 30_000L
            )
        )
    }

    @Test
    fun `repeated request returns remaining cooldown`() {
        assertEquals(
            20_000L,
            WifiScanRequestPolicy.cooldownRemainingMillis(
                nowElapsedMillis = 20_000L,
                lastRequestElapsedMillis = 10_000L,
                cooldownMillis = 30_000L
            )
        )
    }

    @Test
    fun `request becomes eligible after cooldown`() {
        assertEquals(
            0L,
            WifiScanRequestPolicy.cooldownRemainingMillis(
                nowElapsedMillis = 45_000L,
                lastRequestElapsedMillis = 10_000L,
                cooldownMillis = 30_000L
            )
        )
    }

    @Test
    fun `elapsed clock reset never creates a negative cooldown`() {
        assertEquals(
            30_000L,
            WifiScanRequestPolicy.cooldownRemainingMillis(
                nowElapsedMillis = 1_000L,
                lastRequestElapsedMillis = 10_000L,
                cooldownMillis = 30_000L
            )
        )
    }
}
