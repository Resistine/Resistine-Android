package com.resistine.android.network

import android.content.Context
import android.content.SharedPreferences
import java.security.KeyStore

data class WazuhManagerEndpoint(
    val host: String,
    val authPort: Int,
    val logPort: Int
) {
    init {
        validate()
    }

    internal fun validate() {
        require(host.isNotBlank()) { "Manager host is required" }
        require(host == host.trim()) { "Manager host must not contain surrounding whitespace" }
        require(host.length <= 253) { "Manager host is too long" }
        require(HOST_PATTERN.matches(host)) {
            "Manager host must be an IPv4 address, IPv6 address, or DNS hostname"
        }
        require(authPort in 1..65535) { "Enrollment port must be between 1 and 65535" }
        require(logPort in 1..65535) { "Log port must be between 1 and 65535" }
    }

    private companion object {
        val HOST_PATTERN = Regex("[A-Za-z0-9._:-]+")
    }
}

class WazuhConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        prefs.edit().remove(REMOVED_MANAGER_CA_KEY).apply()
        context.getSharedPreferences(REMOVED_ENROLLMENT_PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        runCatching {
            KeyStore.getInstance(ANDROID_KEY_STORE).apply {
                load(null)
                if (containsAlias(REMOVED_ENROLLMENT_KEY_ALIAS)) {
                    deleteEntry(REMOVED_ENROLLMENT_KEY_ALIAS)
                }
            }
        }
    }

    var serverIp: String
        get() = prefs.getString(KEY_SERVER_IP, "10.49.64.53") ?: "10.49.64.53"
        set(value) = prefs.edit().putString(KEY_SERVER_IP, value).apply()

    var authPort: Int
        get() = prefs.getInt(KEY_AUTH_PORT, 1515)
        set(value) = prefs.edit().putInt(KEY_AUTH_PORT, value).apply()

    var logPort: Int
        get() = prefs.getInt(KEY_LOG_PORT, 1514)
        set(value) = prefs.edit().putInt(KEY_LOG_PORT, value).apply()

    fun endpoint(): WazuhManagerEndpoint {
        return WazuhManagerEndpoint(
            host = serverIp,
            authPort = authPort,
            logPort = logPort
        )
    }

    fun updateEndpoint(endpoint: WazuhManagerEndpoint) {
        prefs.edit()
            .putString(KEY_SERVER_IP, endpoint.host.trim())
            .putInt(KEY_AUTH_PORT, endpoint.authPort)
            .putInt(KEY_LOG_PORT, endpoint.logPort)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "wazuh_config"
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_AUTH_PORT = "auth_port"
        private const val KEY_LOG_PORT = "log_port"
        private const val REMOVED_MANAGER_CA_KEY = "manager_ca_pem"
        private const val REMOVED_ENROLLMENT_PREFS = "wazuh_secure_enrollment"
        private const val REMOVED_ENROLLMENT_KEY_ALIAS = "resistine_wazuh_enrollment_v1"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"

        @Volatile
        private var INSTANCE: WazuhConfigManager? = null

        fun getInstance(context: Context): WazuhConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WazuhConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
