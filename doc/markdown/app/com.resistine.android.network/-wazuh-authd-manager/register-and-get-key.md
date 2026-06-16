//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhAuthdManager](index.md)/[registerAndGetKey](register-and-get-key.md)

# registerAndGetKey

[androidJvm]\
suspend fun [registerAndGetKey](register-and-get-key.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), authPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), userEmail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), enrollmentPassword: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = &quot;&quot;, agentIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null): [Pair](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-pair/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;

Connects to the Wazuh authd service and performs registration.

#### Return

A [Pair](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-pair/index.html) containing the (Agent ID, Agent Key).

#### Parameters

androidJvm

| | |
|---|---|
| context | Application context for resource access. |
| serverIp | IP address of the Wazuh Manager. |
| authPort | Port of the authd service (typically 1515). |
| agentName | Desired name for the new agent. |
| userEmail | Email associated with the agent (used for grouping). |
| enrollmentPassword | Optional password if authd is password-protected. |
| agentIp | Optional specific IP to register for the agent. |

#### Throws

| | |
|---|---|
| [Exception](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-exception/index.html) | if connection fails, handshake fails, or server returns an error. |