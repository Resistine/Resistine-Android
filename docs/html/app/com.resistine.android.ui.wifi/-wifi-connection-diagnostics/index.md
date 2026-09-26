//[app](../../../index.md)/[com.resistine.android.ui.wifi](../index.md)/[WifiConnectionDiagnostics](index.md)

# WifiConnectionDiagnostics

[androidJvm]\
data class [WifiConnectionDiagnostics](index.md)(val privateDnsActive: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)? = null, val privateDnsServerName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val dnsServers: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), val hasDefaultRoute: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val activeRouteUsesVpn: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val validatedByAndroid: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val tlsProbeStatus: [TlsProbeStatus](../-tls-probe-status/index.md) = TlsProbeStatus.NOT_CONFIGURED, val tlsProbeLatencyMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null, val tlsProbeDetail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null)

## Constructors

| | |
|---|---|
| [WifiConnectionDiagnostics](-wifi-connection-diagnostics.md) | [androidJvm]<br>constructor(privateDnsActive: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)? = null, privateDnsServerName: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, dnsServers: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), hasDefaultRoute: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, activeRouteUsesVpn: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, validatedByAndroid: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, tlsProbeStatus: [TlsProbeStatus](../-tls-probe-status/index.md) = TlsProbeStatus.NOT_CONFIGURED, tlsProbeLatencyMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null, tlsProbeDetail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [activeRouteUsesVpn](active-route-uses-vpn.md) | [androidJvm]<br>val [activeRouteUsesVpn](active-route-uses-vpn.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [dnsServers](dns-servers.md) | [androidJvm]<br>val [dnsServers](dns-servers.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [hasDefaultRoute](has-default-route.md) | [androidJvm]<br>val [hasDefaultRoute](has-default-route.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [privateDnsActive](private-dns-active.md) | [androidJvm]<br>val [privateDnsActive](private-dns-active.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)? |
| [privateDnsServerName](private-dns-server-name.md) | [androidJvm]<br>val [privateDnsServerName](private-dns-server-name.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [tlsProbeDetail](tls-probe-detail.md) | [androidJvm]<br>val [tlsProbeDetail](tls-probe-detail.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [tlsProbeLatencyMillis](tls-probe-latency-millis.md) | [androidJvm]<br>val [tlsProbeLatencyMillis](tls-probe-latency-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? |
| [tlsProbeStatus](tls-probe-status.md) | [androidJvm]<br>val [tlsProbeStatus](tls-probe-status.md): [TlsProbeStatus](../-tls-probe-status/index.md) |
| [validatedByAndroid](validated-by-android.md) | [androidJvm]<br>val [validatedByAndroid](validated-by-android.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |