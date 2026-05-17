package com.resistine.android.security

import android.content.Context
import android.os.Build
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

        try {
            logFile.appendText(logMessage)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write to log file", e)
        }

        // Save to Room database for background upload
        scope.launch {
            database.logDao().insert(LogEntry(timestamp = System.currentTimeMillis(), message = logMessage))
            // scheduleLogUpload() - Disabling worker, WazuhService will handle upload through its persistent connection
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
                // Read file and wrap in a JSON array for nice printing
                "[\n" + logFile.readLines().joinToString(",\n") + "\n]"
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
    }

    fun logDeviceUnlock() {
        log(LogLevel.INFO, context.getString(R.string.wazuh_log_device_unlocked))
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
        val status = if (isConnected) {
            context.getString(R.string.wazuh_network_connected)
        } else {
            context.getString(R.string.wazuh_network_disconnected)
        }
        val data = JSONObject()
        data.put("status", status)
        data.put("type", if (networkType == "None") context.getString(R.string.wazuh_network_none) else networkType)
        log(LogLevel.INFO, context.getString(R.string.wazuh_log_network_state_changed), data)
    }

    private fun logDeviceInfo() {
        val data = JSONObject()
        data.put("manufacturer", Build.MANUFACTURER)
        data.put("model", Build.MODEL)
        data.put("android_version", Build.VERSION.RELEASE)
        data.put("api_level", Build.VERSION.SDK_INT)
        log(LogLevel.NOTICE, context.getString(R.string.wazuh_log_device_info), data)
    }

    companion object {
        private const val TAG = "WazuhAgent"
        @Volatile
        private var INSTANCE: WazuhAgent? = null

        fun getInstance(context: Context): WazuhAgent {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WazuhAgent(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
