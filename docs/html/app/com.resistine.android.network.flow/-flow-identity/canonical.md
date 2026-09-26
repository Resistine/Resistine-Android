//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowIdentity](index.md)/[canonical](canonical.md)

# canonical

[androidJvm]\
fun [canonical](canonical.md)(ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), protocolCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), srcIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), srcPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), dstIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), dstPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)): [TransportFlowIdentity](../-transport-flow-identity/index.md)

Creates a canonical [TransportFlowIdentity](../-transport-flow-identity/index.md) by sorting endpoints to ensure consistent identification for both directions.

#### Return

Canonical [TransportFlowIdentity](../-transport-flow-identity/index.md).

#### Parameters

androidJvm

| | |
|---|---|
| ipVersion | IP version (4 or 6). |
| protocolCode | Protocol code. |
| srcIp | Source IP address. |
| srcPort | Source port. |
| dstIp | Destination IP address. |
| dstPort | Destination port. |