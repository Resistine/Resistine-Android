//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhAuthdManager](index.md)

# WazuhAuthdManager

[androidJvm]\
class [WazuhAuthdManager](index.md)

Manager responsible for automatic agent registration via the Wazuh 'authd' service.

It communicates over a secure TLS socket to request a unique Agent ID and Key using the device and user metadata.

## Constructors

| | |
|---|---|
| [WazuhAuthdManager](-wazuh-authd-manager.md) | [androidJvm]<br>constructor() |

## Functions

| Name | Summary |
|---|---|
| [registerAndGetKey](register-and-get-key.md) | [androidJvm]<br>suspend fun [registerAndGetKey](register-and-get-key.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), serverIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), authPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), agentName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), userEmail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), enrollmentPassword: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = &quot;&quot;, agentIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null): [Pair](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-pair/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>Connects to the Wazuh authd service and performs registration. |