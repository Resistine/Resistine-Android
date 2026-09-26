//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowTelemetryPipeline](index.md)/[ingest](ingest.md)

# ingest

[androidJvm]\
open override fun [ingest](ingest.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), direction: [PacketDirection](../-packet-direction/index.md), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [PacketTelemetryResult](../-packet-telemetry-result/index.md)

Ingests a raw packet into the pipeline.

#### Return

[PacketTelemetryResult](../-packet-telemetry-result/index.md) indicating acceptance or rejection status.

#### Parameters

androidJvm

| | |
|---|---|
| packet | Raw packet byte array. |
| length | Number of valid bytes in the packet. |
| direction | [PacketDirection](../-packet-direction/index.md) (outbound or inbound). |
| timestampMillis | Timestamp in milliseconds. |