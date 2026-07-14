package com.resistine.android.network

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class WazuhEnrollmentSecretStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    @Synchronized
    fun loadPassword(): String {
        val encodedIv = prefs.getString(KEY_IV, null) ?: return ""
        val encodedCiphertext = prefs.getString(KEY_CIPHERTEXT, null) ?: return ""
        return runCatching {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val iv = Base64.decode(encodedIv, Base64.NO_WRAP)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(
                cipher.doFinal(Base64.decode(encodedCiphertext, Base64.NO_WRAP)),
                Charsets.UTF_8
            )
        }.onFailure {
            clear()
        }.getOrDefault("")
    }

    @Synchronized
    fun savePassword(password: String) {
        require('\n' !in password && '\r' !in password) {
            "Enrollment password must not contain line breaks"
        }
        require(password.length <= MAX_PASSWORD_CHARS) {
            "Enrollment password must be $MAX_PASSWORD_CHARS characters or fewer"
        }
        if (password.isEmpty()) {
            clear()
            return
        }

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        check(
            prefs.edit()
                .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .putString(KEY_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .commit()
        ) { "Could not store the Wazuh enrollment password" }
    }

    @Synchronized
    fun clear() {
        check(prefs.edit().clear().commit()) {
            "Could not clear the Wazuh enrollment password"
        }
    }

    fun hasPassword(): Boolean = prefs.contains(KEY_IV) && prefs.contains(KEY_CIPHERTEXT)

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

    private companion object {
        const val PREFS_NAME = "wazuh_secure_enrollment"
        const val KEY_IV = "iv"
        const val KEY_CIPHERTEXT = "ciphertext"
        const val KEY_ALIAS = "resistine_wazuh_enrollment_v1"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val MAX_PASSWORD_CHARS = 512
    }
}
