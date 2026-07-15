package com.resistine.android.ui.vpn

import com.resistine.android.ui.wifi.WifiMatchConfidence
import com.resistine.android.ui.wifi.WifiPmfState
import com.resistine.android.ui.wifi.WifiSecurityMode
import com.resistine.android.ui.wifi.WifiSecurityProfile
import com.resistine.android.ui.wifi.WifiTrustBaselineStatus
import com.resistine.android.ui.wifi.WifiTrustedBaselineManager
import com.resistine.android.ui.wifi.WifiTrustedFingerprintObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiTrustedBaselineManagerTest {

    @Test
    fun `does not auto promote suspicious fingerprint when learning is disabled`() {
        val baseline = trustedProfile()
        val observation = WifiTrustedFingerprintObservation(
            bssid = "aa:bb:cc:dd:ee:10",
            securityProfile = WifiSecurityProfile(mode = WifiSecurityMode.WPA2_PSK),
            frequencyMhz = 5200,
            gateway = "192.168.0.1"
        )

        val updated = WifiTrustedBaselineManager.updateProfile(
            profile = baseline,
            observation = observation,
            allowLearning = false,
            approveNow = false,
            nowMillis = 123L
        )

        assertEquals(baseline, updated)
        assertFalse(updated.knownBssids.contains("aa:bb:cc:dd:ee:10"))
    }

    @Test
    fun `never promotes a new fingerprint without explicit approval`() {
        val baseline = trustedProfile()
        val observation = WifiTrustedFingerprintObservation(
            bssid = "aa:bb:cc:dd:ee:10",
            securityProfile = WifiSecurityProfile(mode = WifiSecurityMode.WPA3_SAE),
            frequencyMhz = 5200,
            gateway = "192.168.0.1"
        )

        val first = WifiTrustedBaselineManager.updateProfile(
            profile = baseline,
            observation = observation,
            allowLearning = true,
            approveNow = false,
            nowMillis = 1L
        )
        val second = WifiTrustedBaselineManager.updateProfile(
            profile = first,
            observation = observation,
            allowLearning = true,
            approveNow = false,
            nowMillis = 2L
        )
        val third = WifiTrustedBaselineManager.updateProfile(
            profile = second,
            observation = observation,
            allowLearning = true,
            approveNow = false,
            nowMillis = 3L
        )

        assertEquals(1, first.pendingSeenCount)
        assertEquals(2, second.pendingSeenCount)
        assertFalse(third.knownBssids.contains("aa:bb:cc:dd:ee:10"))
        assertEquals(3, third.pendingSeenCount)

        val approved = WifiTrustedBaselineManager.updateProfile(
            profile = third,
            observation = observation,
            allowLearning = true,
            approveNow = true,
            nowMillis = 4L
        )
        assertTrue(approved.knownBssids.contains("aa:bb:cc:dd:ee:10"))
        assertEquals(0, approved.pendingSeenCount)
    }

    @Test
    fun `detects downgrade from wpa3 to wpa2`() {
        val downgrade = WifiTrustedBaselineManager.isProfileDowngrade(
            baseline = WifiSecurityProfile(
                mode = WifiSecurityMode.WPA3_SAE,
                pmfState = WifiPmfState.REQUIRED
            ),
            current = WifiSecurityProfile(
                mode = WifiSecurityMode.WPA2_PSK,
                pmfState = WifiPmfState.ABSENT
            )
        )

        assertTrue(downgrade)
    }

    @Test
    fun `treats matching profile without bssid as stable profile only instead of fingerprint change`() {
        val baseline = trustedProfile()
        val assessment = WifiTrustedBaselineManager.assess(
            profile = baseline,
            observation = WifiTrustedFingerprintObservation(
                bssid = null,
                securityProfile = WifiSecurityProfile(
                    mode = WifiSecurityMode.WPA3_SAE,
                    pmfState = WifiPmfState.REQUIRED
                ),
                frequencyMhz = 5200,
                gateway = "192.168.0.1"
            ),
            matchConfidence = WifiMatchConfidence.WIFI_INFO_ONLY
        )

        assertEquals(WifiTrustBaselineStatus.STABLE_PROFILE_ONLY, assessment.status)
        assertFalse(assessment.shouldFlagFingerprintChange())
    }

    @Test
    fun `treats ssid only fallback as unverified instead of stable for trusted networks`() {
        val baseline = trustedProfile()
        val assessment = WifiTrustedBaselineManager.assess(
            profile = baseline,
            observation = WifiTrustedFingerprintObservation(
                bssid = null,
                securityProfile = WifiSecurityProfile(
                    mode = WifiSecurityMode.WPA3_SAE,
                    pmfState = WifiPmfState.REQUIRED
                ),
                frequencyMhz = 5200,
                gateway = "192.168.0.1"
            ),
            matchConfidence = WifiMatchConfidence.SSID_ONLY_SINGLE
        )

        assertEquals(WifiTrustBaselineStatus.UNVERIFIED, assessment.status)
        assertFalse(assessment.shouldFlagFingerprintChange())
    }

    @Test
    fun `treats matching profile with ambiguous ssid clones as ambiguous instead of stable`() {
        val baseline = trustedProfile()
        val assessment = WifiTrustedBaselineManager.assess(
            profile = baseline,
            observation = WifiTrustedFingerprintObservation(
                bssid = null,
                securityProfile = WifiSecurityProfile(
                    mode = WifiSecurityMode.WPA3_SAE,
                    pmfState = WifiPmfState.REQUIRED
                ),
                frequencyMhz = 5200,
                gateway = "192.168.0.1"
            ),
            matchConfidence = WifiMatchConfidence.AMBIGUOUS_SSID
        )

        assertEquals(WifiTrustBaselineStatus.AMBIGUOUS, assessment.status)
        assertFalse(assessment.shouldFlagFingerprintChange())
    }

    @Test
    fun `treats matching profile with unavailable ap identity as unverified`() {
        val baseline = trustedProfile()
        val assessment = WifiTrustedBaselineManager.assess(
            profile = baseline,
            observation = WifiTrustedFingerprintObservation(
                bssid = null,
                securityProfile = WifiSecurityProfile(
                    mode = WifiSecurityMode.WPA3_SAE,
                    pmfState = WifiPmfState.REQUIRED
                ),
                frequencyMhz = 5200,
                gateway = "192.168.0.1"
            ),
            matchConfidence = WifiMatchConfidence.UNAVAILABLE
        )

        assertEquals(WifiTrustBaselineStatus.UNVERIFIED, assessment.status)
        assertFalse(assessment.shouldFlagFingerprintChange())
    }

    @Test
    fun `pending fingerprint is exposed distinctly from a raw fingerprint change`() {
        val baseline = trustedProfile().copy(
            pendingBssid = "aa:bb:cc:dd:ee:10",
            pendingSecurityProfile = WifiSecurityProfile(mode = WifiSecurityMode.WPA3_SAE),
            pendingSeenCount = 2
        )
        val assessment = WifiTrustedBaselineManager.assess(
            profile = baseline,
            observation = WifiTrustedFingerprintObservation(
                bssid = "aa:bb:cc:dd:ee:10",
                securityProfile = WifiSecurityProfile(mode = WifiSecurityMode.WPA3_SAE),
                frequencyMhz = 5200,
                gateway = "192.168.0.1"
            ),
            matchConfidence = WifiMatchConfidence.VERIFIED_BSSID
        )

        assertEquals(WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT, assessment.status)
        assertEquals(2, assessment.observationCount)
    }

    @Test
    fun `unknown pmf does not count as a trusted downgrade`() {
        val downgrade = WifiTrustedBaselineManager.isProfileDowngrade(
            baseline = WifiSecurityProfile(
                mode = WifiSecurityMode.WPA3_SAE,
                pmfState = WifiPmfState.REQUIRED
            ),
            current = WifiSecurityProfile(
                mode = WifiSecurityMode.WPA3_SAE,
                pmfState = WifiPmfState.UNKNOWN
            )
        )

        assertFalse(downgrade)
    }

    private fun trustedProfile(): TrustedWifiProfile {
        return TrustedWifiProfile(
            ssid = "Office WiFi",
            bssid = "aa:bb:cc:dd:ee:01",
            knownBssids = setOf("aa:bb:cc:dd:ee:01"),
            securityProfile = WifiSecurityProfile(
                mode = WifiSecurityMode.WPA3_SAE,
                pmfState = WifiPmfState.REQUIRED
            )
        )
    }
}
