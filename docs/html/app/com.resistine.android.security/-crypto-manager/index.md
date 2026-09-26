//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[CryptoManager](index.md)

# CryptoManager

[androidJvm]\
object [CryptoManager](index.md)

Manager handling cryptographic encryption and decryption of sensitive configuration and user data using Android KeyStore and AES-GCM.

## Functions

| Name | Summary |
|---|---|
| [decryptData](decrypt-data.md) | [androidJvm]<br>fun [decryptData](decrypt-data.md)(encryptedBase64: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Decrypts encrypted Base64 string data using AES-GCM. |
| [deleteStoredData](delete-stored-data.md) | [androidJvm]<br>fun [deleteStoredData](delete-stored-data.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Deletes all stored encrypted data and configuration files. |
| [encryptData](encrypt-data.md) | [androidJvm]<br>fun [encryptData](encrypt-data.md)(plainText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Encrypts plaintext data using AES-GCM. |
| [hasRegistrationDefaultConfig](has-registration-default-config.md) | [androidJvm]<br>fun [hasRegistrationDefaultConfig](has-registration-default-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Checks whether a registration default configuration file exists. |
| [isConfigStored](is-config-stored.md) | [androidJvm]<br>fun [isConfigStored](is-config-stored.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Checks whether an encrypted VPN configuration is stored. |
| [loadDecryptedConfig](load-decrypted-config.md) | [androidJvm]<br>fun [loadDecryptedConfig](load-decrypted-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Loads and decrypts the stored VPN configuration. |
| [loadDecryptedEmail](load-decrypted-email.md) | [androidJvm]<br>fun [loadDecryptedEmail](load-decrypted-email.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Loads and decrypts the stored user email address. |
| [loadEncryptedConfig](load-encrypted-config.md) | [androidJvm]<br>fun [loadEncryptedConfig](load-encrypted-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Loads the raw encrypted VPN configuration string. |
| [loadRegistrationDefaultConfig](load-registration-default-config.md) | [androidJvm]<br>fun [loadRegistrationDefaultConfig](load-registration-default-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Loads and decrypts the default registration configuration. |
| [saveEmail](save-email.md) | [androidJvm]<br>fun [saveEmail](save-email.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), email: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))<br>Encrypts and saves the user email address. |
| [saveEncryptedConfig](save-encrypted-config.md) | [androidJvm]<br>fun [saveEncryptedConfig](save-encrypted-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), configText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))<br>Encrypts and saves the VPN configuration text. |
| [saveRegistrationDefaultConfig](save-registration-default-config.md) | [androidJvm]<br>fun [saveRegistrationDefaultConfig](save-registration-default-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), configText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))<br>Encrypts and saves the default registration configuration. |