//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[CryptoManager](index.md)/[encryptData](encrypt-data.md)

# encryptData

[androidJvm]\
fun [encryptData](encrypt-data.md)(plainText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)

Encrypts plaintext data using AES-GCM.

#### Return

Base64-encoded encrypted string containing IV and ciphertext.

#### Parameters

androidJvm

| | |
|---|---|
| plainText | Plaintext string to encrypt. |