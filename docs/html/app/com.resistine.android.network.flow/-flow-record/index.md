//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowRecord](index.md)

# FlowRecord

[androidJvm]\
data class [FlowRecord](index.md)(val id: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val timestampStartMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val timestampEndMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val protocol: [FlowProtocol](../-flow-protocol/index.md), val srcIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val srcPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val dstIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val dstPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val bytesOut: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val bytesIn: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val packetsOut: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val packetsIn: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val networkType: [FlowNetworkType](../-flow-network-type/index.md) = FlowNetworkType.UNKNOWN, val appUid: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, val appPackage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val vpnActive: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = true, val protocolEvidence: [ProtocolEvidence](../-protocol-evidence/index.md) = ProtocolEvidence())

Aggregated record representing a complete network connection flow.

## Constructors

| | |
|---|---|
| [FlowRecord](-flow-record.md) | [androidJvm]<br>constructor(id: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), timestampStartMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), timestampEndMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), protocol: [FlowProtocol](../-flow-protocol/index.md), srcIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), srcPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), dstIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), dstPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), bytesOut: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), bytesIn: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), packetsOut: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), packetsIn: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), networkType: [FlowNetworkType](../-flow-network-type/index.md) = FlowNetworkType.UNKNOWN, appUid: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, appPackage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, vpnActive: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = true, protocolEvidence: [ProtocolEvidence](../-protocol-evidence/index.md) = ProtocolEvidence()) |

## Properties

| Name | Summary |
|---|---|
| [appPackage](app-package.md) | [androidJvm]<br>val [appPackage](app-package.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Application package name. |
| [appUid](app-uid.md) | [androidJvm]<br>val [appUid](app-uid.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)?<br>Application UID associated with the flow. |
| [bytesIn](bytes-in.md) | [androidJvm]<br>val [bytesIn](bytes-in.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Total inbound bytes transferred. |
| [bytesOut](bytes-out.md) | [androidJvm]<br>val [bytesOut](bytes-out.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Total outbound bytes transferred. |
| [dstIp](dst-ip.md) | [androidJvm]<br>val [dstIp](dst-ip.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Destination IP address. |
| [dstPort](dst-port.md) | [androidJvm]<br>val [dstPort](dst-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Destination port. |
| [durationMillis](duration-millis.md) | [androidJvm]<br>val [durationMillis](duration-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Total duration of the flow in milliseconds. |
| [id](id.md) | [androidJvm]<br>val [id](id.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Unique flow record UUID. |
| [ipVersion](ip-version.md) | [androidJvm]<br>val [ipVersion](ip-version.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>IP version. |
| [networkType](network-type.md) | [androidJvm]<br>val [networkType](network-type.md): [FlowNetworkType](../-flow-network-type/index.md)<br>Active [FlowNetworkType](../-flow-network-type/index.md). |
| [packetsIn](packets-in.md) | [androidJvm]<br>val [packetsIn](packets-in.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Total inbound packets. |
| [packetsOut](packets-out.md) | [androidJvm]<br>val [packetsOut](packets-out.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Total outbound packets. |
| [protocol](protocol.md) | [androidJvm]<br>val [protocol](protocol.md): [FlowProtocol](../-flow-protocol/index.md)<br>Transport protocol [FlowProtocol](../-flow-protocol/index.md). |
| [protocolEvidence](protocol-evidence.md) | [androidJvm]<br>val [protocolEvidence](protocol-evidence.md): [ProtocolEvidence](../-protocol-evidence/index.md)<br>Associated [ProtocolEvidence](../-protocol-evidence/index.md). |
| [srcIp](src-ip.md) | [androidJvm]<br>val [srcIp](src-ip.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Source IP address. |
| [srcPort](src-port.md) | [androidJvm]<br>val [srcPort](src-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Source port. |
| [timestampEndMillis](timestamp-end-millis.md) | [androidJvm]<br>val [timestampEndMillis](timestamp-end-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Flow end epoch timestamp in milliseconds. |
| [timestampStartMillis](timestamp-start-millis.md) | [androidJvm]<br>val [timestampStartMillis](timestamp-start-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Flow start epoch timestamp in milliseconds. |
| [vpnActive](vpn-active.md) | [androidJvm]<br>val [vpnActive](vpn-active.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Whether VPN was active. |