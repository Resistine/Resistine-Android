package com.resistine.android.network

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Credentials for a registered Wazuh agent.
 *
 * @property agentId Unique agent ID assigned by the manager.
 * @property agentKey Secret key assigned to the agent.
 * @property agentName Human-readable agent name.
 */
data class WazuhAgentCredentials(
    val agentId: String,
    val agentKey: String,
    val agentName: String
) {
    init {
        require(agentId.isNotBlank()) { "Agent ID is required" }
        require(agentKey.isNotBlank()) { "Agent key is required" }
        require(agentName.isNotBlank()) { "Agent name is required" }
    }
}

/**
 * Secure store for persisting encrypted Wazuh agent credentials using Android KeyStore and AES-GCM.
 *
 * @param context Application context.
 */
class WazuhCredentialStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val legacyPrefs = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Loads and decrypts stored Wazuh agent credentials.
     *
     * @return [WazuhAgentCredentials] if available and valid, or null.
     */
    @Synchronized
    fun load(): WazuhAgentCredentials? {
        val encrypted = prefs.getString(KEY_CIPHERTEXT, null)
        val encodedIv = prefs.getString(KEY_IV, null)
        if (encrypted != null && encodedIv != null) {
            return runCatching {
                decrypt(encodedIv, encrypted)
            }.onFailure {
                clearEncryptedValues()
            }.getOrNull()
        }

        val legacy = loadLegacy() ?: return null
        save(legacy)
        legacyPrefs.edit().clear().apply()
        return legacy
    }

    /**
     * Encrypts and saves Wazuh agent credentials.
     *
     * @param credentials The [WazuhAgentCredentials] to persist.
     */
    @Synchronized
    fun save(credentials: WazuhAgentCredentials) {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(encode(credentials))
        check(
            prefs.edit()
                .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .putString(KEY_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .commit()
        ) { "Could not store Wazuh agent credentials" }
        legacyPrefs.edit().clear().apply()
    }

    /**
     * Clears all stored Wazuh credentials.
     */
    @Synchronized
    fun clear() {
        clearEncryptedValues()
        legacyPrefs.edit().clear().apply()
    }

    /**
     * Loads legacy unencrypted credentials.
     *
     * @return [WazuhAgentCredentials] if present in legacy prefs.
     */
    private fun loadLegacy(): WazuhAgentCredentials? {
        val id = legacyPrefs.getString(LEGACY_AGENT_ID, null)
        val key = legacyPrefs.getString(LEGACY_AGENT_KEY, null)
        val name = legacyPrefs.getString(LEGACY_AGENT_NAME, null)
        if (id.isNullOrBlank() || key.isNullOrBlank() || name.isNullOrBlank()) return null
        return WazuhAgentCredentials(id, key, name)
    }

    /**
     * Decrypts ciphertext credentials using AES-GCM.
     *
     * @param encodedIv Base64-encoded initialization vector.
     * @param encrypted Base64-encoded ciphertext.
     * @return Decrypted [WazuhAgentCredentials].
     */
    private fun decrypt(encodedIv: String, encrypted: String): WazuhAgentCredentials {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        val plaintext = cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP))
        return decode(plaintext)
    }

    /**
     * Retrieves or generates an AES encryption key stored in the Android KeyStore.
     *
     * @return [SecretKey] instance.
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            generateKey()
        }
    }

    /**
     * Serializes agent credentials into a byte array.
     *
     * @param credentials The credentials to encode.
     * @return Byte array representation.
     */
    private fun encode(credentials: WazuhAgentCredentials): ByteArray {
        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeUTF(credentials.agentId)
                output.writeUTF(credentials.agentKey)
                output.writeUTF(credentials.agentName)
            }
            bytes.toByteArray()
        }
    }

    /**
     * Deserializes agent credentials from a byte array.
     *
     * @param bytes Byte array to decode.
     * @return Decoded [WazuhAgentCredentials].
     */
    private fun decode(bytes: ByteArray): WazuhAgentCredentials {
        return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            WazuhAgentCredentials(
                agentId = input.readUTF(),
                agentKey = input.readUTF(),
                agentName = input.readUTF()
            )
        }
    }

    /**
     * Clears encrypted credential preferences.
     */
    private fun clearEncryptedValues() {
        check(prefs.edit().clear().commit()) { "Could not clear Wazuh agent credentials" }
    }

    companion object {
        private const val PREFS_NAME = "wazuh_secure_credentials"
        private const val LEGACY_PREFS_NAME = "wazuh_prefs"
        private const val LEGACY_AGENT_ID = "agent_id"
        private const val LEGACY_AGENT_KEY = "agent_key"
        private const val LEGACY_AGENT_NAME = "agent_name"
        private const val KEY_IV = "iv"
        private const val KEY_CIPHERTEXT = "ciphertext"
        private const val KEY_ALIAS = "resistine_wazuh_credentials_v1"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
