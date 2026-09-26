//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhLogger](index.md)/[sendSingleLog](send-single-log.md)

# sendSingleLog

[androidJvm]\
suspend fun [sendSingleLog](send-single-log.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), logMessage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)

Sends a single log message over the established connection.

#### Return

True if sent successfully; false otherwise.

#### Parameters

androidJvm

| | |
|---|---|
| agentId | Agent ID. |
| rawAgentKey | Agent key. |
| logMessage | The log message string. |