//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[WazuhCrypto](index.md)/[buildPacket](build-packet.md)

# buildPacket

[androidJvm]\
fun [buildPacket](build-packet.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawSharedKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), globalCount: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 1): [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html)

#### Parameters

androidJvm

| | |
|---|---|
| agentId | e.g. &quot;004&quot; |
| rawSharedKey | Only the key itself (the 4th part), e.g. &quot;e10adc3949ba...&quot; |
| globalCount | Message counter (for simplicity you can send an incrementally increasing number) |