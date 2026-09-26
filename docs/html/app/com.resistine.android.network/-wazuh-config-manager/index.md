//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhConfigManager](index.md)

# WazuhConfigManager

[androidJvm]\
class [WazuhConfigManager](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

Manager for persistent Wazuh server connection configuration settings.

## Constructors

| | |
|---|---|
| [WazuhConfigManager](-wazuh-config-manager.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Properties

| Name | Summary |
|---|---|
| [authPort](auth-port.md) | [androidJvm]<br>var [authPort](auth-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Port number for Wazuh agent enrollment/authd. |
| [logPort](log-port.md) | [androidJvm]<br>var [logPort](log-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Port number for Wazuh agent log streaming. |
| [serverIp](server-ip.md) | [androidJvm]<br>var [serverIp](server-ip.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>IP address or hostname of the Wazuh manager. |

## Functions

| Name | Summary |
|---|---|
| [endpoint](endpoint.md) | [androidJvm]<br>fun [endpoint](endpoint.md)(): [WazuhManagerEndpoint](../-wazuh-manager-endpoint/index.md)<br>Constructs a [WazuhManagerEndpoint](../-wazuh-manager-endpoint/index.md) from current configuration settings. |
| [updateEndpoint](update-endpoint.md) | [androidJvm]<br>fun [updateEndpoint](update-endpoint.md)(endpoint: [WazuhManagerEndpoint](../-wazuh-manager-endpoint/index.md))<br>Updates stored configuration endpoint settings. |