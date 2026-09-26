//[app](../../../index.md)/[com.resistine.android.security](../index.md)/[WazuhAgent](index.md)

# WazuhAgent

class [WazuhAgent](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

Security agent responsible for recording system logs, security posture checks, device info, and persisting log entries for Wazuh transmission.

#### Parameters

androidJvm

| | |
|---|---|
| context | Application context. |

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
| [getLogFilePath](get-log-file-path.md) | [androidJvm]<br>fun [getLogFilePath](get-log-file-path.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Returns the absolute file path of the local agent log file. |
| [log](log.md) | [androidJvm]<br>fun [log](log.md)(level: [LogLevel](../-log-level/index.md), message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), data: [JSONObject](https://developer.android.com/reference/kotlin/org/json/JSONObject.html)? = null)<br>Records a log entry with the specified log level, message, and optional JSON data. |
| [logAppStart](log-app-start.md) | [androidJvm]<br>fun [logAppStart](log-app-start.md)()<br>Logs application startup event along with device info and security posture. |
| [logBatteryState](log-battery-state.md) | [androidJvm]<br>fun [logBatteryState](log-battery-state.md)(level: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), scale: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), isCharging: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html))<br>Logs battery state changes. |
| [logDeviceUnlock](log-device-unlock.md) | [androidJvm]<br>fun [logDeviceUnlock](log-device-unlock.md)()<br>Logs device unlock event. |
| [logNetworkState](log-network-state.md) | [androidJvm]<br>fun [logNetworkState](log-network-state.md)(isConnected: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), networkType: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))<br>Logs network state changes. |
| [logPackageEvent](log-package-event.md) | [androidJvm]<br>fun [logPackageEvent](log-package-event.md)(action: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), packageName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))<br>Logs package lifecycle events (added, removed, replaced). |
| [logScreenState](log-screen-state.md) | [androidJvm]<br>fun [logScreenState](log-screen-state.md)(isOn: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html))<br>Logs screen state changes (on/off). |
| [logSystemSecurityPosture](log-system-security-posture.md) | [androidJvm]<br>fun [logSystemSecurityPosture](log-system-security-posture.md)()<br>Logs system security posture checks (ADB enabled, development settings, root status, etc.). |
| [readLogs](read-logs.md) | [androidJvm]<br>fun [readLogs](read-logs.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Reads recent logs from the log file. |