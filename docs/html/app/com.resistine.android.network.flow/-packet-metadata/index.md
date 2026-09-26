//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[PacketMetadata](index.md)

# PacketMetadata

[androidJvm]\
data class [PacketMetadata](index.md)(val timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val protocolCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val srcIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val srcPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val dstIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val dstPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val bytes: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val outbound: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val payloadBytes: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val protocolEvidence: [ProtocolEvidence](../-protocol-evidence/index.md) = ProtocolEvidence())

Metadata parsed from an individual IP packet.

## Constructors

| | |
|---|---|
| [PacketMetadata](-packet-metadata.md) | [androidJvm]<br>constructor(timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), ipVersion: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), protocolCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), srcIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), srcPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), dstIp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), dstPort: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), bytes: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), outbound: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), payloadBytes: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, protocolEvidence: [ProtocolEvidence](../-protocol-evidence/index.md) = ProtocolEvidence()) |

## Properties

| Name | Summary |
|---|---|
| [bytes](bytes.md) | [androidJvm]<br>val [bytes](bytes.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Total packet length in bytes. |
| [dstIp](dst-ip.md) | [androidJvm]<br>val [dstIp](dst-ip.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Destination IP address. |
| [dstPort](dst-port.md) | [androidJvm]<br>val [dstPort](dst-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Destination port. |
| [ipVersion](ip-version.md) | [androidJvm]<br>val [ipVersion](ip-version.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>IP version (4 or 6). |
| [outbound](outbound.md) | [androidJvm]<br>val [outbound](outbound.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Whether the packet is outbound. |
| [payloadBytes](payload-bytes.md) | [androidJvm]<br>val [payloadBytes](payload-bytes.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Payload length in bytes. |
| [protocolCode](protocol-code.md) | [androidJvm]<br>val [protocolCode](protocol-code.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Protocol number. |
| [protocolEvidence](protocol-evidence.md) | [androidJvm]<br>val [protocolEvidence](protocol-evidence.md): [ProtocolEvidence](../-protocol-evidence/index.md)<br>Extracted protocol inspection evidence. |
| [srcIp](src-ip.md) | [androidJvm]<br>val [srcIp](src-ip.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Source IP address. |
| [srcPort](src-port.md) | [androidJvm]<br>val [srcPort](src-port.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Source port. |
| [timestampMillis](timestamp-millis.md) | [androidJvm]<br>val [timestampMillis](timestamp-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Timestamp in milliseconds when captured. |