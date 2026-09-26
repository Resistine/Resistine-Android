//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[WazuhCrypto](index.md)

# WazuhCrypto

[androidJvm]\
object [WazuhCrypto](index.md)

Cryptographic utility for building and encrypting Wazuh protocol packets (AES-256-CBC, Zlib compression, MD5 hashing).

## Functions

| Name | Summary |
|---|---|
| [buildPacket](build-packet.md) | [androidJvm]<br>fun [buildPacket](build-packet.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawSharedKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), globalCount: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 1): [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html)<br>Builds and encrypts a Wazuh protocol packet. |