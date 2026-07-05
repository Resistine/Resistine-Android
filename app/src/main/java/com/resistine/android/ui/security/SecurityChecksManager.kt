package com.resistine.android.ui.security

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import java.util.concurrent.TimeUnit

/**
 * Utility class to perform system security inspections.
 * 
 * It gathers information about OS updates, lock screen settings,
 * developer options, and radio states.
 */
class SecurityChecksManager(private val context: Context) {

    /**
     * Executes all security checks and returns a list of items.
     */
    fun performAllChecks(): List<SecurityCheckItem> {
        return mutableListOf<SecurityCheckItem>().apply {
            addAll(getUpdateChecks())
            addAll(getLockChecks())
            addAll(getMalwareChecks())
            addAll(getRadioChecks())
            addAll(getLocationChecks())
        }
    }

    /**
     * Calculates an overall security score from 0 to 100 based on check results.
     */
    fun calculateScore(checks: List<SecurityCheckItem>): Int {
        if (checks.isEmpty()) return 100
        val totalPenalty = checks.sumOf { it.status.getPenalty() }
        return (100 - totalPenalty).coerceIn(0, 100)
    }

    private fun getUpdateChecks(): List<SecurityCheckItem> {
        val list = mutableListOf<SecurityCheckItem>()

        // 1. Android Security Patch
        val patchDate = Build.VERSION.SECURITY_PATCH
        val status = try {
            if (patchDate.isNullOrEmpty()) {
                SecurityStatus.INFO
            } else {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val date = sdf.parse(patchDate)
                val diff = System.currentTimeMillis() - (date?.time ?: 0)
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                when {
                    days < 90 -> SecurityStatus.SAFE
                    days < 180 -> SecurityStatus.WARNING
                    else -> SecurityStatus.DANGER
                }
            }
        } catch (e: Exception) {
            SecurityStatus.INFO
        }

        list.add(SecurityCheckItem(
            "sec_patch",
            SecurityCategory.UPDATES,
            "Security Patch",
            patchDate,
            status,
            "Target: Not older than 90 days."
        ))

        // 2. Android Version
        list.add(SecurityCheckItem(
            "os_version",
            SecurityCategory.UPDATES,
            "Android OS",
            "Version ${Build.VERSION.RELEASE}",
            SecurityStatus.INFO,
            "Check for system updates in device settings."
        ))

        return list
    }

    private fun getLockChecks(): List<SecurityCheckItem> {
        val list = mutableListOf<SecurityCheckItem>()
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        // 1. Secure Screen Lock
        val isSecure = km.isDeviceSecure
        list.add(SecurityCheckItem(
            "lock_secure",
            SecurityCategory.DEVICE_LOCK,
            "Secure Lock",
            if (isSecure) "On" else "Off",
            if (isSecure) SecurityStatus.SAFE else SecurityStatus.DANGER,
            "Mandatory for protecting device data."
        ))

        // 2. Lock Quality
        val lockQuality = dpm.getPasswordQuality(null)
        val lockValue = when (lockQuality) {
            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING -> "Pattern/Gesture"
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC, 
            DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX -> "PIN"
            DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC,
            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX -> "Password"
            else -> if (isSecure) "Set" else "None"
        }
        list.add(SecurityCheckItem(
            "lock_quality",
            SecurityCategory.DEVICE_LOCK,
            "Lock Quality",
            lockValue,
            if (lockQuality >= DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) SecurityStatus.SAFE else SecurityStatus.WARNING,
            "Target: 6+ digit PIN or Password."
        ))

        // 3. Biometrics
        val biometricManager = BiometricManager.from(context)
        val biometricStatus = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Enrolled"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Not Enrolled"
            else -> "Unavailable"
        }
        list.add(SecurityCheckItem(
            "biometrics",
            SecurityCategory.DEVICE_LOCK,
            "Biometrics",
            biometricStatus,
            if (biometricStatus == "Enrolled") SecurityStatus.SAFE else SecurityStatus.INFO,
            "Convenient, but use strong PIN/Password as primary."
        ))

        // 4. Auto-lock timeout
        val timeout = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 0)
        val timeoutSec = timeout / 1000
        list.add(SecurityCheckItem(
            "lock_timeout",
            SecurityCategory.DEVICE_LOCK,
            "Auto-lock",
            if (timeoutSec > 60) "${timeoutSec / 60} min" else "$timeoutSec sec",
            if (timeoutSec <= 120) SecurityStatus.SAFE else SecurityStatus.WARNING,
            "Target: Short timeout (e.g., 30s - 2min)."
        ))

        return list
    }

    private fun getMalwareChecks(): List<SecurityCheckItem> {
        val list = mutableListOf<SecurityCheckItem>()

        // 1. Developer Options
        val devOptions = Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
        list.add(SecurityCheckItem(
            "dev_options",
            SecurityCategory.MALWARE_PROTECTION,
            "Developer Options",
            if (devOptions) "Enabled" else "Disabled",
            if (devOptions) SecurityStatus.WARNING else SecurityStatus.SAFE,
            "Recommended to keep Disabled."
        ))

        // 2. USB Debugging
        val usbDebug = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
        list.add(SecurityCheckItem(
            "usb_debug",
            SecurityCategory.MALWARE_PROTECTION,
            "USB Debugging",
            if (usbDebug) "Enabled" else "Disabled",
            if (usbDebug) SecurityStatus.DANGER else SecurityStatus.SAFE,
            "Major risk if left on."
        ))

        // 3. Unknown Sources (Legacy check, modern is per-app)
        @Suppress("DEPRECATION")
        val unknownSources = Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) != 0
        list.add(SecurityCheckItem(
            "unknown_sources",
            SecurityCategory.MALWARE_PROTECTION,
            "Unknown Apps",
            if (unknownSources) "Allowed" else "Restricted",
            if (unknownSources) SecurityStatus.DANGER else SecurityStatus.SAFE,
            "Prevents installation of apps outside Play Store."
        ))

        return list
    }

    private fun getRadioChecks(): List<SecurityCheckItem> {
        val list = mutableListOf<SecurityCheckItem>()

        // 1. Bluetooth
        @Suppress("DEPRECATION")
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val btEnabled = btAdapter?.isEnabled == true
        list.add(SecurityCheckItem(
            "bt_state",
            SecurityCategory.RADIO_SURFACE,
            "Bluetooth",
            if (btEnabled) "On" else "Off",
            if (btEnabled) SecurityStatus.WARNING else SecurityStatus.SAFE,
            "Turn off when not in use."
        ))

        // 2. NFC
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        val nfcEnabled = nfcAdapter?.isEnabled == true
        list.add(SecurityCheckItem(
            "nfc_state",
            SecurityCategory.RADIO_SURFACE,
            "NFC",
            if (nfcEnabled) "On" else "Off",
            SecurityStatus.INFO,
            "Can be turned off for maximum security."
        ))

        // 3. Wi-Fi Scanning
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        val wifiScanning = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wifiManager.isScanAlwaysAvailable
        } else {
            false
        }
        list.add(SecurityCheckItem(
            "wifi_scan",
            SecurityCategory.RADIO_SURFACE,
            "Wi-Fi Scanning",
            if (wifiScanning) "On" else "Off",
            if (wifiScanning) SecurityStatus.WARNING else SecurityStatus.SAFE,
            "Can leak location if On."
        ))

        return list
    }

    private fun getLocationChecks(): List<SecurityCheckItem> {
        val list = mutableListOf<SecurityCheckItem>()
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 1. Global Location
        val locationEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE, 0) != 0
        }
        list.add(SecurityCheckItem(
            "location_global",
            SecurityCategory.LOCATION_PRIVACY,
            "Global Location",
            if (locationEnabled) "On" else "Off",
            SecurityStatus.INFO,
            "Minimize use to protect privacy."
        ))

        return list
    }
}
