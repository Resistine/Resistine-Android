//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[CryptoManager](index.md)/[decryptData](decrypt-data.md)

# decryptData

[androidJvm]\
fun [decryptData](decrypt-data.md)(encryptedBase64: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)

Decrypts encrypted Base64 string data using AES-GCM.

#### Return

Decrypted plaintext string.

#### Parameters

androidJvm

| | |
|---|---|
| encryptedBase64 | Base64 string containing IV and ciphertext. |