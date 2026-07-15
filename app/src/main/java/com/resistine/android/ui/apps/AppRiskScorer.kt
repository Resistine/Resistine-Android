package com.resistine.android.ui.apps

internal data class AppRiskInput(
    val provenance: InstallProvenance,
    val targetSdk: Int,
    val deviceSdk: Int,
    val isDebuggable: Boolean,
    val sensitivePermissionWeight: Int,
    val accessibilityEnabled: Boolean,
    val deviceAdminActive: Boolean,
    val notificationAccessEnabled: Boolean,
    val overlayEnabled: Boolean,
    val usageAccessEnabled: Boolean,
    val declaresInstallerCapability: Boolean,
    val declaresOverlayCapability: Boolean
)

internal data class AppRiskScore(
    val value: Int,
    val verdict: RiskVerdict
)

internal object AppRiskScorer {

    fun score(input: AppRiskInput): AppRiskScore {
        val uncertainSource = input.provenance == InstallProvenance.LOCAL_OR_ADB ||
            input.provenance == InstallProvenance.UNKNOWN
        var score = when (input.provenance) {
            InstallProvenance.LOCAL_OR_ADB -> 10
            InstallProvenance.UNKNOWN -> 5
            else -> 0
        }

        if (input.isDebuggable && input.provenance != InstallProvenance.SYSTEM) score += 8
        val targetLag = input.deviceSdk - input.targetSdk
        if (input.targetSdk in 1..28) score += 10
        else if (targetLag >= 5) score += 5

        score += input.sensitivePermissionWeight.coerceAtMost(15)
        if (input.accessibilityEnabled) score += 35
        if (input.deviceAdminActive) score += 25
        if (input.notificationAccessEnabled) score += 15
        if (input.overlayEnabled) score += 20
        if (input.usageAccessEnabled) score += 8
        if (input.declaresInstallerCapability) score += 8
        if (input.declaresOverlayCapability && !input.overlayEnabled) score += 8

        if (uncertainSource && input.accessibilityEnabled) score += 20
        if (uncertainSource && input.deviceAdminActive) score += 20
        if (uncertainSource && input.isDebuggable) score += 12
        if (uncertainSource && input.declaresInstallerCapability && input.declaresOverlayCapability) score += 25
        if (uncertainSource && input.notificationAccessEnabled) score += 10
        if (uncertainSource && input.overlayEnabled) score += 15
        if (uncertainSource && input.usageAccessEnabled) score += 7

        val value = score.coerceIn(0, 100)
        val verdict = when {
            value >= 45 -> RiskVerdict.URGENT_REVIEW
            value >= 12 -> RiskVerdict.REVIEW
            else -> RiskVerdict.NO_CONCERN
        }
        return AppRiskScore(value, verdict)
    }
}
