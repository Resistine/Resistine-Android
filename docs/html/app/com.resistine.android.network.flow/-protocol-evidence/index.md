//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[ProtocolEvidence](index.md)

# ProtocolEvidence

[androidJvm]\
data class [ProtocolEvidence](index.md)(val dnsQueryName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val dnsQueryType: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val dnsResponseCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, val dnsAnswerValue: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val tlsSni: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val tlsAlpn: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val httpHost: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val httpMethod: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null)

Protocol inspection evidence extracted from packet payloads (DNS, TLS SNI, HTTP host, etc.).

## Constructors

| | |
|---|---|
| [ProtocolEvidence](-protocol-evidence.md) | [androidJvm]<br>constructor(dnsQueryName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, dnsQueryType: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, dnsResponseCode: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, dnsAnswerValue: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, tlsSni: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, tlsAlpn: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, httpHost: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, httpMethod: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [dnsAnswerValue](dns-answer-value.md) | [androidJvm]<br>val [dnsAnswerValue](dns-answer-value.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Resolved DNS answer value. |
| [dnsQueryName](dns-query-name.md) | [androidJvm]<br>val [dnsQueryName](dns-query-name.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Extracted DNS query domain name. |
| [dnsQueryType](dns-query-type.md) | [androidJvm]<br>val [dnsQueryType](dns-query-type.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>DNS query type (e.g. A, AAAA). |
| [dnsResponseCode](dns-response-code.md) | [androidJvm]<br>val [dnsResponseCode](dns-response-code.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)?<br>DNS response return code. |
| [httpHost](http-host.md) | [androidJvm]<br>val [httpHost](http-host.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>HTTP Host header value. |
| [httpMethod](http-method.md) | [androidJvm]<br>val [httpMethod](http-method.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>HTTP request method. |
| [tlsAlpn](tls-alpn.md) | [androidJvm]<br>val [tlsAlpn](tls-alpn.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>TLS ALPN protocol. |
| [tlsSni](tls-sni.md) | [androidJvm]<br>val [tlsSni](tls-sni.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>TLS Server Name Indication (SNI). |

## Functions

| Name | Summary |
|---|---|
| [merge](merge.md) | [androidJvm]<br>fun [merge](merge.md)(other: [ProtocolEvidence](index.md)): [ProtocolEvidence](index.md)<br>Merges another [ProtocolEvidence](index.md) into this one, preferring non-null values. |
| [preferredHost](preferred-host.md) | [androidJvm]<br>fun [preferredHost](preferred-host.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Returns the preferred hostname from TLS SNI, HTTP host, or DNS query name. |