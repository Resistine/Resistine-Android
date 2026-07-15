package com.resistine.android.ui.apps

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat

internal object ScanUtils {

    val sensitiveRuntimePermissions = setOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val permissionLabels = mapOf(
        Manifest.permission.READ_SMS to "Read SMS",
        Manifest.permission.RECEIVE_SMS to "Receive SMS",
        Manifest.permission.SEND_SMS to "Send SMS",
        Manifest.permission.READ_CALL_LOG to "Read call log",
        Manifest.permission.WRITE_CALL_LOG to "Write call log",
        Manifest.permission.RECORD_AUDIO to "Microphone",
        Manifest.permission.READ_CONTACTS to "Read contacts",
        Manifest.permission.WRITE_CONTACTS to "Edit contacts",
        Manifest.permission.READ_PHONE_STATE to "Phone state",
        Manifest.permission.CAMERA to "Camera",
        Manifest.permission.ACCESS_FINE_LOCATION to "Precise location"
    )

    private val recognizedStores = setOf(
        "com.sec.android.app.samsungapps",
        "com.amazon.venezia",
        "com.huawei.appmarket",
        "com.xiaomi.mipicks",
        "org.fdroid.fdroid",
        "com.aurora.store"
    )

    data class DeviceCapabilitySnapshot(
        val enabledAccessibilityPackages: Set<String>,
        val activeDeviceAdminPackages: Set<String>,
        val enabledNotificationListenerPackages: Set<String>
    )

    data class InstallSource(
        val provenance: InstallProvenance,
        val installerPackage: String?
    )

    fun captureDeviceCapabilities(context: Context): DeviceCapabilitySnapshot {
        val accessibilityPackages = runCatching {
            val manager = context.getSystemService(AccessibilityManager::class.java)
            manager?.getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            ).orEmpty().mapNotNull { info ->
                info.resolveInfo?.serviceInfo?.packageName
            }.toSet()
        }.getOrDefault(emptySet())

        val deviceAdminPackages = runCatching {
            context.getSystemService(DevicePolicyManager::class.java)
                ?.activeAdmins.orEmpty()
                .map { it.packageName }
                .toSet()
        }.getOrDefault(emptySet())

        val notificationListenerPackages = runCatching {
            NotificationManagerCompat.getEnabledListenerPackages(context)
        }.getOrDefault(emptySet())

        return DeviceCapabilitySnapshot(
            enabledAccessibilityPackages = accessibilityPackages,
            activeDeviceAdminPackages = deviceAdminPackages,
            enabledNotificationListenerPackages = notificationListenerPackages
        )
    }

    fun resolveInstallSource(context: Context, info: PackageInfo): InstallSource {
        val appInfo = info.applicationInfo
        if (appInfo != null && isSystemApp(appInfo)) {
            return InstallSource(InstallProvenance.SYSTEM, null)
        }

        val packageName = info.packageName
        val sourceInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { context.packageManager.getInstallSourceInfo(packageName) }.getOrNull()
        } else {
            null
        }
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            sourceInfo?.initiatingPackageName ?: sourceInfo?.installingPackageName
        } else {
            @Suppress("DEPRECATION")
            runCatching { context.packageManager.getInstallerPackageName(packageName) }.getOrNull()
        }

        if (installer == PLAY_STORE_PACKAGE) {
            return InstallSource(InstallProvenance.PLAY_STORE, installer)
        }
        if (installer in recognizedStores) {
            return InstallSource(InstallProvenance.RECOGNIZED_STORE, installer)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (sourceInfo?.packageSource) {
                PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE,
                PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE ->
                    return InstallSource(InstallProvenance.LOCAL_OR_ADB, installer)

                PackageInstaller.PACKAGE_SOURCE_STORE ->
                    return InstallSource(InstallProvenance.RECOGNIZED_STORE, installer)
            }
        }

        return if (installer == null) {
            InstallSource(InstallProvenance.UNKNOWN, null)
        } else {
            InstallSource(InstallProvenance.LOCAL_OR_ADB, installer)
        }
    }

    fun grantedSensitivePermissions(info: PackageInfo): List<String> {
        val requested = info.requestedPermissions ?: return emptyList()
        val flags = info.requestedPermissionsFlags
        return requested.mapIndexedNotNull { index, permission ->
            if (permission !in sensitiveRuntimePermissions) return@mapIndexedNotNull null
            val granted = flags != null && flags.size > index &&
                (flags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            permission.takeIf { granted }
        }
    }

    fun declaresPermission(info: PackageInfo, permission: String): Boolean {
        return info.requestedPermissions?.contains(permission) == true
    }

    fun declaresServicePermission(info: PackageInfo, permission: String): Boolean {
        return info.services?.any { service -> service.permission == permission } == true
    }

    fun permissionDisplayName(permission: String): String {
        return permissionLabels[permission] ?: permission.substringAfterLast('.')
            .replace('_', ' ')
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    fun permissionWeight(permission: String): Int {
        return when (permission) {
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG -> 4

            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS -> 3

            else -> 2
        }
    }

    fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return isSystem || isUpdatedSystem
    }

    private const val PLAY_STORE_PACKAGE = "com.android.vending"
}
