//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[WazuhAgent](index.md)

# WazuhAgent

[androidJvm]\
class [WazuhAgent](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

## Constructors

| | |
|---|---|
| [WazuhAgent](-wazuh-agent.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [getLogFilePath](get-log-file-path.md) | [androidJvm]<br>fun [getLogFilePath](get-log-file-path.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [log](log.md) | [androidJvm]<br>fun [log](log.md)(level: [LogLevel](../-log-level/index.md), message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), data: [JSONObject](https://developer.android.com/reference/kotlin/org/json/JSONObject.html)? = null) |
| [logAppStart](log-app-start.md) | [androidJvm]<br>fun [logAppStart](log-app-start.md)() |
| [logBatteryState](log-battery-state.md) | [androidJvm]<br>fun [logBatteryState](log-battery-state.md)(level: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), scale: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), isCharging: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)) |
| [logDeviceUnlock](log-device-unlock.md) | [androidJvm]<br>fun [logDeviceUnlock](log-device-unlock.md)() |
| [logNetworkState](log-network-state.md) | [androidJvm]<br>fun [logNetworkState](log-network-state.md)(isConnected: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), networkType: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |
| [readLogs](read-logs.md) | [androidJvm]<br>fun [readLogs](read-logs.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |