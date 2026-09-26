//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhLogger](index.md)/[connectOneShot](connect-one-shot.md)

# connectOneShot

[androidJvm]\
suspend fun [connectOneShot](connect-one-shot.md)(serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), agentPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentId: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), rawAgentKey: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)

Connects, sends a single operation, and disconnects in one shot.

#### Return

True if successful; false otherwise.

#### Parameters

androidJvm

| | |
|---|---|
| serverIp | Server IP address. |
| agentPort | Agent port number. |
| agentId | Agent ID. |
| rawAgentKey | Agent key. |