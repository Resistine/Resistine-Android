//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[TunPacketParser](index.md)/[parse](parse.md)

# parse

[androidJvm]\
fun [parse](parse.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), direction: [PacketDirection](../-packet-direction/index.md)? = null): [PacketMetadata](../-packet-metadata/index.md)?

Parses a raw IP packet byte array into [PacketMetadata](../-packet-metadata/index.md).

#### Return

[PacketMetadata](../-packet-metadata/index.md) if parsing succeeds; null otherwise.

#### Parameters

androidJvm

| | |
|---|---|
| packet | Packet byte array. |
| length | Number of valid bytes in the packet. |
| timestampMillis | Timestamp in milliseconds. |
| direction | Optional explicit packet direction. |