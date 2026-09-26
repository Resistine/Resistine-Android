//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[TunPacketParser](index.md)

# TunPacketParser

[androidJvm]\
class [TunPacketParser](index.md)(localIpv4Prefixes: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = listOf(&quot;10.&quot;, &quot;172.16.&quot;, &quot;172.17.&quot;, &quot;172.18.&quot;, &quot;172.19.&quot;, &quot;172.20.&quot;, &quot;172.21.&quot;, &quot;172.22.&quot;, &quot;172.23.&quot;, &quot;172.24.&quot;, &quot;172.25.&quot;, &quot;172.26.&quot;, &quot;172.27.&quot;, &quot;172.28.&quot;, &quot;172.29.&quot;, &quot;172.30.&quot;, &quot;172.31.&quot;, &quot;192.168.&quot;), localIpv6Prefixes: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = listOf(&quot;fd&quot;, &quot;fe80&quot;))

Parser for analyzing IPv4 and IPv6 packets captured from a TUN interface, extracting transport headers, port numbers, app traffic characteristics, and application-layer protocol evidence.

## Constructors

| | |
|---|---|
| [TunPacketParser](-tun-packet-parser.md) | [androidJvm]<br>constructor(localIpv4Prefixes: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = listOf(&quot;10.&quot;, &quot;172.16.&quot;, &quot;172.17.&quot;, &quot;172.18.&quot;, &quot;172.19.&quot;, &quot;172.20.&quot;, &quot;172.21.&quot;, &quot;172.22.&quot;, &quot;172.23.&quot;, &quot;172.24.&quot;, &quot;172.25.&quot;, &quot;172.26.&quot;, &quot;172.27.&quot;, &quot;172.28.&quot;, &quot;172.29.&quot;, &quot;172.30.&quot;, &quot;172.31.&quot;, &quot;192.168.&quot;), localIpv6Prefixes: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = listOf(&quot;fd&quot;, &quot;fe80&quot;)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [parse](parse.md) | [androidJvm]<br>fun [parse](parse.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), direction: [PacketDirection](../-packet-direction/index.md)? = null): [PacketMetadata](../-packet-metadata/index.md)?<br>Parses a raw IP packet byte array into [PacketMetadata](../-packet-metadata/index.md). |