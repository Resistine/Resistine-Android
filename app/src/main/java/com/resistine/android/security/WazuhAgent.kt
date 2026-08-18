package com.resistine.android.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.resistine.android.R
import org.json.JSONObject
import com.resistine.android.database.AppDatabase
import com.resistine.android.database.LogEntry
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Syslog severity levels (RFC 5424)
enum class LogLevel(val severity: Int) {
    EMERGENCY(0), // System is unusable
    ALERT(1),     // Action must be taken immediately
    CRITICAL(2),  // Critical conditions
    ERROR(3),     // Error conditions
    WARN(4),    // Warning conditions
    NOTICE(5),    // Normal but significant condition
    INFO(6),      // Informational messages
    DEBUG(7)      // Debug-level messages
}

class WazuhAgent(private val context: Context) {

    private val logFile: File
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        val logDir = File(context.filesDir, "wazuh_logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        logFile = File(logDir, "agent.log")
        Log.i(TAG, "Log file path: ${logFile.absolutePath}")
    }

    fun log(level: LogLevel, message: String, data: JSONObject? = null) {
        val timestamp = dateFormat.format(Date())

        val logJson = JSONObject()
        logJson.put("timestamp", timestamp)
        logJson.put("level", level.name)
        logJson.put("severity", level.severity)
        logJson.put("message", message)
        data?.let {
            logJson.put("data", it)
        }

        val logMessage = logJson.toString() + "\n"

        // Log to Logcat
        val logcatMessage = "Wazuh Agent: $logMessage"
        when (level) {
            LogLevel.INFO, LogLevel.NOTICE -> Log.i(TAG, logcatMessage)
            LogLevel.WARN -> Log.w(TAG, logcatMessage)
            LogLevel.ERROR, LogLevel.CRITICAL, LogLevel.ALERT, LogLevel.EMERGENCY -> Log.e(TAG, logcatMessage)
            LogLevel.DEBUG -> Log.d(TAG, logcatMessage)
        }



        // Save to Room database for background upload
        scope.launch {
            try {
                rotateLogIfNeeded()
                logFile.appendText(logMessage)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to write to log file", e)
            }
            try {
                database.logDao().insertBounded(
                    LogEntry(timestamp = System.currentTimeMillis(), message = logMessage)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert log entry", e)
            }

            // scheduleLogUpload() - Disabling worker, WazuhService will handle upload through its persistent connection
        }
    }

    private fun rotateLogIfNeeded() {
        if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
            Log.i(TAG, "Rotating log file (size: ${logFile.length()})")
            val lines = logFile.readLines()
            if (lines.size > 500) {
                // Keep only the last 200 lines if the file is getting too big
                val lastLines = lines.takeLast(200)
                logFile.writeText(lastLines.joinToString("\n") + "\n")
            } else {
                // If not many lines but large size (unlikely for text), just clear it
                logFile.writeText("")
            }
        }
    }

    private fun scheduleLogUpload() {
        // Legacy worker-based upload disabled to prevent connection conflicts
    }

    fun getLogFilePath(): String {
        return logFile.absolutePath
    }

    fun readLogs(): String {
        return try {
            if (logFile.exists()) {
                // Read last 200 lines to prevent OOM
                val lines = logFile.readLines()
                val lastLines = if (lines.size > 200) lines.takeLast(200) else lines
                "[\n" + lastLines.joinToString(",\n") + "\n]"
            } else {
                context.getString(R.string.wazuh_log_file_not_found)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read log file", e)
            context.getString(R.string.wazuh_error_reading_log_file)
        }
    }

    fun logAppStart() {
        log(LogLevel.NOTICE, context.getString(R.string.wazuh_log_app_started))
        logDeviceInfo()
        logSystemSecurityPosture()
    }

    fun logDeviceUnlock() {
        log(LogLevel.INFO, context.getString(R.string.wazuh_log_device_unlocked))
    }

    fun logScreenState(isOn: Boolean) {
        val data = JSONObject()
        data.put("screen_on", isOn)
        val message = context.getString(if (isOn) R.string.wazuh_screen_on else R.string.wazuh_screen_off)
        log(LogLevel.DEBUG, message, data)
    }

    fun logPackageEvent(action: String, packageName: String) {
        val data = JSONObject()
        data.put("package_name", packageName)
        data.put("action", action)
        log(LogLevel.NOTICE, context.getString(R.string.wazuh_log_package_event, action, packageName), data)
    }

    fun logSystemSecurityPosture() {
        val data = JSONObject()
        val adbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
        val developmentSettingsEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
        val installNonMarketApps = Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) != 0

        data.put("adb_enabled", adbEnabled)
        data.put("development_settings_enabled", developmentSettingsEnabled)
        data.put("install_non_market_apps", installNonMarketApps)
        data.put("is_rooted", isDeviceRooted())

        log(LogLevel.NOTICE, context.getString(R.string.wazuh_log_system_posture), data)
    }

    fun logBatteryState(level: Int, scale: Int, isCharging: Boolean) {
        val batteryPct = level * 100 / scale.toFloat()
        val status = if (isCharging) {
            context.getString(R.string.wazuh_battery_charging)
        } else {
            context.getString(R.string.wazuh_battery_discharging)
        }
        val data = JSONObject()
        data.put("percent", batteryPct.toInt())
        data.put("status", status)
        log(LogLevel.INFO, context.getString(R.string.wazuh_log_battery_state_changed), data)
    }

    fun logNetworkState(isConnected: Boolean, networkType: String) {
        val vpnActive = isVpnActive()
        val status = if (isConnected) {
            context.getString(R.string.wazuh_network_connected)
        } else {
            context.getString(R.string.wazuh_network_disconnected)
        }
        val data = JSONObject()
        data.put("status", status)
        data.put("type", if (networkType == "None") context.getString(R.string.wazuh_network_none) else networkType)
        data.put("vpn_active", vpnActive)
        log(LogLevel.INFO, context.getString(R.string.wazuh_log_network_state_changed), data)
    }

    private fun isVpnActive(): Boolean {
        return try {
            java.net.NetworkInterface.getNetworkInterfaces()?.asSequence()?.any {
                it.isUp && (it.name.contains("tun") || it.name.contains("wg") || it.name.contains("wireguard"))
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun logDeviceInfo() {
        val data = JSONObject()
        data.put("manufacturer", Build.MANUFACTURER)
        data.put("model", Build.MODEL)
        data.put("android_version", Build.VERSION.RELEASE)
        data.put("api_level", Build.VERSION.SDK_INT)
        data.put("is_rooted", isDeviceRooted())
        log(LogLevel.NOTICE, context.getString(R.string.wazuh_log_device_info), data)
    }

    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        return try {
            Runtime.getRuntime().exec("which su").inputStream.bufferedReader().readLine() != null
        } catch (t: Throwable) {
            false
        }
    }

    companion object {
        private const val TAG = "WazuhAgent"
        private const val MAX_LOG_FILE_SIZE = 1 * 1024 * 1024 // 1MB
        @Volatile
        private var INSTANCE: WazuhAgent? = null

        fun getInstance(context: Context): WazuhAgent {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WazuhAgent(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
