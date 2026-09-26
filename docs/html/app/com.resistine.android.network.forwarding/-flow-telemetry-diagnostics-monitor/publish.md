//[app](../../../index.md)/[com.resistine.android.network.forwarding](../index.md)/[FlowTelemetryDiagnosticsMonitor](index.md)/[publish](publish.md)

# publish

[androidJvm]\

@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)

fun [publish](publish.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), snapshot: [PacketPipelineSnapshot](../-packet-pipeline-snapshot/index.md))

Publishes a pipeline snapshot and persists it to shared preferences.

#### Parameters

androidJvm

| | |
|---|---|
| context | Application context. |
| snapshot | The [PacketPipelineSnapshot](../-packet-pipeline-snapshot/index.md) to publish. |