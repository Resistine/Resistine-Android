//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhLogger](index.md)

# WazuhLogger

class [WazuhLogger](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

Logger managing direct TCP socket connections and encrypted log transmission to a Wazuh manager.

#### Parameters

androidJvm

| | |
|---|---|
| context | Application context. |

## Constructors

| | |
|---|---|
| [WazuhLogger](-wazuh-logger.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Functions

| Name | Summary |
|---|---|
| [connect](connect.md) | [androidJvm]<br>suspend fun [connect](connect.md)(serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), onStatusUpdate: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Connects to the Wazuh manager log ingestion socket and sends startup messages. |
| [connectOneShot](connect-one-shot.md) | [androidJvm]<br>suspend fun [connectOneShot](connect-one-shot.md)(serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Connects, sends a single operation, and disconnects in one shot. |
| [disconnect](disconnect.md) | [androidJvm]<br>fun [disconnect](disconnect.md)()<br>Closes the socket connection and releases resources. |
| [isConnected](is-connected.md) | [androidJvm]<br>fun [isConnected](is-connected.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Checks whether the TCP socket connection is currently active and open. |
| [sendKeepalive](send-keepalive.md) | [androidJvm]<br>suspend fun [sendKeepalive](send-keepalive.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Sends a keepalive heartbeat message to the manager. |
| [sendSingleLog](send-single-log.md) | [androidJvm]<br>suspend fun [sendSingleLog](send-single-log.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), logMessage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Sends a single log message over the established connection. |
| [sendStartup](send-startup.md) | [androidJvm]<br>suspend fun [sendStartup](send-startup.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Sends the agent startup handshake message. |