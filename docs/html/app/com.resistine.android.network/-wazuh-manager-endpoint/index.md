//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhManagerEndpoint](index.md)

# WazuhManagerEndpoint

[androidJvm]\
data class [WazuhManagerEndpoint](index.md)(val host: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val authPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val logPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))

Represents a Wazuh manager network endpoint configuration.

## Constructors

| | |
|---|---|
| [WazuhManagerEndpoint](-wazuh-manager-endpoint.md) | [androidJvm]<br>constructor(host: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), authPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), logPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [authPort](auth-port.md) | [androidJvm]<br>val [authPort](auth-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Port number for agent authentication/enrollment (authd). |
| [host](host.md) | [androidJvm]<br>val [host](host.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Server hostname or IP address. |
| [logPort](log-port.md) | [androidJvm]<br>val [logPort](log-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Port number for secure log delivery. |