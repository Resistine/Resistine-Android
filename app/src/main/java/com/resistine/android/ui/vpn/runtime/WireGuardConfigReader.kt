package com.resistine.android.ui.vpn.runtime

import android.content.Context
import android.util.Log
import com.resistine.android.security.CryptoManager
import com.wireguard.config.Config

object WireGuardConfigReader {
    fun loadConfig(context: Context): Config? {
        return try {
            val encryptedConfig = CryptoManager.loadEncryptedConfig(context)
            if (encryptedConfig.isNullOrBlank()) {
                return null
            }
            val decryptedText = CryptoManager.decryptData(encryptedConfig)
            Config.parse(decryptedText.byteInputStream(Charsets.UTF_8))
        } catch (e: Exception) {
            Log.e(TAG, "Could not load the stored WireGuard configuration", e)
            null
        }
    }

    fun loadInterfaceIp(context: Context): String? {
        return loadConfig(context)?.`interface`?.addresses?.firstOrNull()?.address?.hostAddress
    }

    private const val TAG = "WireGuardConfig"
}

