//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhConnectionMonitor](index.md)

# WazuhConnectionMonitor

[androidJvm]\
object [WazuhConnectionMonitor](index.md)

Singleton monitor tracking Wazuh connection state and telemetry delivery progress.

## Properties

| Name | Summary |
|---|---|
| [status](status.md) | [androidJvm]<br>val [status](status.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[WazuhConnectionStatus](../-wazuh-connection-status/index.md)&gt;<br>LiveData stream observing the current Wazuh connection status. |

## Functions

| Name | Summary |
|---|---|
| [beginTelemetrySession](begin-telemetry-session.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [beginTelemetrySession](begin-telemetry-session.md)()<br>Begins a new telemetry session, resetting delivered flow counts and errors. |
| [recordFlowDelivered](record-flow-delivered.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [recordFlowDelivered](record-flow-delivered.md)()<br>Records that a flow telemetry record has been successfully delivered. |
| [update](update.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [update](update.md)(state: [WazuhConnectionState](../-wazuh-connection-state/index.md), detail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))<br>Updates the connection state and detail message. |