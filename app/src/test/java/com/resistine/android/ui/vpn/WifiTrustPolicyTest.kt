package com.resistine.android.ui.vpn

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiTrustPolicyTest {

    @Test
    fun `blocks synthetic or hidden ssids from being trusted`() {
        val reserved = setOf("Connected Wi-Fi", "Hidden network")

        assertFalse(WifiTrustPolicy.isTrustEligibleSsid("Connected Wi-Fi", reserved))
        assertFalse(WifiTrustPolicy.isTrustEligibleSsid("Hidden network", reserved))
        assertTrue(WifiTrustPolicy.isTrustEligibleSsid("Office WiFi", reserved))
    }

    @Test
    fun `only current or already trusted nearby networks can toggle trust`() {
        assertFalse(
            WifiTrustPolicy.canToggleNearbyTrust(
                isCurrent = false,
                isTrusted = false,
                isTrustEligible = true
            )
        )
        assertTrue(
            WifiTrustPolicy.canToggleNearbyTrust(
                isCurrent = true,
                isTrusted = false,
                isTrustEligible = true
            )
        )
        assertTrue(
            WifiTrustPolicy.canToggleNearbyTrust(
                isCurrent = false,
                isTrusted = true,
                isTrustEligible = false
            )
        )
    }
}
