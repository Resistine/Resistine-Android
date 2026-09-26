//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[TransportFlowIdentity](index.md)

# TransportFlowIdentity

[androidJvm]\
data class [TransportFlowIdentity](index.md)(val ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val protocolCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val endpointA: [FlowEndpointIdentity](../-flow-endpoint-identity/index.md), val endpointB: [FlowEndpointIdentity](../-flow-endpoint-identity/index.md))

Represents a bi-directional transport flow identity between two endpoints.

## Constructors

| | |
|---|---|
| [TransportFlowIdentity](-transport-flow-identity.md) | [androidJvm]<br>constructor(ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), protocolCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), endpointA: [FlowEndpointIdentity](../-flow-endpoint-identity/index.md), endpointB: [FlowEndpointIdentity](../-flow-endpoint-identity/index.md)) |

## Properties

| Name | Summary |
|---|---|
| [endpointA](endpoint-a.md) | [androidJvm]<br>val [endpointA](endpoint-a.md): [FlowEndpointIdentity](../-flow-endpoint-identity/index.md)<br>First normalized endpoint. |
| [endpointB](endpoint-b.md) | [androidJvm]<br>val [endpointB](endpoint-b.md): [FlowEndpointIdentity](../-flow-endpoint-identity/index.md)<br>Second normalized endpoint. |
| [ipVersion](ip-version.md) | [androidJvm]<br>val [ipVersion](ip-version.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>IP version (4 or 6). |
| [protocolCode](protocol-code.md) | [androidJvm]<br>val [protocolCode](protocol-code.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Transport protocol code. |

## Functions

| Name | Summary |
|---|---|
| [shardKey](shard-key.md) | [androidJvm]<br>fun [shardKey](shard-key.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Returns a shard key string for the flow identity. |