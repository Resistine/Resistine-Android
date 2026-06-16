//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[CryptoManager](index.md)

# CryptoManager

[androidJvm]\
object [CryptoManager](index.md)

## Functions

| Name | Summary |
|---|---|
| [decryptData](decrypt-data.md) | [androidJvm]<br>fun [decryptData](decrypt-data.md)(encryptedBase64: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [deleteStoredData](delete-stored-data.md) | [androidJvm]<br>fun [deleteStoredData](delete-stored-data.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |
| [encryptData](encrypt-data.md) | [androidJvm]<br>fun [encryptData](encrypt-data.md)(plainText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [isConfigStored](is-config-stored.md) | [androidJvm]<br>fun [isConfigStored](is-config-stored.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [loadDecryptedEmail](load-decrypted-email.md) | [androidJvm]<br>fun [loadDecryptedEmail](load-decrypted-email.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [loadEncryptedConfig](load-encrypted-config.md) | [androidJvm]<br>fun [loadEncryptedConfig](load-encrypted-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [saveEmail](save-email.md) | [androidJvm]<br>fun [saveEmail](save-email.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), email: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |
| [saveEncryptedConfig](save-encrypted-config.md) | [androidJvm]<br>fun [saveEncryptedConfig](save-encrypted-config.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), configText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |