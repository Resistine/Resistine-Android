//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhLogger](index.md)/[sendStartup](send-startup.md)

# sendStartup

[androidJvm]\
suspend fun [sendStartup](send-startup.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)

Sends the agent startup handshake message.

#### Return

True if sent successfully; false otherwise.

#### Parameters

androidJvm

| | |
|---|---|
| agentId | Assigned Wazuh agent ID. |
| rawAgentKey | Raw shared agent key. |