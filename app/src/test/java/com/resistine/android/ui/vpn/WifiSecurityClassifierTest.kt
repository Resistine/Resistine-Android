package com.resistine.android.ui.vpn

import com.resistine.android.ui.wifi.WifiClassificationInput
import com.resistine.android.ui.wifi.WifiSecurityClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSecurityClassifierTest {

    @Test
    fun `returns no network when network is unavailable`() {
        val result = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = false,
                isWifi = false,
                hasCaptivePortal = false,
                isValidated = false,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = null
            )
        )

        assertEquals(WifiAlertLevel.INFO, result.level)
        assertEquals(WifiAlertReason.NO_NETWORK, result.reason)
        assertFalse(result.requiresLocationPermission)
    }

    @Test
    fun `returns captive portal warning`() {
        val result = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = true,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = WifiSecurityType.SECURE
            )
        )

        assertEquals(WifiAlertLevel.WARNING, result.level)
        assertEquals(WifiAlertReason.CAPTIVE_PORTAL, result.reason)
        assertFalse(result.requiresLocationPermission)
    }

    @Test
    fun `returns unvalidated warning`() {
        val result = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = false,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = WifiSecurityType.SECURE
            )
        )

        assertEquals(WifiAlertLevel.WARNING, result.level)
        assertEquals(WifiAlertReason.UNVALIDATED, result.reason)
        assertFalse(result.requiresLocationPermission)
    }

    @Test
    fun `returns not wifi info when active transport is not wifi`() {
        val result = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = false,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = null
            )
        )

        assertEquals(WifiAlertLevel.INFO, result.level)
        assertEquals(WifiAlertReason.NOT_WIFI, result.reason)
        assertFalse(result.requiresLocationPermission)
    }

    @Test
    fun `returns open or wep warning`() {
        val openResult = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = WifiSecurityType.OPEN
            )
        )
        val wepResult = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = WifiSecurityType.WEP
            )
        )

        assertEquals(WifiAlertLevel.WARNING, openResult.level)
        assertEquals(WifiAlertReason.OPEN_OR_WEP, openResult.reason)
        assertEquals(WifiAlertLevel.WARNING, wepResult.level)
        assertEquals(WifiAlertReason.OPEN_OR_WEP, wepResult.reason)
    }

    @Test
    fun `returns unknown security when security type is unknown`() {
        val unknownResult = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = WifiSecurityType.UNKNOWN
            )
        )
        val nullResult = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = null
            )
        )

        assertEquals(WifiAlertLevel.INFO, unknownResult.level)
        assertEquals(WifiAlertReason.UNKNOWN_SECURITY, unknownResult.reason)
        assertEquals(WifiAlertLevel.INFO, nullResult.level)
        assertEquals(WifiAlertReason.UNKNOWN_SECURITY, nullResult.reason)
    }

    @Test
    fun `returns secure for modern protected wifi`() {
        val result = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = true,
                securityType = WifiSecurityType.SECURE
            )
        )

        assertEquals(WifiAlertLevel.SECURE, result.level)
        assertEquals(WifiAlertReason.SECURE, result.reason)
        assertFalse(result.requiresLocationPermission)
    }

    @Test
    fun `returns missing permission when wifi encryption needs location access`() {
        val result = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = true,
                hasLocationPermission = false,
                securityType = null
            )
        )

        assertEquals(WifiAlertLevel.INFO, result.level)
        assertEquals(WifiAlertReason.MISSING_PERMISSION, result.reason)
        assertTrue(result.requiresLocationPermission)
    }

    @Test
    fun `returns legacy info when security type detection is unsupported`() {
        val result = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = true,
                hasCaptivePortal = false,
                isValidated = true,
                supportsSecurityTypeDetection = false,
                hasLocationPermission = true,
                securityType = WifiSecurityType.SECURE
            )
        )

        assertEquals(WifiAlertLevel.INFO, result.level)
        assertEquals(WifiAlertReason.LEGACY_NO_SECURITY_TYPE, result.reason)
        assertFalse(result.requiresLocationPermission)
    }
}
