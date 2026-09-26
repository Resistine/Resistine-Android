//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhLogger](index.md)/[connect](connect.md)

# connect

[androidJvm]\
suspend fun [connect](connect.md)(serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), onStatusUpdate: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)

Connects to the Wazuh manager log ingestion socket and sends startup messages.

#### Return

True if connection and startup succeed; false otherwise.

#### Parameters

androidJvm

| | |
|---|---|
| serverIp | IP address or hostname of the Wazuh manager. |
| agentPort | Log ingestion port on the manager. |
| agentId | Assigned Wazuh agent ID. |
| rawAgentKey | Raw shared agent key. |
| onStatusUpdate | Callback invoked with status update messages. |