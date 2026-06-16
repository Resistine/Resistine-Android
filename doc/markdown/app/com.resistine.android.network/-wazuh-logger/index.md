//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhLogger](index.md)

# WazuhLogger

[androidJvm]\
class [WazuhLogger](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

## Constructors

| | |
|---|---|
| [WazuhLogger](-wazuh-logger.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Functions

| Name | Summary |
|---|---|
| [connect](connect.md) | [androidJvm]<br>suspend fun [connect](connect.md)(serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), onStatusUpdate: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [connectOneShot](connect-one-shot.md) | [androidJvm]<br>suspend fun [connectOneShot](connect-one-shot.md)(serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [disconnect](disconnect.md) | [androidJvm]<br>fun [disconnect](disconnect.md)() |
| [isConnected](is-connected.md) | [androidJvm]<br>fun [isConnected](is-connected.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendKeepalive](send-keepalive.md) | [androidJvm]<br>suspend fun [sendKeepalive](send-keepalive.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [sendSingleLog](send-single-log.md) | [androidJvm]<br>suspend fun [sendSingleLog](send-single-log.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), logMessage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [sendStartup](send-startup.md) | [androidJvm]<br>suspend fun [sendStartup](send-startup.md)(agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)? |