//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowTelemetryPipeline](index.md)/[flushExpired](flush-expired.md)

# flushExpired

[androidJvm]\
fun [flushExpired](flush-expired.md)(timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis()): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)

Flushes expired flows based on idle or hard timeouts.

#### Return

Number of flushed expired flows.

#### Parameters

androidJvm

| | |
|---|---|
| timestampMillis | Current timestamp in milliseconds. |