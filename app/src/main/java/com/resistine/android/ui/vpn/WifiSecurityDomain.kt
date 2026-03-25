package com.resistine.android.ui.vpn

import com.resistine.android.R

enum class WifiSecurityMode {
    OPEN,
    OWE,
    WEP,
    WPA2_PSK,
    WPA3_SAE,
    WPA2_ENTERPRISE,
    WPA3_ENTERPRISE,
    TRANSITION,
    UNKNOWN
}

enum class WifiPmfState {
    REQUIRED,
    CAPABLE,
    ABSENT,
    NOT_APPLICABLE,
    UNKNOWN
}

enum class WifiMatchConfidence {
    VERIFIED_BSSID,
    SSID_ONLY_SINGLE,
    AMBIGUOUS_SSID,
    WIFI_INFO_ONLY,
    UNAVAILABLE
}

enum class WifiTrustBaselineStatus {
    NOT_TRUSTED,
    STABLE_VERIFIED,
    STABLE_PROFILE_ONLY,
    PENDING_NEW_FINGERPRINT,
    FINGERPRINT_CHANGED,
    DOWNGRADED,
    AMBIGUOUS,
    UNVERIFIED
}

enum class WifiAssessmentUncertainty {
    ENCRYPTION_UNVERIFIED,
    AP_IDENTITY_FALLBACK,
    AP_IDENTITY_AMBIGUOUS,
    AP_IDENTITY_UNAVAILABLE
}

enum class WifiRiskDimension(val titleResId: Int, val cap: Int) {
    ENCRYPTION(R.string.wifi_dimension_encryption, 55),
    TRUST(R.string.wifi_dimension_trust, 25),
    VALIDATION(R.string.wifi_dimension_validation, 22),
    LEGACY(R.string.wifi_dimension_legacy, 18),
    NETWORK(R.string.wifi_dimension_network, 18),
    VISIBILITY(R.string.wifi_dimension_visibility, 48)
}

data class WifiSecurityProfile(
    val mode: WifiSecurityMode,
    val pmfState: WifiPmfState = WifiPmfState.UNKNOWN,
    val hasWeakCipher: Boolean = false,
    val hasWps: Boolean = false
) {
    val broadType: WifiSecurityType
        get() = when (mode) {
            WifiSecurityMode.OPEN -> WifiSecurityType.OPEN
            WifiSecurityMode.WEP -> WifiSecurityType.WEP
            WifiSecurityMode.UNKNOWN -> WifiSecurityType.UNKNOWN
            else -> WifiSecurityType.SECURE
        }

    fun strengthRank(): Int {
        return when (mode) {
            WifiSecurityMode.OPEN -> 0
            WifiSecurityMode.WEP -> 1
            WifiSecurityMode.OWE -> 2
            WifiSecurityMode.WPA2_PSK -> 3
            WifiSecurityMode.WPA2_ENTERPRISE -> 4
            WifiSecurityMode.TRANSITION -> 5
            WifiSecurityMode.WPA3_SAE -> 6
            WifiSecurityMode.WPA3_ENTERPRISE -> 7
            WifiSecurityMode.UNKNOWN -> 2
        }
    }

    fun pmfRank(): Int {
        return when (pmfState) {
            WifiPmfState.REQUIRED -> 3
            WifiPmfState.CAPABLE -> 2
            WifiPmfState.ABSENT -> 1
            WifiPmfState.NOT_APPLICABLE,
            WifiPmfState.UNKNOWN -> 0
        }
    }

    fun storageKey(): String {
        return listOf(mode.name, pmfState.name, hasWeakCipher.toString(), hasWps.toString())
            .joinToString("|")
    }

    fun equivalentTo(other: WifiSecurityProfile?): Boolean {
        if (other == null) return false
        return mode == other.mode &&
            pmfState == other.pmfState &&
            hasWeakCipher == other.hasWeakCipher &&
            hasWps == other.hasWps
    }
}

data class WifiSecuritySignals(
    val profile: WifiSecurityProfile,
    val isTransitionMode: Boolean,
    val isOwe: Boolean,
    val isOpenPlain: Boolean,
    val isWpa2Personal: Boolean,
    val isWpa3Personal: Boolean,
    val isWpa2Enterprise: Boolean,
    val isWpa3Enterprise: Boolean,
    val isWep: Boolean
) {
    val securityType: WifiSecurityType get() = profile.broadType
    val hasWps: Boolean get() = profile.hasWps
    val hasWeakCipher: Boolean get() = profile.hasWeakCipher
    val hasPmfRequired: Boolean get() = profile.pmfState == WifiPmfState.REQUIRED
    val hasPmfCapable: Boolean get() = profile.pmfState == WifiPmfState.CAPABLE
}

data class WifiScanNetworkSnapshot(
    val ssid: String?,
    val bssid: String?,
    val capabilities: String?,
    val frequencyMhz: Int? = null,
    val signalLevel: Int = Int.MIN_VALUE
)

data class WifiCurrentNetworkMatch(
    val matchedSnapshot: WifiScanNetworkSnapshot?,
    val confidence: WifiMatchConfidence,
    val detail: String,
    val sameSsidCandidates: Int
) {
    fun isLimitedData(): Boolean {
        return confidence == WifiMatchConfidence.AMBIGUOUS_SSID ||
            confidence == WifiMatchConfidence.WIFI_INFO_ONLY ||
            confidence == WifiMatchConfidence.UNAVAILABLE
    }
}

data class WifiScoreDimensionResult(
    val dimension: WifiRiskDimension,
    val penalty: Int,
    val rawPenalty: Int,
    val level: WifiNetworkRiskLevel,
    val summary: String
)

data class WifiTrustedFingerprintObservation(
    val bssid: String?,
    val securityProfile: WifiSecurityProfile?,
    val frequencyMhz: Int?,
    val gateway: String?
)

data class WifiTrustAssessment(
    val status: WifiTrustBaselineStatus,
    val observationCount: Int = 0,
    val threshold: Int = 0
) {
    fun shouldFlagFingerprintChange(): Boolean {
        return status == WifiTrustBaselineStatus.FINGERPRINT_CHANGED
    }

    fun shouldFlagDowngrade(): Boolean {
        return status == WifiTrustBaselineStatus.DOWNGRADED
    }

    fun isStable(): Boolean {
        return status == WifiTrustBaselineStatus.STABLE_VERIFIED ||
            status == WifiTrustBaselineStatus.STABLE_PROFILE_ONLY
    }
}

object WifiSecurityProfileParser {

    fun parseCapabilities(capabilities: String?): WifiSecuritySignals {
        val normalized = capabilities.orEmpty().uppercase()
        val hasWep = "WEP" in normalized
        val hasPsk = "PSK" in normalized
        val hasSae = "SAE" in normalized
        val hasEap = "EAP" in normalized
        val hasSuiteB = "SUITE_B_192" in normalized || "EAP_WPA3_ENTERPRISE_192_BIT" in normalized
        val hasOweTransition = "OWE_TRANSITION" in normalized
        val hasOwe = ("OWE" in normalized) && !hasOweTransition
        val hasWps = "WPS" in normalized
        val hasWeakCipher = "TKIP" in normalized
        val pmfState = when {
            "MFPR" in normalized -> WifiPmfState.REQUIRED
            "MFPC" in normalized -> WifiPmfState.CAPABLE
            normalized.isBlank() || normalized == "[ESS]" || hasWep -> WifiPmfState.NOT_APPLICABLE
            "RSN" in normalized || "WPA" in normalized || hasPsk || hasSae || hasEap || hasOwe || hasOweTransition ->
                WifiPmfState.ABSENT

            else -> WifiPmfState.UNKNOWN
        }
        val mode = when {
            hasWep -> WifiSecurityMode.WEP
            hasOwe -> WifiSecurityMode.OWE
            hasOweTransition -> WifiSecurityMode.TRANSITION
            hasSae && hasPsk -> WifiSecurityMode.TRANSITION
            hasSae -> WifiSecurityMode.WPA3_SAE
            hasSuiteB -> WifiSecurityMode.WPA3_ENTERPRISE
            hasEap -> WifiSecurityMode.WPA2_ENTERPRISE
            hasPsk -> WifiSecurityMode.WPA2_PSK
            normalized.isBlank() || normalized == "[ESS]" -> WifiSecurityMode.OPEN
            else -> WifiSecurityMode.UNKNOWN
        }
        return WifiSecuritySignals(
            profile = WifiSecurityProfile(
                mode = mode,
                pmfState = pmfState,
                hasWeakCipher = hasWeakCipher,
                hasWps = hasWps
            ),
            isTransitionMode = mode == WifiSecurityMode.TRANSITION || hasOweTransition,
            isOwe = mode == WifiSecurityMode.OWE || hasOweTransition,
            isOpenPlain = mode == WifiSecurityMode.OPEN,
            isWpa2Personal = mode == WifiSecurityMode.WPA2_PSK,
            isWpa3Personal = mode == WifiSecurityMode.WPA3_SAE,
            isWpa2Enterprise = mode == WifiSecurityMode.WPA2_ENTERPRISE,
            isWpa3Enterprise = mode == WifiSecurityMode.WPA3_ENTERPRISE,
            isWep = mode == WifiSecurityMode.WEP
        )
    }
}

object WifiCurrentNetworkMatcher {

    fun match(
        currentSsid: String?,
        currentBssid: String?,
        scanResults: List<WifiScanNetworkSnapshot>
    ): WifiCurrentNetworkMatch {
        val ssidMatches = scanResults.filter { candidate ->
            currentSsid != null &&
                candidate.ssid != null &&
                candidate.ssid.equals(currentSsid, ignoreCase = true)
        }.sortedByDescending { it.signalLevel }

        if (currentBssid != null) {
            val exact = scanResults.firstOrNull { candidate ->
                candidate.bssid != null && candidate.bssid.equals(currentBssid, ignoreCase = true)
            }
            if (exact != null) {
                return WifiCurrentNetworkMatch(
                    matchedSnapshot = exact,
                    confidence = WifiMatchConfidence.VERIFIED_BSSID,
                    detail = "Current access point was verified by BSSID.",
                    sameSsidCandidates = ssidMatches.size
                )
            }
            if (ssidMatches.size == 1) {
                return WifiCurrentNetworkMatch(
                    matchedSnapshot = ssidMatches.first(),
                    confidence = WifiMatchConfidence.SSID_ONLY_SINGLE,
                    detail = "Current access point was not present by BSSID, so one same-name AP was used as a fallback.",
                    sameSsidCandidates = 1
                )
            }
            return WifiCurrentNetworkMatch(
                matchedSnapshot = null,
                confidence = if (ssidMatches.isNotEmpty()) {
                    WifiMatchConfidence.WIFI_INFO_ONLY
                } else {
                    WifiMatchConfidence.UNAVAILABLE
                },
                detail = if (ssidMatches.isNotEmpty()) {
                    "The connected access point was not found by BSSID in scan results."
                } else {
                    "Current access point details were unavailable from the scan cache."
                },
                sameSsidCandidates = ssidMatches.size
            )
        }

        return when {
            ssidMatches.size == 1 -> WifiCurrentNetworkMatch(
                matchedSnapshot = ssidMatches.first(),
                confidence = WifiMatchConfidence.SSID_ONLY_SINGLE,
                detail = "Current access point was matched by SSID only.",
                sameSsidCandidates = 1
            )

            ssidMatches.size > 1 -> WifiCurrentNetworkMatch(
                matchedSnapshot = null,
                confidence = WifiMatchConfidence.AMBIGUOUS_SSID,
                detail = "Multiple same-name access points are nearby, so the current AP could not be matched confidently.",
                sameSsidCandidates = ssidMatches.size
            )

            else -> WifiCurrentNetworkMatch(
                matchedSnapshot = null,
                confidence = WifiMatchConfidence.UNAVAILABLE,
                detail = "Current access point details were unavailable from scan results.",
                sameSsidCandidates = 0
            )
        }
    }
}

object WifiTrustPolicy {

    fun isTrustEligibleSsid(
        ssid: String?,
        reservedLabels: Set<String>
    ): Boolean {
        val normalized = ssid?.trim()
        return !normalized.isNullOrBlank() &&
            reservedLabels.none { reserved ->
                reserved.equals(normalized, ignoreCase = true)
            }
    }

    fun canToggleNearbyTrust(
        isCurrent: Boolean,
        isTrusted: Boolean,
        isTrustEligible: Boolean
    ): Boolean {
        return isTrusted || (isCurrent && isTrustEligible)
    }
}

object WifiTrustedBaselineManager {

    const val AUTO_PROMOTION_THRESHOLD = 3

    fun isProfileDowngrade(
        baseline: WifiSecurityProfile,
        current: WifiSecurityProfile?
    ): Boolean {
        if (current == null) return false
        if (baseline.mode == WifiSecurityMode.UNKNOWN) {
            return baseline.broadType == WifiSecurityType.SECURE &&
                (current.broadType == WifiSecurityType.OPEN || current.broadType == WifiSecurityType.WEP)
        }
        if (current.mode == WifiSecurityMode.UNKNOWN) return false
        if (current.strengthRank() < baseline.strengthRank()) return true
        if (current.mode == WifiSecurityMode.TRANSITION &&
            (baseline.mode == WifiSecurityMode.WPA3_SAE || baseline.mode == WifiSecurityMode.WPA3_ENTERPRISE)
        ) {
            return true
        }
        val baselinePmfKnown = baseline.pmfState != WifiPmfState.UNKNOWN &&
            baseline.pmfState != WifiPmfState.NOT_APPLICABLE
        val currentPmfKnown = current.pmfState != WifiPmfState.UNKNOWN &&
            current.pmfState != WifiPmfState.NOT_APPLICABLE
        return current.mode == baseline.mode &&
            baselinePmfKnown &&
            currentPmfKnown &&
            current.pmfRank() < baseline.pmfRank()
    }

    fun matchesStableFingerprint(
        profile: TrustedWifiProfile,
        observation: WifiTrustedFingerprintObservation
    ): Boolean {
        val normalizedBssid = observation.bssid
        if (normalizedBssid != null &&
            profile.knownBssids.any { it.equals(normalizedBssid, ignoreCase = true) }
        ) {
            return observation.securityProfile?.equivalentTo(profile.securityProfile) != false
        }
        return false
    }

    fun assess(
        profile: TrustedWifiProfile,
        observation: WifiTrustedFingerprintObservation?,
        matchConfidence: WifiMatchConfidence
    ): WifiTrustAssessment {
        val currentProfile = observation?.securityProfile
        if (currentProfile == null) {
            return WifiTrustAssessment(status = WifiTrustBaselineStatus.UNVERIFIED)
        }
        if (isProfileDowngrade(profile.securityProfile, currentProfile)) {
            return WifiTrustAssessment(status = WifiTrustBaselineStatus.DOWNGRADED)
        }
        if (observation != null && matchesPendingFingerprint(profile, observation)) {
            return WifiTrustAssessment(
                status = WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT,
                observationCount = profile.pendingSeenCount,
                threshold = AUTO_PROMOTION_THRESHOLD
            )
        }

        val profileMatches = currentProfile.equivalentTo(profile.securityProfile)
        val normalizedBssid = observation?.bssid
        val knownBssid = normalizedBssid != null &&
            profile.knownBssids.any { it.equals(normalizedBssid, ignoreCase = true) }

        return when {
            knownBssid && profileMatches -> WifiTrustAssessment(
                status = WifiTrustBaselineStatus.STABLE_VERIFIED
            )

            profileMatches && matchConfidence == WifiMatchConfidence.WIFI_INFO_ONLY -> WifiTrustAssessment(
                status = WifiTrustBaselineStatus.STABLE_PROFILE_ONLY
            )

            profileMatches && matchConfidence == WifiMatchConfidence.AMBIGUOUS_SSID -> WifiTrustAssessment(
                status = WifiTrustBaselineStatus.AMBIGUOUS
            )

            profileMatches -> WifiTrustAssessment(
                status = WifiTrustBaselineStatus.UNVERIFIED
            )

            normalizedBssid != null -> WifiTrustAssessment(
                status = WifiTrustBaselineStatus.FINGERPRINT_CHANGED
            )

            matchConfidence == WifiMatchConfidence.AMBIGUOUS_SSID -> WifiTrustAssessment(
                status = WifiTrustBaselineStatus.AMBIGUOUS
            )

            else -> WifiTrustAssessment(
                status = WifiTrustBaselineStatus.UNVERIFIED
            )
        }
    }

    fun matchesPendingFingerprint(
        profile: TrustedWifiProfile,
        observation: WifiTrustedFingerprintObservation
    ): Boolean {
        return profile.pendingBssid != null &&
            observation.bssid != null &&
            profile.pendingBssid.equals(observation.bssid, ignoreCase = true) &&
            profile.pendingSecurityProfile?.equivalentTo(observation.securityProfile) != false
    }

    fun updateProfile(
        profile: TrustedWifiProfile,
        observation: WifiTrustedFingerprintObservation,
        allowLearning: Boolean,
        approveNow: Boolean,
        nowMillis: Long
    ): TrustedWifiProfile {
        if (matchesStableFingerprint(profile, observation)) {
            return profile.copy(
                lastFrequencyMhz = observation.frequencyMhz ?: profile.lastFrequencyMhz,
                lastSeenMillis = nowMillis,
                lastGateway = observation.gateway ?: profile.lastGateway
            )
        }

        if (!allowLearning && !approveNow) {
            return profile
        }

        val observationBssid = observation.bssid ?: return profile
        val observationProfile = observation.securityProfile ?: return profile
        val pendingCount = if (matchesPendingFingerprint(profile, observation)) {
            profile.pendingSeenCount + 1
        } else {
            1
        }
        val promote = approveNow || pendingCount >= AUTO_PROMOTION_THRESHOLD
        return if (promote) {
            profile.copy(
                bssid = observationBssid,
                knownBssids = (profile.knownBssids + observationBssid),
                securityProfile = observationProfile,
                lastFrequencyMhz = observation.frequencyMhz ?: profile.lastFrequencyMhz,
                lastSeenMillis = nowMillis,
                lastGateway = observation.gateway ?: profile.lastGateway,
                pendingBssid = null,
                pendingSecurityProfile = null,
                pendingSeenCount = 0,
                pendingLastFrequencyMhz = null,
                pendingLastGateway = null,
                pendingLastSeenMillis = null
            )
        } else {
            profile.copy(
                pendingBssid = observationBssid,
                pendingSecurityProfile = observationProfile,
                pendingSeenCount = pendingCount,
                pendingLastFrequencyMhz = observation.frequencyMhz,
                pendingLastGateway = observation.gateway,
                pendingLastSeenMillis = nowMillis
            )
        }
    }
}

object WifiAutoProtectionDecider {

    fun shouldProtect(
        assessment: WifiSafetyAssessment,
        protectUnknownWifi: Boolean
    ): Boolean {
        return assessment.isOnWifi && (
            assessment.level != WifiNetworkRiskLevel.SAFE ||
            (protectUnknownWifi && assessment.uncertainties.any {
                it == WifiAssessmentUncertainty.ENCRYPTION_UNVERIFIED ||
                    it == WifiAssessmentUncertainty.AP_IDENTITY_FALLBACK ||
                    it == WifiAssessmentUncertainty.AP_IDENTITY_AMBIGUOUS ||
                    it == WifiAssessmentUncertainty.AP_IDENTITY_UNAVAILABLE
            })
        )
    }
}
