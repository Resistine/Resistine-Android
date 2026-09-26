//[app](../../../index.md)/[com.resistine.android.network.forwarding](../index.md)/[FlowTelemetryDiagnosticsMonitor](index.md)

# FlowTelemetryDiagnosticsMonitor

[androidJvm]\
object [FlowTelemetryDiagnosticsMonitor](index.md)

Diagnostics monitor tracking and persisting packet pipeline telemetry snapshots.

## Properties

| Name | Summary |
|---|---|
| [snapshot](snapshot.md) | [androidJvm]<br>val [snapshot](snapshot.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[PacketPipelineSnapshot](../-packet-pipeline-snapshot/index.md)&gt;<br>LiveData observing pipeline diagnostic snapshots. |

## Functions

| Name | Summary |
|---|---|
| [beginSession](begin-session.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [beginSession](begin-session.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Begins a new diagnostics session, publishing an empty snapshot. |
| [initialize](initialize.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [initialize](initialize.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Initializes the diagnostics monitor, loading last saved snapshot if available. |
| [load](load.md) | [androidJvm]<br>fun [load](load.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)): [PacketPipelineSnapshot](../-packet-pipeline-snapshot/index.md)<br>Loads the last persisted pipeline snapshot from preferences. |
| [publish](publish.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [publish](publish.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), snapshot: [PacketPipelineSnapshot](../-packet-pipeline-snapshot/index.md))<br>Publishes a pipeline snapshot and persists it to shared preferences. |