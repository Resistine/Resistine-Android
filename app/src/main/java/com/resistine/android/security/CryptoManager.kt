package com.resistine.android.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manager handling cryptographic encryption and decryption of sensitive configuration and user data
 * using Android KeyStore and AES-GCM.
 */
object CryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "VpnAesKey"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val VPN_CONFIG_FILENAME = "vpn_config.enc"
    private const val REGISTRATION_CONFIG_FILENAME = "vpn_registration_default.enc"
    private const val EMAIL_FILENAME = "user_email.enc"

    /**
     * Retrieves or generates the AES secret key from the Android KeyStore.
     *
     * @return [SecretKey] instance.
     */
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) {
            return existingKey.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts plaintext data using AES-GCM.
     *
     * @param plainText Plaintext string to encrypt.
     * @return Base64-encoded encrypted string containing IV and ciphertext.
     */
    fun encryptData(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Decrypts encrypted Base64 string data using AES-GCM.
     *
     * @param encryptedBase64 Base64 string containing IV and ciphertext.
     * @return Decrypted plaintext string.
     */
    fun decryptData(encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until 12)
        val encrypted = combined.sliceArray(12 until combined.size)

        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Encrypts and saves the VPN configuration text.
     *
     * @param context Application context.
     * @param configText Plaintext VPN configuration string.
     */
    fun saveEncryptedConfig(context: Context, configText: String) {
        val encrypted = encryptData(configText)
        val file = File(context.filesDir, VPN_CONFIG_FILENAME)
        file.writeText(encrypted)
    }

    /**
     * Encrypts and saves the default registration configuration.
     *
     * @param context Application context.
     * @param configText Plaintext configuration string.
     */
    fun saveRegistrationDefaultConfig(context: Context, configText: String) {
        val encrypted = encryptData(configText)
        File(context.filesDir, REGISTRATION_CONFIG_FILENAME).writeText(encrypted)
    }

    /**
     * Loads and decrypts the stored VPN configuration.
     *
     * @param context Application context.
     * @return Decrypted configuration string, or null if not found.
     */
    fun loadDecryptedConfig(context: Context): String? =
        loadEncryptedFile(context, VPN_CONFIG_FILENAME)

    /**
     * Loads and decrypts the default registration configuration.
     *
     * @param context Application context.
     * @return Decrypted configuration string, or null if not found.
     */
    fun loadRegistrationDefaultConfig(context: Context): String? =
        loadEncryptedFile(context, REGISTRATION_CONFIG_FILENAME)

    /**
     * Checks whether a registration default configuration file exists.
     *
     * @param context Application context.
     * @return True if exists; false otherwise.
     */
    fun hasRegistrationDefaultConfig(context: Context): Boolean =
        File(context.filesDir, REGISTRATION_CONFIG_FILENAME).exists()

    /**
     * Checks whether an encrypted VPN configuration is stored.
     *
     * @param context Application context.
     * @return True if stored; false otherwise.
     */
    fun isConfigStored(context: Context): Boolean {
        val file = File(context.filesDir, VPN_CONFIG_FILENAME)
        return file.exists()
    }

    /**
     * Loads the raw encrypted VPN configuration string.
     *
     * @param context Application context.
     * @return Encrypted string, or null if not found.
     */
    fun loadEncryptedConfig(context: Context): String? {
        val file = File(context.filesDir, VPN_CONFIG_FILENAME)
        return if (file.exists()) file.readText() else null
    }

    /**
     * Loads an encrypted file and decrypts its contents.
     *
     * @param context Application context.
     * @param fileName File name.
     * @return Decrypted string or null.
     */
    private fun loadEncryptedFile(context: Context, fileName: String): String? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null
        return runCatching { decryptData(file.readText()) }.getOrNull()
    }

    /**
     * Encrypts and saves the user email address.
     *
     * @param context Application context.
     * @param email User email string.
     */
    fun saveEmail(context: Context, email: String) {
        val encryptedEmail = encryptData(email)
        val file = File(context.filesDir, EMAIL_FILENAME)
        file.writeText(encryptedEmail)
    }

    /**
     * Loads and decrypts the stored user email address.
     *
     * @param context Application context.
     * @return Decrypted email string, or null if not found.
     */
    fun loadDecryptedEmail(context: Context): String? {
        val file = File(context.filesDir, EMAIL_FILENAME)
        if (!file.exists()) return null
        return try {
            val encryptedEmail = file.readText()
            decryptData(encryptedEmail)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes all stored encrypted data and configuration files.
     *
     * @param context Application context.
     */
    fun deleteStoredData(context: Context) {
        val vpnFile = File(context.filesDir, VPN_CONFIG_FILENAME)
        if (vpnFile.exists()) {
            vpnFile.delete()
        }
        val emailFile = File(context.filesDir, EMAIL_FILENAME)
        if (emailFile.exists()) {
            emailFile.delete()
        }
        val registrationConfigFile = File(context.filesDir, REGISTRATION_CONFIG_FILENAME)
        if (registrationConfigFile.exists()) {
            registrationConfigFile.delete()
        }
    }
}
