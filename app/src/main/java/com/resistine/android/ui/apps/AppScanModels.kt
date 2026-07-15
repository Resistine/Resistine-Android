package com.resistine.android.ui.apps

import android.content.pm.PackageInfo

enum class RiskVerdict {
    NO_CONCERN,
    REVIEW,
    URGENT_REVIEW,
    UNKNOWN
}

enum class BadgeType {
    UNKNOWN_SOURCE,
    LOCAL_INSTALL,
    DEBUGGABLE,
    OLD_TARGET_SDK,
    SENSITIVE_PERMISSION,
    ACCESSIBILITY_ENABLED,
    DEVICE_ADMIN,
    NOTIFICATION_ACCESS,
    INSTALLER_CAPABILITY,
    OVERLAY_CAPABILITY,
    VPN_CAPABILITY
}

enum class InstallProvenance {
    SYSTEM,
    PLAY_STORE,
    RECOGNIZED_STORE,
    LOCAL_OR_ADB,
    UNKNOWN
}

enum class AppIdentityConfidence {
    SYSTEM_VERIFIED,
    STORE_RECORDED,
    UNVERIFIED
}

data class Badge(
    val type: BadgeType,
    val label: String,
    val description: String? = null
)

data class AppScanResult(
    val score: Int,
    val verdict: RiskVerdict,
    val badges: List<Badge>,
    val highRiskPermissions: List<String> = emptyList(),
    val provenance: InstallProvenance = InstallProvenance.UNKNOWN,
    val identityConfidence: AppIdentityConfidence = AppIdentityConfidence.UNVERIFIED,
    val scannedAtMillis: Long = 0L
)

data class AppEntry(
    val packageInfo: PackageInfo,
    val label: String,
    val scanResult: AppScanResult? = null
)

data class ScanSummary(
    val total: Int,
    val noConcern: Int,
    val review: Int,
    val urgentReview: Int,
    val lastScanAt: Long?,
    val isScanned: Boolean
)
