//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[SystemEventLogger](index.md)

# SystemEventLogger

[androidJvm]\
class [SystemEventLogger](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

Logger observing system broadcasts (screen state, battery changes, connectivity changes, and package events) and reporting them to the [WazuhAgent](../-wazuh-agent/index.md).

## Constructors

| | |
|---|---|
| [SystemEventLogger](-system-event-logger.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Functions

| Name | Summary |
|---|---|
| [start](start.md) | [androidJvm]<br>fun [start](start.md)()<br>Registers broadcast receivers to start logging system events. |
| [stop](stop.md) | [androidJvm]<br>fun [stop](stop.md)()<br>Unregisters broadcast receivers to stop logging system events. |