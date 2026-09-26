//[app](../../../index.md)/[com.resistine.android.network.wireguard](../index.md)/[WireGuardFlowTelemetryCoordinator](index.md)

# WireGuardFlowTelemetryCoordinator

class [WireGuardFlowTelemetryCoordinator](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), onHealthMessage: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {}) : [PacketTelemetrySink](../../com.resistine.android.network.flow/-packet-telemetry-sink/index.md), [TelemetryGoBackend.TelemetryHealthListener](../../com.wireguard.android.backend/-telemetry-go-backend/-telemetry-health-listener/index.md)

Coordinator bridging WireGuard backend telemetry callbacks and packet ingestion with the flow telemetry pipeline, local database queue, and Wazuh delivery service.

#### Parameters

androidJvm

| | |
|---|---|
| context | Application context. |
| onHealthMessage | Callback invoked with health messages. |

## Constructors

| | |
|---|---|
| [WireGuardFlowTelemetryCoordinator](-wire-guard-flow-telemetry-coordinator.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), onHealthMessage: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {}) |

## Functions

| Name | Summary |
|---|---|
| [ingest](ingest.md) | [androidJvm]<br>open override fun [ingest](ingest.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), direction: [PacketDirection](../../com.resistine.android.network.flow/-packet-direction/index.md), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [PacketTelemetryResult](../../com.resistine.android.network.flow/-packet-telemetry-result/index.md)<br>Ingests a packet into the active pipeline. |
| [onNativePacketDrops](on-native-packet-drops.md) | [androidJvm]<br>open override fun [onNativePacketDrops](on-native-packet-drops.md)(count: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html))<br>Callback invoked when native packets are dropped. |
| [onNativeQueueStats](on-native-queue-stats.md) | [androidJvm]<br>open override fun [onNativeQueueStats](on-native-queue-stats.md)(depth: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), highWater: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html))<br>Callback invoked with native queue statistics. |
| [onTelemetryReaderFailure](on-telemetry-reader-failure.md) | [androidJvm]<br>open override fun [onTelemetryReaderFailure](on-telemetry-reader-failure.md)(error: [Throwable](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-throwable/index.html))<br>Callback invoked when the telemetry reader fails. |
| [onTunnelConfirmed](on-tunnel-confirmed.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [onTunnelConfirmed](on-tunnel-confirmed.md)()<br>Called when the WireGuard tunnel handshake has been confirmed. |
| [snapshot](snapshot.md) | [androidJvm]<br>fun [snapshot](snapshot.md)(): [PacketPipelineSnapshot](../../com.resistine.android.network.forwarding/-packet-pipeline-snapshot/index.md)?<br>Returns a snapshot of current pipeline statistics. |
| [start](start.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [start](start.md)()<br>Starts the telemetry session, initializing pipelines, coroutine scopes, and background tasks. |
| [stop](stop.md) | [androidJvm]<br>suspend fun [stop](stop.md)(): [PacketPipelineSnapshot](../../com.resistine.android.network.forwarding/-packet-pipeline-snapshot/index.md)?<br>Stops the telemetry coordinator and session, flushing pending records and stopping background workers. |
| [updateNativeStats](update-native-stats.md) | [androidJvm]<br>fun [updateNativeStats](update-native-stats.md)(depth: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), highWater: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), dropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html))<br>Updates native statistics with explicit drops. |