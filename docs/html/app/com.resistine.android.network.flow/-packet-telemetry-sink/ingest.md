//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[PacketTelemetrySink](index.md)/[ingest](ingest.md)

# ingest

[androidJvm]\
abstract fun [ingest](ingest.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), direction: [PacketDirection](../-packet-direction/index.md), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [PacketTelemetryResult](../-packet-telemetry-result/index.md)

Ingests a packet buffer.

#### Return

[PacketTelemetryResult](../-packet-telemetry-result/index.md).

#### Parameters

androidJvm

| | |
|---|---|
| packet | Packet byte array. |
| length | Length of the packet in bytes. |
| direction | [PacketDirection](../-packet-direction/index.md) (outbound or inbound). |
| timestampMillis | Timestamp in milliseconds. |