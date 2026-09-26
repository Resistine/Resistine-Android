//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhLogger](index.md)/[sendKeepalive](send-keepalive.md)

# sendKeepalive

[androidJvm]\
suspend fun [sendKeepalive](send-keepalive.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)

Sends a keepalive heartbeat message to the manager.

#### Return

True if sent successfully; false otherwise.

#### Parameters

androidJvm

| | |
|---|---|
| agentId | Assigned Wazuh agent ID. |
| rawAgentKey | Raw shared agent key. |
| agentName | Name of the agent. |