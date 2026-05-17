package com.resistine.android.network

import android.content.Context
import android.content.SharedPreferences

class WazuhConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var serverIp: String
        get() = prefs.getString(KEY_SERVER_IP, "10.0.0.24") ?: "10.0.0.24"
        set(value) = prefs.edit().putString(KEY_SERVER_IP, value).apply()

    var authPort: Int
        get() = prefs.getInt(KEY_AUTH_PORT, 1515)
        set(value) = prefs.edit().putInt(KEY_AUTH_PORT, value).apply()

    var logPort: Int
        get() = prefs.getInt(KEY_LOG_PORT, 1514)
        set(value) = prefs.edit().putInt(KEY_LOG_PORT, value).apply()

    companion object {
        private const val PREFS_NAME = "wazuh_config"
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_AUTH_PORT = "auth_port"
        private const val KEY_LOG_PORT = "log_port"

        @Volatile
        private var INSTANCE: WazuhConfigManager? = null

        fun getInstance(context: Context): WazuhConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WazuhConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
