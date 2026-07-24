package com.resistine.android.ui.wifi

import com.resistine.android.R
import com.resistine.android.ui.vpn.WifiAdvancedCheckItem
import com.resistine.android.ui.vpn.WifiAlertReason
import com.resistine.android.ui.vpn.WifiNetworkRiskLevel
import com.resistine.android.ui.vpn.WifiSafetyAssessment
import com.resistine.android.ui.vpn.WifiSecurityAlert
import kotlin.collections.plus

object WifiSafetyScorer {

    fun buildCoreChecks(
        alert: WifiSecurityAlert,
        signals: WifiSecuritySignals?
    ): List<WifiAdvancedCheckItem> {
        return listOf(
            buildEncryptionCheck(alert, signals),
            buildInternetValidationCheck(alert),
            buildLegacyRouterCheck(alert, signals),
            buildTrustedFingerprintCheck(alert)
        ) + buildVerificationCoverageChecks(alert)
    }

    fun buildSafetyAssessment(
        alert: WifiSecurityAlert,
        checks: List<WifiAdvancedCheckItem>
    ): WifiSafetyAssessment {
        val dimensionResults = WifiRiskDimension.values().map { dimension ->
            val dimensionChecks = checks.filter { it.dimension == dimension }
            val rawPenalty = dimensionChecks.sumOf { it.penalty }
            val cappedPenalty = rawPenalty.coerceAtMost(dimension.cap)
            val topCheck = dimensionChecks
                .filter { it.penalty > 0 }
                .maxWithOrNull(compareBy<WifiAdvancedCheckItem> { it.penalty }.thenBy { severityScore(it.level) })
            WifiScoreDimensionResult(
                dimension = dimension,
                penalty = cappedPenalty,
                rawPenalty = rawPenalty,
                level = when {
                    cappedPenalty == 0 -> WifiNetworkRiskLevel.SAFE
                    topCheck != null -> topCheck.level
                    else -> WifiNetworkRiskLevel.WARNING
                },
                summary = topCheck?.detail ?: safeDimensionSummary(dimension)
            )
        }
        val totalPenalty = dimensionResults
            .filter { it.dimension != WifiRiskDimension.VISIBILITY }
            .sumOf { it.penalty }
        val score = (100 - totalPenalty).coerceIn(0, 100)
        val level = when {
            score >= 80 -> WifiNetworkRiskLevel.SAFE
            score >= 55 -> WifiNetworkRiskLevel.WARNING
            else -> WifiNetworkRiskLevel.DANGER
        }
        val topDimension = dimensionResults
            .filter { it.penalty > 0 }
            .maxWithOrNull(compareBy<WifiScoreDimensionResult> { it.penalty }.thenBy { severityScore(it.level) })
        val limitedReason = when (alert.reason) {
            WifiAlertReason.UNAVAILABLE,
            WifiAlertReason.NO_NETWORK,
            WifiAlertReason.NOT_WIFI,
            WifiAlertReason.LEGACY_NO_SECURITY_TYPE,
            WifiAlertReason.MISSING_PERMISSION,
            WifiAlertReason.LOCATION_SERVICES_DISABLED,
            WifiAlertReason.UNKNOWN_SECURITY -> true

            else -> false
        }
        val uncertainties = buildSet {
            when (alert.reason) {
                WifiAlertReason.LEGACY_NO_SECURITY_TYPE,
                WifiAlertReason.MISSING_PERMISSION,
                WifiAlertReason.LOCATION_SERVICES_DISABLED,
                WifiAlertReason.UNKNOWN_SECURITY -> add(WifiAssessmentUncertainty.ENCRYPTION_UNVERIFIED)

                else -> Unit
            }
            when (alert.matchConfidence) {
                WifiMatchConfidence.SSID_ONLY_SINGLE,
                WifiMatchConfidence.WIFI_INFO_ONLY -> add(WifiAssessmentUncertainty.AP_IDENTITY_FALLBACK)

                WifiMatchConfidence.AMBIGUOUS_SSID -> add(WifiAssessmentUncertainty.AP_IDENTITY_AMBIGUOUS)
                WifiMatchConfidence.UNAVAILABLE -> add(WifiAssessmentUncertainty.AP_IDENTITY_UNAVAILABLE)
                WifiMatchConfidence.VERIFIED_BSSID -> Unit
            }
        }
        val limitedConfidence = uncertainties.isNotEmpty()
        val isLimitedData = limitedReason || limitedConfidence
        val isScoreAvailable = alert.isOnWifi && uncertainties.isEmpty()
        val recommendationResId = when {
            level != WifiNetworkRiskLevel.SAFE -> R.string.wifi_security_recommendation_warning
            isLimitedData -> R.string.wifi_security_recommendation_info
            else -> R.string.wifi_security_recommendation_secure
        }
        return WifiSafetyAssessment(
            score = score,
            level = level,
            summary = topDimension?.summary ?: alert.matchDetail ?: alert.message,
            recommendationResId = recommendationResId,
            isLimitedData = isLimitedData,
            isScoreAvailable = isScoreAvailable,
            isOnWifi = alert.isOnWifi,
            uncertainties = uncertainties,
            dimensions = dimensionResults
        )
    }

    private fun buildEncryptionCheck(
        alert: WifiSecurityAlert,
        signals: WifiSecuritySignals?
    ): WifiAdvancedCheckItem {
        if (!alert.isOnWifi) {
            return safeCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "Not on Wi-Fi."
            )
        }

        val profile = signals?.profile ?: alert.securityProfile
        return when {
            profile?.mode == WifiSecurityMode.OPEN -> dangerCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "This Wi-Fi is open. Traffic between your device and the access point is not encrypted.",
                penalty = 55
            )

            profile?.mode == WifiSecurityMode.WEP -> dangerCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "This Wi-Fi uses WEP, which is obsolete and should be avoided.",
                penalty = 50
            )

            alert.reason == WifiAlertReason.MISSING_PERMISSION -> warningCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "Location permission is required to inspect Wi-Fi encryption details on this Android version.",
                penalty = 0
            )

            alert.reason == WifiAlertReason.LOCATION_SERVICES_DISABLED -> warningCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "Location services are off, so Wi-Fi encryption details could not be verified.",
                penalty = 0
            )

            alert.reason == WifiAlertReason.LEGACY_NO_SECURITY_TYPE -> warningCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "This Android version cannot confirm the current Wi-Fi encryption type.",
                penalty = 0
            )

            profile == null || profile.mode == WifiSecurityMode.UNKNOWN || alert.reason == WifiAlertReason.UNKNOWN_SECURITY ->
                warningCheck(
                    key = "encryption",
                    dimension = WifiRiskDimension.ENCRYPTION,
                    titleRes = R.string.wifi_check_encryption_title,
                    detail = "Wi-Fi encryption could not be verified for the current connection.",
                    penalty = 0
                )

            profile.mode == WifiSecurityMode.TRANSITION -> warningCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "WPA2/WPA3 transition mode is in use. It is better than WPA2-only, but still allows downgrade paths.",
                penalty = 5
            )

            profile.mode == WifiSecurityMode.WPA2_PSK -> warningCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "Protected Wi-Fi is in use, but WPA2 Personal is older than WPA3.",
                penalty = 0
            )

            profile.mode == WifiSecurityMode.WPA2_ENTERPRISE -> warningCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "Protected enterprise Wi-Fi is in use, but it does not appear to be WPA3 Enterprise.",
                penalty = 0
            )

            profile.mode == WifiSecurityMode.OWE -> safeCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "OWE protection is enabled for this open-style Wi-Fi."
            )

            profile.mode == WifiSecurityMode.WPA3_SAE || profile.mode == WifiSecurityMode.WPA3_ENTERPRISE -> safeCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "Modern WPA3 protection is in use."
            )

            else -> safeCheck(
                key = "encryption",
                dimension = WifiRiskDimension.ENCRYPTION,
                titleRes = R.string.wifi_check_encryption_title,
                detail = "Protected Wi-Fi encryption is in use."
            )
        }
    }

    private fun buildInternetValidationCheck(alert: WifiSecurityAlert): WifiAdvancedCheckItem {
        if (!alert.isOnWifi) {
            return safeCheck(
                key = "internet_validation",
                dimension = WifiRiskDimension.VALIDATION,
                titleRes = R.string.wifi_check_validation_title,
                detail = "Not on Wi-Fi."
            )
        }

        return when {
            alert.reason == WifiAlertReason.CAPTIVE_PORTAL -> warningCheck(
                key = "internet_validation",
                dimension = WifiRiskDimension.VALIDATION,
                titleRes = R.string.wifi_check_validation_title,
                detail = "Captive portal sign-in is required. Avoid sensitive activity until sign-in is complete.",
                penalty = 22
            )

            alert.reason == WifiAlertReason.UNVALIDATED -> warningCheck(
                key = "internet_validation",
                dimension = WifiRiskDimension.VALIDATION,
                titleRes = R.string.wifi_check_validation_title,
                detail = "Android has not validated internet access on this Wi-Fi yet.",
                penalty = 21
            )

            alert.hasInternetAccess == true -> safeCheck(
                key = "internet_validation",
                dimension = WifiRiskDimension.VALIDATION,
                titleRes = R.string.wifi_check_validation_title,
                detail = "Android validated internet access for this Wi-Fi."
            )

            else -> safeCheck(
                key = "internet_validation",
                dimension = WifiRiskDimension.VALIDATION,
                titleRes = R.string.wifi_check_validation_title,
                detail = "Internet validation is not applicable right now."
            )
        }
    }

    private fun buildLegacyRouterCheck(
        alert: WifiSecurityAlert,
        signals: WifiSecuritySignals?
    ): WifiAdvancedCheckItem {
        if (!alert.isOnWifi) {
            return safeCheck(
                key = "legacy_router",
                dimension = WifiRiskDimension.LEGACY,
                titleRes = R.string.wifi_check_legacy_router_title,
                detail = "Not on Wi-Fi."
            )
        }

        val hasWeakCipher = signals?.hasWeakCipher == true || alert.reason == WifiAlertReason.WEAK_LEGACY_CIPHER
        val hasWps = signals?.hasWps == true || alert.reason == WifiAlertReason.WPS_ENABLED
        return when {
            hasWeakCipher && hasWps -> warningCheck(
                key = "legacy_router",
                dimension = WifiRiskDimension.LEGACY,
                titleRes = R.string.wifi_check_legacy_router_title,
                detail = "Legacy cipher support and WPS are both exposed on this network.",
                penalty = 18
            )

            hasWeakCipher -> warningCheck(
                key = "legacy_router",
                dimension = WifiRiskDimension.LEGACY,
                titleRes = R.string.wifi_check_legacy_router_title,
                detail = "Legacy cipher support such as TKIP was detected.",
                penalty = 16
            )

            hasWps -> warningCheck(
                key = "legacy_router",
                dimension = WifiRiskDimension.LEGACY,
                titleRes = R.string.wifi_check_legacy_router_title,
                detail = "WPS is enabled. Disable it on the router when possible.",
                penalty = 10
            )

            signals == null && requiresInspectionCoverageWarning(alert.reason) -> warningCheck(
                key = "legacy_router",
                dimension = WifiRiskDimension.LEGACY,
                titleRes = R.string.wifi_check_legacy_router_title,
                detail = "Router capability details were unavailable for this scan.",
                penalty = 0
            )

            else -> safeCheck(
                key = "legacy_router",
                dimension = WifiRiskDimension.LEGACY,
                titleRes = R.string.wifi_check_legacy_router_title,
                detail = "No weak legacy router features were detected."
            )
        }
    }

    private fun buildTrustedFingerprintCheck(alert: WifiSecurityAlert): WifiAdvancedCheckItem {
        if (!alert.isOnWifi) {
            return safeCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = "Not on Wi-Fi."
            )
        }
        if (!alert.isTrustedNetwork) {
            return safeCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = "No trusted baseline is saved for this network."
            )
        }

        return when (alert.trustStatus) {
            WifiTrustBaselineStatus.DOWNGRADED -> dangerCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = "Trusted network encryption is weaker than the saved baseline.",
                penalty = 25
            )

            WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT,
            WifiTrustBaselineStatus.FINGERPRINT_CHANGED -> warningCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = if (alert.trustObservationCount > 0) {
                    "Trusted network fingerprint changed and has been observed ${alert.trustObservationCount} times. Explicit approval is required."
                } else {
                    "Trusted network identity changed for this SSID. Verify the access point."
                },
                penalty = if (alert.trustObservationCount > 0) 16 else 22
            )

            WifiTrustBaselineStatus.STABLE_PROFILE_ONLY -> warningCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = "Trusted profile matches, but BSSID verification was unavailable.",
                penalty = 4
            )

            WifiTrustBaselineStatus.AMBIGUOUS -> warningCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = "Multiple same-name access points are nearby, so the trusted AP could not be verified confidently.",
                penalty = 8
            )

            WifiTrustBaselineStatus.UNVERIFIED -> warningCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = "Trusted baseline could not be fully verified right now.",
                penalty = 6
            )

            else -> safeCheck(
                key = "trusted_fingerprint",
                dimension = WifiRiskDimension.TRUST,
                titleRes = R.string.wifi_check_trusted_fingerprint_title,
                detail = "Trusted network fingerprint matches the saved baseline."
            )
        }
    }

    private fun buildVerificationCoverageChecks(alert: WifiSecurityAlert): List<WifiAdvancedCheckItem> {
        return listOf(
            buildSecurityVerificationCoverageCheck(alert),
            buildIdentityVerificationCoverageCheck(alert)
        )
    }

    private fun buildSecurityVerificationCoverageCheck(alert: WifiSecurityAlert): WifiAdvancedCheckItem {
        if (!alert.isOnWifi) {
            return safeCheck(
                key = "security_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_security_coverage_title,
                detail = "Not on Wi-Fi."
            )
        }

        return when (alert.reason) {
            WifiAlertReason.LOCATION_SERVICES_DISABLED -> warningCheck(
                key = "security_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_security_coverage_title,
                detail = "Location services are off, so Wi-Fi security details could not be verified fully.",
                penalty = 24
            )

            WifiAlertReason.MISSING_PERMISSION -> warningCheck(
                key = "security_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_security_coverage_title,
                detail = "Location permission is missing, so Wi-Fi security details are only partially available.",
                penalty = 20
            )

            WifiAlertReason.UNKNOWN_SECURITY -> warningCheck(
                key = "security_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_security_coverage_title,
                detail = "Android could not expose the encryption details for this connection.",
                penalty = 28
            )

            WifiAlertReason.LEGACY_NO_SECURITY_TYPE -> warningCheck(
                key = "security_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_security_coverage_title,
                detail = "This Android version cannot fully verify the current Wi-Fi security mode.",
                penalty = 18
            )

            else -> safeCheck(
                key = "security_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_security_coverage_title,
                detail = "Security verification coverage looks complete."
            )
        }
    }

    private fun buildIdentityVerificationCoverageCheck(alert: WifiSecurityAlert): WifiAdvancedCheckItem {
        if (!alert.isOnWifi) {
            return safeCheck(
                key = "identity_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_identity_coverage_title,
                detail = "Not on Wi-Fi."
            )
        }

        return when (alert.matchConfidence) {
            WifiMatchConfidence.VERIFIED_BSSID -> safeCheck(
                key = "identity_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_identity_coverage_title,
                detail = "The current access point identity was verified by BSSID."
            )

            WifiMatchConfidence.SSID_ONLY_SINGLE -> warningCheck(
                key = "identity_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_identity_coverage_title,
                detail = "Only an SSID-only fallback was available for the current access point.",
                penalty = 10
            )

            WifiMatchConfidence.WIFI_INFO_ONLY -> warningCheck(
                key = "identity_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_identity_coverage_title,
                detail = "Wi-Fi profile data is available, but the current access point identity could not be cross-checked.",
                penalty = 14
            )

            WifiMatchConfidence.AMBIGUOUS_SSID -> warningCheck(
                key = "identity_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_identity_coverage_title,
                detail = "Multiple same-name access points are nearby, so the current AP identity is ambiguous.",
                penalty = 20
            )

            WifiMatchConfidence.UNAVAILABLE -> warningCheck(
                key = "identity_coverage",
                dimension = WifiRiskDimension.VISIBILITY,
                titleRes = R.string.wifi_check_identity_coverage_title,
                detail = "The current access point identity was unavailable.",
                penalty = 18
            )
        }
    }

    private fun requiresInspectionCoverageWarning(reason: WifiAlertReason): Boolean {
        return when (reason) {
            WifiAlertReason.LEGACY_NO_SECURITY_TYPE,
            WifiAlertReason.MISSING_PERMISSION,
            WifiAlertReason.LOCATION_SERVICES_DISABLED,
            WifiAlertReason.UNKNOWN_SECURITY -> true

            else -> false
        }
    }

    private fun safeDimensionSummary(dimension: WifiRiskDimension): String {
        return when (dimension) {
            WifiRiskDimension.ENCRYPTION -> "Encryption posture looks healthy."
            WifiRiskDimension.TRUST -> "Trusted network fingerprint looks stable."
            WifiRiskDimension.VALIDATION -> "Internet validation looks healthy."
            WifiRiskDimension.LEGACY -> "No weak legacy router features were detected."
            WifiRiskDimension.NETWORK -> "No active network anomalies were detected."
            WifiRiskDimension.VISIBILITY -> "Verification coverage looks complete."
        }
    }

    private fun safeCheck(
        key: String,
        dimension: WifiRiskDimension,
        titleRes: Int,
        detail: String
    ): WifiAdvancedCheckItem {
        return WifiAdvancedCheckItem(
            key = key,
            dimension = dimension,
            titleResId = titleRes,
            detail = detail,
            level = WifiNetworkRiskLevel.SAFE,
            penalty = 0
        )
    }

    private fun warningCheck(
        key: String,
        dimension: WifiRiskDimension,
        titleRes: Int,
        detail: String,
        penalty: Int
    ): WifiAdvancedCheckItem {
        return WifiAdvancedCheckItem(
            key = key,
            dimension = dimension,
            titleResId = titleRes,
            detail = detail,
            level = WifiNetworkRiskLevel.WARNING,
            penalty = penalty
        )
    }

    private fun dangerCheck(
        key: String,
        dimension: WifiRiskDimension,
        titleRes: Int,
        detail: String,
        penalty: Int
    ): WifiAdvancedCheckItem {
        return WifiAdvancedCheckItem(
            key = key,
            dimension = dimension,
            titleResId = titleRes,
            detail = detail,
            level = WifiNetworkRiskLevel.DANGER,
            penalty = penalty
        )
    }

    private fun severityScore(level: WifiNetworkRiskLevel): Int {
        return when (level) {
            WifiNetworkRiskLevel.SAFE -> 0
            WifiNetworkRiskLevel.WARNING -> 1
            WifiNetworkRiskLevel.DANGER -> 2
        }
    }
}
