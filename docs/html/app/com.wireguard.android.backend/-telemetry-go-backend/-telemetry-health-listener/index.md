//[app](../../../../index.md)/[com.wireguard.android.backend](../../index.md)/[TelemetryGoBackend](../index.md)/[TelemetryHealthListener](index.md)

# TelemetryHealthListener

interface [TelemetryHealthListener](index.md)

#### Inheritors

| |
|---|
| [WireGuardFlowTelemetryCoordinator](../../../com.resistine.android.network.wireguard/-wire-guard-flow-telemetry-coordinator/index.md) |

## Functions

| Name | Summary |
|---|---|
| [onNativePacketDrops](on-native-packet-drops.md) | [androidJvm]<br>abstract fun [onNativePacketDrops](on-native-packet-drops.md)(count: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)) |
| [onNativeQueueStats](on-native-queue-stats.md) | [androidJvm]<br>open fun [onNativeQueueStats](on-native-queue-stats.md)(depth: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), highWater: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)) |
| [onTelemetryReaderFailure](on-telemetry-reader-failure.md) | [androidJvm]<br>abstract fun [onTelemetryReaderFailure](on-telemetry-reader-failure.md)(error: [Throwable](https://developer.android.com/reference/kotlin/java/lang/Throwable.html)) |