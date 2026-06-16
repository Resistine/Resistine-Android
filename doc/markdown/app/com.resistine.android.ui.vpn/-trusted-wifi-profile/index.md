//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[TrustedWifiProfile](index.md)

# TrustedWifiProfile

[androidJvm]\
data class [TrustedWifiProfile](index.md)(val ssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val bssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?, val knownBssids: [Set](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-set/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptySet(), val securityProfile: [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md), val lastFrequencyMhz: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, val lastSeenMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null, val lastGateway: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val pendingBssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val pendingSecurityProfile: [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md)? = null, val pendingSeenCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val pendingLastFrequencyMhz: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, val pendingLastGateway: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val pendingLastSeenMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null)

Stored profile for a network the user has explicitly trusted.

## Constructors

| | |
|---|---|
| [TrustedWifiProfile](-trusted-wifi-profile.md) | [androidJvm]<br>constructor(ssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), bssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?, knownBssids: [Set](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-set/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptySet(), securityProfile: [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md), lastFrequencyMhz: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, lastSeenMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null, lastGateway: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, pendingBssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, pendingSecurityProfile: [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md)? = null, pendingSeenCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, pendingLastFrequencyMhz: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, pendingLastGateway: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, pendingLastSeenMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [bssid](bssid.md) | [androidJvm]<br>val [bssid](bssid.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [knownBssids](known-bssids.md) | [androidJvm]<br>val [knownBssids](known-bssids.md): [Set](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-set/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [lastFrequencyMhz](last-frequency-mhz.md) | [androidJvm]<br>val [lastFrequencyMhz](last-frequency-mhz.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? |
| [lastGateway](last-gateway.md) | [androidJvm]<br>val [lastGateway](last-gateway.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [lastSeenMillis](last-seen-millis.md) | [androidJvm]<br>val [lastSeenMillis](last-seen-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? |
| [pendingBssid](pending-bssid.md) | [androidJvm]<br>val [pendingBssid](pending-bssid.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [pendingLastFrequencyMhz](pending-last-frequency-mhz.md) | [androidJvm]<br>val [pendingLastFrequencyMhz](pending-last-frequency-mhz.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? |
| [pendingLastGateway](pending-last-gateway.md) | [androidJvm]<br>val [pendingLastGateway](pending-last-gateway.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [pendingLastSeenMillis](pending-last-seen-millis.md) | [androidJvm]<br>val [pendingLastSeenMillis](pending-last-seen-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? |
| [pendingSecurityProfile](pending-security-profile.md) | [androidJvm]<br>val [pendingSecurityProfile](pending-security-profile.md): [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md)? |
| [pendingSeenCount](pending-seen-count.md) | [androidJvm]<br>val [pendingSeenCount](pending-seen-count.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [securityProfile](security-profile.md) | [androidJvm]<br>val [securityProfile](security-profile.md): [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md) |
| [securityType](security-type.md) | [androidJvm]<br>val [securityType](security-type.md): [WifiSecurityType](../-wifi-security-type/index.md)<br>The broad security type of this trusted profile. |
| [ssid](ssid.md) | [androidJvm]<br>val [ssid](ssid.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |