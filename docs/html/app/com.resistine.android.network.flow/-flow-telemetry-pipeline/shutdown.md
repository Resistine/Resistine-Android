//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowTelemetryPipeline](index.md)/[shutdown](shutdown.md)

# shutdown

[androidJvm]\
fun [shutdown](shutdown.md)(timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis()): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)

Shuts down the telemetry pipeline, flushing all active flows and closing the writer.

#### Return

Number of flushed flows.

#### Parameters

androidJvm

| | |
|---|---|
| timestampMillis | Shutdown timestamp in milliseconds. |