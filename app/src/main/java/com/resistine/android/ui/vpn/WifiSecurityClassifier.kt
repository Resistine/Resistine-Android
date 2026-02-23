package com.resistine.android.ui.vpn

data class WifiClassificationInput(
    val hasNetwork: Boolean,
    val isWifi: Boolean,
    val hasCaptivePortal: Boolean,
    val isValidated: Boolean,
    val supportsSecurityTypeDetection: Boolean,
    val hasLocationPermission: Boolean,
    val securityType: WifiSecurityType?
)

data class WifiClassificationResult(
    val level: WifiAlertLevel,
    val reason: WifiAlertReason,
    val requiresLocationPermission: Boolean
)

object WifiSecurityClassifier {
    fun classify(input: WifiClassificationInput): WifiClassificationResult {
        if (!input.hasNetwork) {
            return WifiClassificationResult(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.NO_NETWORK,
                requiresLocationPermission = false
            )
        }

        if (!input.isWifi) {
            return WifiClassificationResult(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.NOT_WIFI,
                requiresLocationPermission = false
            )
        }

        if (input.hasCaptivePortal) {
            return WifiClassificationResult(
                level = WifiAlertLevel.WARNING,
                reason = WifiAlertReason.CAPTIVE_PORTAL,
                requiresLocationPermission = false
            )
        }

        if (!input.isValidated) {
            return WifiClassificationResult(
                level = WifiAlertLevel.WARNING,
                reason = WifiAlertReason.UNVALIDATED,
                requiresLocationPermission = false
            )
        }

        if (!input.supportsSecurityTypeDetection) {
            return WifiClassificationResult(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.LEGACY_NO_SECURITY_TYPE,
                requiresLocationPermission = false
            )
        }

        if (!input.hasLocationPermission) {
            return WifiClassificationResult(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.MISSING_PERMISSION,
                requiresLocationPermission = true
            )
        }

        return when (input.securityType) {
            WifiSecurityType.OPEN,
            WifiSecurityType.WEP -> WifiClassificationResult(
                level = WifiAlertLevel.WARNING,
                reason = WifiAlertReason.OPEN_OR_WEP,
                requiresLocationPermission = false
            )

            WifiSecurityType.SECURE -> WifiClassificationResult(
                level = WifiAlertLevel.SECURE,
                reason = WifiAlertReason.SECURE,
                requiresLocationPermission = false
            )

            WifiSecurityType.UNKNOWN,
            null -> WifiClassificationResult(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.UNKNOWN_SECURITY,
                requiresLocationPermission = false
            )
        }
    }
}
