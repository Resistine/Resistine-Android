//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowIdentity](index.md)

# FlowIdentity

[androidJvm]\
object [FlowIdentity](index.md)

Utility for constructing canonical [TransportFlowIdentity](../-transport-flow-identity/index.md) instances regardless of packet direction.

## Functions

| Name | Summary |
|---|---|
| [canonical](canonical.md) | [androidJvm]<br>fun [canonical](canonical.md)(ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), protocolCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), srcIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), srcPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), dstIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), dstPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)): [TransportFlowIdentity](../-transport-flow-identity/index.md)<br>Creates a canonical [TransportFlowIdentity](../-transport-flow-identity/index.md) by sorting endpoints to ensure consistent identification for both directions. |