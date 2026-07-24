package com.resistine.android.ui.vpn

import com.resistine.android.R
import com.resistine.android.ui.wifi.WifiAssessmentUncertainty
import com.resistine.android.ui.wifi.WifiMatchConfidence
import com.resistine.android.ui.wifi.WifiPmfState
import com.resistine.android.ui.wifi.WifiSafetyScorer
import com.resistine.android.ui.wifi.WifiSecurityMode
import com.resistine.android.ui.wifi.WifiSecurityProfile
import com.resistine.android.ui.wifi.WifiSecuritySignals
import com.resistine.android.ui.wifi.WifiTrustBaselineStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSafetyScorerTest {

    @Test
    fun `open wifi does not score as safe even without scan capabilities`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.OPEN_OR_WEP,
            securityType = WifiSecurityType.OPEN
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertTrue(assessment.score <= 45)
        assertEquals(WifiNetworkRiskLevel.DANGER, assessment.level)
        assertTrue(checks.any { it.key == "encryption" && it.penalty >= 55 })
    }

    @Test
    fun `wep wifi is scored as danger even without scan capabilities`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.OPEN_OR_WEP,
            securityType = WifiSecurityType.WEP
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertTrue(assessment.score <= 50)
        assertEquals(WifiNetworkRiskLevel.DANGER, assessment.level)
        assertTrue(checks.any { it.key == "encryption" && it.penalty >= 50 })
    }

    @Test
    fun `missing permission limits confidence without lowering safety score`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.MISSING_PERMISSION,
            securityType = null
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertEquals(100, assessment.score)
        assertEquals(WifiNetworkRiskLevel.SAFE, assessment.level)
        assertEquals(R.string.wifi_security_recommendation_info, assessment.recommendationResId)
        assertTrue(assessment.isLimitedData)
        assertFalse(assessment.isScoreAvailable)
        assertTrue(assessment.uncertainties.contains(WifiAssessmentUncertainty.ENCRYPTION_UNVERIFIED))
    }

    @Test
    fun `unknown security remains limited data instead of being called dangerous`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.UNKNOWN_SECURITY,
            securityType = WifiSecurityType.UNKNOWN
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertEquals(100, assessment.score)
        assertEquals(WifiNetworkRiskLevel.SAFE, assessment.level)
        assertTrue(assessment.isLimitedData)
        assertFalse(assessment.isScoreAvailable)
        assertTrue(assessment.uncertainties.contains(WifiAssessmentUncertainty.ENCRYPTION_UNVERIFIED))
    }

    @Test
    fun `trusted downgrade adds direct fingerprint penalty`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE,
            securityType = WifiSecurityType.OPEN,
            isTrustedNetwork = true,
            trustStatus = WifiTrustBaselineStatus.DOWNGRADED
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertEquals(WifiNetworkRiskLevel.DANGER, assessment.level)
        assertTrue(checks.any { it.key == "trusted_fingerprint" && it.penalty >= 25 })
        assertTrue(assessment.score <= 20)
    }

    @Test
    fun `ambiguous trusted wifi keeps warning trust penalty without forcing fingerprint change`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.SECURE,
            securityType = WifiSecurityType.SECURE,
            isTrustedNetwork = true,
            trustStatus = WifiTrustBaselineStatus.AMBIGUOUS
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val trustCheck = checks.first { it.key == "trusted_fingerprint" }
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertEquals(WifiNetworkRiskLevel.WARNING, trustCheck.level)
        assertEquals(8, trustCheck.penalty)
        assertTrue(assessment.score < 100)
    }

    @Test
    fun `stacked limited data signals do not create a false danger score`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.LOCATION_SERVICES_DISABLED,
            securityType = WifiSecurityType.UNKNOWN,
            matchConfidence = WifiMatchConfidence.UNAVAILABLE
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertEquals(100, assessment.score)
        assertEquals(WifiNetworkRiskLevel.SAFE, assessment.level)
        assertTrue(assessment.isLimitedData)
        assertFalse(assessment.isScoreAvailable)
    }

    @Test
    fun `secure wifi without verified access point identity hides numeric score`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.SECURE,
            securityType = WifiSecurityType.SECURE,
            matchConfidence = WifiMatchConfidence.UNAVAILABLE
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals = null)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertEquals(100, assessment.score)
        assertTrue(assessment.isLimitedData)
        assertFalse(assessment.isScoreAvailable)
        assertTrue(
            assessment.uncertainties.contains(
                WifiAssessmentUncertainty.AP_IDENTITY_UNAVAILABLE
            )
        )
    }

    @Test
    fun `validated wpa2 is acceptable and is not penalized only for lacking wpa3`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.SECURE,
            securityType = WifiSecurityType.SECURE
        ).copy(securityProfile = WifiSecurityProfile(mode = WifiSecurityMode.WPA2_PSK))
        val signals = WifiSecuritySignals(
            profile = WifiSecurityProfile(mode = WifiSecurityMode.WPA2_PSK),
            isTransitionMode = false,
            isOwe = false,
            isOpenPlain = false,
            isWpa2Personal = true,
            isWpa3Personal = false,
            isWpa2Enterprise = false,
            isWpa3Enterprise = false,
            isWep = false
        )

        val assessment = WifiSafetyScorer.buildSafetyAssessment(
            alert,
            WifiSafetyScorer.buildCoreChecks(alert, signals)
        )

        assertEquals(100, assessment.score)
        assertEquals(WifiNetworkRiskLevel.SAFE, assessment.level)
    }

    @Test
    fun `validated wpa3 network stays at full score`() {
        val alert = wifiAlert(
            reason = WifiAlertReason.SECURE,
            securityType = WifiSecurityType.SECURE
        )
        val signals = WifiSecuritySignals(
            profile = WifiSecurityProfile(
                mode = WifiSecurityMode.WPA3_SAE,
                pmfState = WifiPmfState.REQUIRED
            ),
            isTransitionMode = false,
            isOwe = false,
            isOpenPlain = false,
            isWpa2Personal = false,
            isWpa3Personal = true,
            isWpa2Enterprise = false,
            isWpa3Enterprise = false,
            isWep = false
        )

        val checks = WifiSafetyScorer.buildCoreChecks(alert, signals)
        val assessment = WifiSafetyScorer.buildSafetyAssessment(alert, checks)

        assertEquals(100, assessment.score)
        assertEquals(WifiNetworkRiskLevel.SAFE, assessment.level)
        assertEquals(R.string.wifi_security_recommendation_secure, assessment.recommendationResId)
        assertFalse(assessment.isLimitedData)
    }

    private fun wifiAlert(
        reason: WifiAlertReason,
        securityType: WifiSecurityType?,
        isTrustedNetwork: Boolean = false,
        trustStatus: WifiTrustBaselineStatus = WifiTrustBaselineStatus.NOT_TRUSTED,
        matchConfidence: WifiMatchConfidence = WifiMatchConfidence.VERIFIED_BSSID
    ): WifiSecurityAlert {
        return WifiSecurityAlert(
            level = WifiAlertLevel.WARNING,
            reason = reason,
            message = reason.name,
            securityType = securityType,
            securityProfile = when (securityType) {
                WifiSecurityType.OPEN -> WifiSecurityProfile(
                    mode = WifiSecurityMode.OPEN,
                    pmfState = WifiPmfState.NOT_APPLICABLE
                )

                WifiSecurityType.WEP -> WifiSecurityProfile(
                    mode = WifiSecurityMode.WEP,
                    pmfState = WifiPmfState.NOT_APPLICABLE
                )

                WifiSecurityType.SECURE -> WifiSecurityProfile(mode = WifiSecurityMode.WPA3_SAE)
                WifiSecurityType.UNKNOWN,
                null -> WifiSecurityProfile(mode = WifiSecurityMode.UNKNOWN)
            },
            requiresLocationPermission = false,
            ssid = "Cafe WiFi",
            bssid = "aa:bb:cc:dd:ee:ff",
            isOnWifi = true,
            isTrustedNetwork = isTrustedNetwork,
            trustStatus = trustStatus,
            canToggleTrust = true,
            hasInternetAccess = true,
            matchConfidence = matchConfidence
        )
    }
}
