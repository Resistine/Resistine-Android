package com.resistine.android.ui.apps

import android.content.pm.PackageInfo

enum class RiskVerdict {
    SAFE,
    WARNING,
    RISK,
    UNKNOWN
}

enum class BadgeType {
    SIGNATURE_MISMATCH,
    LOOKALIKE_NAME,
    OUTDATED,
    SIDELOADED,
    DEBUGGABLE,
    OLD_TARGET_SDK,
    HIGH_RISK_PERMISSION
}

data class Badge(
    val type: BadgeType,
    val label: String
)

data class AppScanResult(
    val score: Int,
    val verdict: RiskVerdict,
    val badges: List<Badge>,
    val apkSha256: List<String>
)

data class AppEntry(
    val packageInfo: PackageInfo,
    val label: String,
    val scanResult: AppScanResult? = null
)

data class ScanSummary(
    val total: Int,
    val safe: Int,
    val warning: Int,
    val risk: Int,
    val lastScanAt: Long?,
    val isScanned: Boolean
)

internal data class RegistryEntry(
    val packageName: String,
    val name: String,
    val versionCode: Long,
    val versionName: String,
    val certSha256: Set<String>,
    val devName: String,
    val storeUrl: String,
    val category: String
)

internal data class RegistryNameEntry(
    val packageName: String,
    val name: String,
    val normalizedName: String
)

internal data class RegistryIndex(
    val byPackage: Map<String, RegistryEntry>,
    val nameIndex: Map<String, List<RegistryNameEntry>>
)
