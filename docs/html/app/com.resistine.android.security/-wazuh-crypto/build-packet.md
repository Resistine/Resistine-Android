//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[WazuhCrypto](index.md)/[buildPacket](build-packet.md)

# buildPacket

[androidJvm]\
fun [buildPacket](build-packet.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawSharedKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), globalCount: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 1): [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html)

Builds and encrypts a Wazuh protocol packet.

#### Return

Encrypted packet byte array ready for TCP transmission.

#### Parameters

androidJvm

| | |
|---|---|
| agentId | Agent ID e.g. &quot;004&quot;. |
| rawSharedKey | Raw shared agent key. |
| message | Message plaintext string. |
| globalCount | Message sequence counter. |