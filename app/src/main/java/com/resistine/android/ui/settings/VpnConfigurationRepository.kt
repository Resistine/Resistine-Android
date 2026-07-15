package com.resistine.android.ui.settings

import android.content.Context
import com.resistine.android.security.CryptoManager
import com.wireguard.config.Config

class VpnConfigurationRepository(context: Context) {
    private val appContext = context.applicationContext

    init {
        if (!CryptoManager.hasRegistrationDefaultConfig(appContext)) {
            CryptoManager.loadDecryptedConfig(appContext)?.let { current ->
                CryptoManager.saveRegistrationDefaultConfig(appContext, current)
            }
        }
    }

    fun loadActive(): String = CryptoManager.loadDecryptedConfig(appContext).orEmpty()

    fun hasRegistrationDefault(): Boolean =
        CryptoManager.hasRegistrationDefaultConfig(appContext)

    fun save(configText: String) {
        val normalized = validate(configText)
        CryptoManager.saveEncryptedConfig(appContext, normalized)
    }

    fun restoreRegistrationDefault(): String {
        val original = CryptoManager.loadRegistrationDefaultConfig(appContext)
            ?: error("No registration configuration is available to restore.")
        val normalized = validate(original)
        CryptoManager.saveEncryptedConfig(appContext, normalized)
        return normalized
    }

    private fun validate(raw: String): String {
        val normalized = raw.trim().plus("\n")
        require(normalized.isNotBlank()) { "Configuration cannot be empty." }
        Config.parse(normalized.byteInputStream(Charsets.UTF_8))
        return normalized
    }
}
