//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowEndpointIdentity](index.md)

# FlowEndpointIdentity

[androidJvm]\
data class [FlowEndpointIdentity](index.md)(val ip: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val port: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))

Represents an IP endpoint with IP address and port.

## Constructors

| | |
|---|---|
| [FlowEndpointIdentity](-flow-endpoint-identity.md) | [androidJvm]<br>constructor(ip: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), port: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [ip](ip.md) | [androidJvm]<br>val [ip](ip.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>IP address string. |
| [port](port.md) | [androidJvm]<br>val [port](port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Port number. |

## Functions

| Name | Summary |
|---|---|
| [sortKey](sort-key.md) | [androidJvm]<br>fun [sortKey](sort-key.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Returns a sortable string representation of the endpoint. |