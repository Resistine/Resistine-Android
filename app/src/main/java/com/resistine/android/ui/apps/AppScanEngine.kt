package com.resistine.android.ui.apps

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.net.VpnService

internal class AppScanEngine(
    private val context: Context
) {

    fun scan(
        entry: AppEntry,
        capabilities: ScanUtils.DeviceCapabilitySnapshot,
        nowMillis: Long = System.currentTimeMillis()
    ): AppEntry {
        val info = entry.packageInfo
        val packageName = info.packageName
        val appInfo = info.applicationInfo
        val source = ScanUtils.resolveInstallSource(context, info)
        val badges = mutableListOf<Badge>()

        when (source.provenance) {
            InstallProvenance.LOCAL_OR_ADB -> {
                badges += Badge(
                    BadgeType.LOCAL_INSTALL,
                    "Installed outside a recognized store",
                    source.installerPackage?.let { "Installer of record: $it" }
                        ?: "Android reports a local, downloaded, or ADB installation."
                )
            }

            InstallProvenance.UNKNOWN -> {
                badges += Badge(
                    BadgeType.UNKNOWN_SOURCE,
                    "Install source unavailable",
                    "Android could not provide a reliable installer record."
                )
            }

            else -> Unit
        }

        val isDebuggable = appInfo != null &&
            (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable && source.provenance != InstallProvenance.SYSTEM) {
            badges += Badge(
                BadgeType.DEBUGGABLE,
                "Debug build",
                "Debuggable production apps expose a larger attack surface."
            )
        }

        val targetSdk = appInfo?.targetSdkVersion ?: 0
        val targetLag = Build.VERSION.SDK_INT - targetSdk
        if (targetSdk in 1..28 || targetLag >= 5) {
            badges += Badge(
                BadgeType.OLD_TARGET_SDK,
                "Legacy Android target",
                "Targets Android SDK $targetSdk while this device runs SDK ${Build.VERSION.SDK_INT}."
            )
        }

        val sensitivePermissions = ScanUtils.grantedSensitivePermissions(info)
        if (sensitivePermissions.isNotEmpty()) {
            val permissionNames = sensitivePermissions.map(ScanUtils::permissionDisplayName)
            badges += Badge(
                BadgeType.SENSITIVE_PERMISSION,
                "Sensitive access (${sensitivePermissions.size})",
                "Currently granted:\n${permissionNames.joinToString("\n")}"
            )
        }

        val accessibilityEnabled = packageName in capabilities.enabledAccessibilityPackages
        if (accessibilityEnabled) {
            badges += Badge(
                BadgeType.ACCESSIBILITY_ENABLED,
                "Accessibility control enabled",
                "This app has an enabled accessibility service and may observe or control on-screen interactions."
            )
        }

        val deviceAdminActive = packageName in capabilities.activeDeviceAdminPackages
        if (deviceAdminActive) {
            badges += Badge(
                BadgeType.DEVICE_ADMIN,
                "Device administrator active",
                "This app currently holds device-administrator privileges."
            )
        }

        val notificationAccess = packageName in capabilities.enabledNotificationListenerPackages
        if (notificationAccess) {
            badges += Badge(
                BadgeType.NOTIFICATION_ACCESS,
                "Notification access enabled",
                "This app can read notifications posted by other apps."
            )
        }

        val canRequestInstalls = ScanUtils.declaresPermission(info, Manifest.permission.REQUEST_INSTALL_PACKAGES)
        if (canRequestInstalls) {
            badges += Badge(
                BadgeType.INSTALLER_CAPABILITY,
                "Can request app installs",
                "The app declares the ability to send APKs to Android's package installer."
            )
        }

        val canOverlay = ScanUtils.declaresPermission(info, Manifest.permission.SYSTEM_ALERT_WINDOW)
        if (canOverlay) {
            badges += Badge(
                BadgeType.OVERLAY_CAPABILITY,
                "Requests overlay access",
                "The app declares the ability to display content over other apps. Android controls the active grant separately."
            )
        }

        val declaresVpn = ScanUtils.declaresServicePermission(info, Manifest.permission.BIND_VPN_SERVICE) ||
            info.services?.any { it.name == VpnService::class.java.name } == true
        if (declaresVpn) {
            badges += Badge(
                BadgeType.VPN_CAPABILITY,
                "VPN provider",
                "The app declares an Android VPN service. This is informational unless combined with other concerns."
            )
        }

        val risk = AppRiskScorer.score(
            AppRiskInput(
                provenance = source.provenance,
                targetSdk = targetSdk,
                deviceSdk = Build.VERSION.SDK_INT,
                isDebuggable = isDebuggable,
                sensitivePermissionWeight = sensitivePermissions.sumOf(ScanUtils::permissionWeight),
                accessibilityEnabled = accessibilityEnabled,
                deviceAdminActive = deviceAdminActive,
                notificationAccessEnabled = notificationAccess,
                declaresInstallerCapability = canRequestInstalls,
                declaresOverlayCapability = canOverlay
            )
        )
        val identityConfidence = when (source.provenance) {
            InstallProvenance.SYSTEM -> AppIdentityConfidence.SYSTEM_VERIFIED
            InstallProvenance.PLAY_STORE,
            InstallProvenance.RECOGNIZED_STORE -> AppIdentityConfidence.STORE_RECORDED
            else -> AppIdentityConfidence.UNVERIFIED
        }

        return entry.copy(
            scanResult = AppScanResult(
                score = risk.value,
                verdict = risk.verdict,
                badges = badges,
                highRiskPermissions = sensitivePermissions,
                provenance = source.provenance,
                identityConfidence = identityConfidence,
                scannedAtMillis = nowMillis
            )
        )
    }
}
