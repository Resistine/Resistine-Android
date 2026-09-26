//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[PacketTelemetrySink](index.md)

# PacketTelemetrySink

fun interface [PacketTelemetrySink](index.md)

Receives plaintext IP packets at a VPN capture boundary.

A WireGuard integration must call this before outbound encryption and after inbound decryption. Buffers may be reused by the caller after this method returns.

#### Inheritors

| |
|---|
| [FlowTelemetryPipeline](../-flow-telemetry-pipeline/index.md) |
| [WireGuardFlowTelemetryCoordinator](../../com.resistine.android.network.wireguard/-wire-guard-flow-telemetry-coordinator/index.md) |

## Functions

| Name | Summary |
|---|---|
| [ingest](ingest.md) | [androidJvm]<br>abstract fun [ingest](ingest.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), direction: [PacketDirection](../-packet-direction/index.md), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [PacketTelemetryResult](../-packet-telemetry-result/index.md)<br>Ingests a packet buffer. |