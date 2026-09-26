//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[VpnViewModel](index.md)/[toggleTrustedNetwork](toggle-trusted-network.md)

# toggleTrustedNetwork

[androidJvm]\
fun [toggleTrustedNetwork](toggle-trusted-network.md)(ssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), bssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?, securityProfile: [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md)?, frequencyMhz: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)?, isCurrent: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), currentlyTrusted: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)

Toggles trust for a network, typically called from a list of nearby networks.