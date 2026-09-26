//[app](../../index.md)/[com.resistine.android.network.wireguard](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [WireGuardFlowTelemetryCoordinator](-wire-guard-flow-telemetry-coordinator/index.md) | [androidJvm]<br>class [WireGuardFlowTelemetryCoordinator](-wire-guard-flow-telemetry-coordinator/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), onHealthMessage: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {}) : [PacketTelemetrySink](../com.resistine.android.network.flow/-packet-telemetry-sink/index.md), [TelemetryGoBackend.TelemetryHealthListener](../com.wireguard.android.backend/-telemetry-go-backend/-telemetry-health-listener/index.md)<br>Coordinator bridging WireGuard backend telemetry callbacks and packet ingestion with the flow telemetry pipeline, local database queue, and Wazuh delivery service. |