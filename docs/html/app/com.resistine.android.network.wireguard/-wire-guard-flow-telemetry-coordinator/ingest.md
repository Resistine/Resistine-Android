//[app](../../../index.md)/[com.resistine.android.network.wireguard](../index.md)/[WireGuardFlowTelemetryCoordinator](index.md)/[ingest](ingest.md)

# ingest

[androidJvm]\
open override fun [ingest](ingest.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), direction: [PacketDirection](../../com.resistine.android.network.flow/-packet-direction/index.md), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [PacketTelemetryResult](../../com.resistine.android.network.flow/-packet-telemetry-result/index.md)

Ingests a packet into the active pipeline.

#### Return

[PacketTelemetryResult](../../com.resistine.android.network.flow/-packet-telemetry-result/index.md).

#### Parameters

androidJvm

| | |
|---|---|
| packet | Packet byte array. |
| length | Length in bytes. |
| direction | [PacketDirection](../../com.resistine.android.network.flow/-packet-direction/index.md). |
| timestampMillis | Timestamp in milliseconds. |