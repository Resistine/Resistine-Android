package com.resistine.android.ui.vpn

import com.resistine.android.ui.wifi.WifiAssessmentUncertainty
import com.resistine.android.ui.wifi.WifiAutoProtectionDecider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiAutoProtectionDeciderTest {

    @Test
    fun `protects limited data wifi when unknown protection is enabled`() {
        val assessment = WifiSafetyAssessment(
            score = 88,
            level = WifiNetworkRiskLevel.SAFE,
            summary = "Current access point could not be verified.",
            isLimitedData = true,
            isOnWifi = true,
            uncertainties = setOf(WifiAssessmentUncertainty.AP_IDENTITY_UNAVAILABLE)
        )

        assertTrue(
            WifiAutoProtectionDecider.shouldProtect(
                assessment = assessment,
                protectUnknownWifi = true
            )
        )
    }

    @Test
    fun `does not protect limited data wifi when unknown protection is disabled`() {
        val assessment = WifiSafetyAssessment(
            score = 88,
            level = WifiNetworkRiskLevel.SAFE,
            summary = "Current access point could not be verified.",
            isLimitedData = true,
            isOnWifi = true,
            uncertainties = setOf(WifiAssessmentUncertainty.AP_IDENTITY_UNAVAILABLE)
        )

        assertFalse(
            WifiAutoProtectionDecider.shouldProtect(
                assessment = assessment,
                protectUnknownWifi = false
            )
        )
    }

    @Test
    fun `does not protect when not on wifi even if assessment is limited`() {
        val assessment = WifiSafetyAssessment(
            score = 100,
            level = WifiNetworkRiskLevel.SAFE,
            summary = "Not on Wi-Fi.",
            isLimitedData = true,
            isOnWifi = false,
            uncertainties = setOf(WifiAssessmentUncertainty.AP_IDENTITY_UNAVAILABLE)
        )

        assertFalse(
            WifiAutoProtectionDecider.shouldProtect(
                assessment = assessment,
                protectUnknownWifi = true
            )
        )
    }
}
