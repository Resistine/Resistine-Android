//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhConnectionStatus](index.md)

# WazuhConnectionStatus

[androidJvm]\
data class [WazuhConnectionStatus](index.md)(val state: [WazuhConnectionState](../-wazuh-connection-state/index.md), val detail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val flowRecordsDelivered: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0, val lastError: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null)

Data class representing the current status of the Wazuh connection.

## Constructors

| | |
|---|---|
| [WazuhConnectionStatus](-wazuh-connection-status.md) | [androidJvm]<br>constructor(state: [WazuhConnectionState](../-wazuh-connection-state/index.md), detail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), flowRecordsDelivered: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0, lastError: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [detail](detail.md) | [androidJvm]<br>val [detail](detail.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Descriptive text explaining the current state or error. |
| [flowRecordsDelivered](flow-records-delivered.md) | [androidJvm]<br>val [flowRecordsDelivered](flow-records-delivered.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Total number of flow telemetry records successfully delivered. |
| [lastError](last-error.md) | [androidJvm]<br>val [lastError](last-error.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Most recent error message encountered, if any. |
| [state](state.md) | [androidJvm]<br>val [state](state.md): [WazuhConnectionState](../-wazuh-connection-state/index.md)<br>Current [WazuhConnectionState](../-wazuh-connection-state/index.md). |