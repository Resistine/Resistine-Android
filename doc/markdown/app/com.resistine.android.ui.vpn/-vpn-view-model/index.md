//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[VpnViewModel](index.md)

# VpnViewModel

class [VpnViewModel](index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

ViewModel responsible for managing VPN state, Wi-Fi security monitoring, and device information updates.

It coordinates WireGuard tunnel operations, Wazuh agent lifecycle, and real-time Wi-Fi safety assessments.

#### Parameters

androidJvm

| | |
|---|---|
| application | The application context. |

## Constructors

| | |
|---|---|
| [VpnViewModel](-vpn-view-model.md) | [androidJvm]<br>constructor(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Properties

| Name | Summary |
|---|---|
| [androidVersion](android-version.md) | [androidJvm]<br>val [androidVersion](android-version.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>LiveData holding the formatted Android version and SDK level. |
| [autoProtectUnknownWifi](auto-protect-unknown-wifi.md) | [androidJvm]<br>val [autoProtectUnknownWifi](auto-protect-unknown-wifi.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt;<br>LiveData indicating if VPN should auto-connect on networks with limited data. |
| [autoVpnActionMessageRes](auto-vpn-action-message-res.md) | [androidJvm]<br>val [autoVpnActionMessageRes](auto-vpn-action-message-res.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)?&gt;<br>LiveData for passing string resource IDs of feedback messages (e.g., auto-disconnected). |
| [autoVpnPolicy](auto-vpn-policy.md) | [androidJvm]<br>val [autoVpnPolicy](auto-vpn-policy.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[AutoVpnPolicy](../-auto-vpn-policy/index.md)&gt;<br>LiveData representing the current automatic VPN connection policy. |
| [backgroundScanState](background-scan-state.md) | [androidJvm]<br>val [backgroundScanState](background-scan-state.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[WifiBackgroundScanState](../-wifi-background-scan-state/index.md)&gt;<br>LiveData describing the interval and active status of background Wi-Fi monitoring. |
| [batteryLevel](battery-level.md) | [androidJvm]<br>val [batteryLevel](battery-level.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>LiveData holding the current battery percentage as a formatted string. |
| [deviceModel](device-model.md) | [androidJvm]<br>val [deviceModel](device-model.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>LiveData holding the device manufacturer and model name. |
| [ipAddress](ip-address.md) | [androidJvm]<br>val [ipAddress](ip-address.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>LiveData holding the public IP address of the device. |
| [isVpnConnectedLiveData](is-vpn-connected-live-data.md) | [androidJvm]<br>val [isVpnConnectedLiveData](is-vpn-connected-live-data.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt;<br>LiveData boolean indicating whether the VPN is currently connected. |
| [locationString](location-string.md) | [androidJvm]<br>val [locationString](location-string.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>LiveData holding the location information (City, Country) inferred from the IP. |
| [nearbyWifiNetworksState](nearby-wifi-networks-state.md) | [androidJvm]<br>val [nearbyWifiNetworksState](nearby-wifi-networks-state.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[WifiNearbyNetworksState](../-wifi-nearby-networks-state/index.md)&gt;<br>LiveData representing the state of nearby Wi-Fi networks found in scans. |
| [trustedWifiNetworks](trusted-wifi-networks.md) | [androidJvm]<br>val [trustedWifiNetworks](trusted-wifi-networks.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[TrustedWifiProfile](../-trusted-wifi-profile/index.md)&gt;&gt;<br>LiveData list of Wi-Fi networks previously marked as trusted by the user. |
| [vpnStatus](vpn-status.md) | [androidJvm]<br>val [vpnStatus](vpn-status.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>LiveData representing the current readable status of the VPN connection. |
| [wifiAdvancedChecks](wifi-advanced-checks.md) | [androidJvm]<br>val [wifiAdvancedChecks](wifi-advanced-checks.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[WifiAdvancedCheckItem](../-wifi-advanced-check-item/index.md)&gt;&gt;<br>LiveData list of specific security check items (Encryption, PMF, Evil Twin, etc.). |
| [wifiRiskTransitionAlert](wifi-risk-transition-alert.md) | [androidJvm]<br>val [wifiRiskTransitionAlert](wifi-risk-transition-alert.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[WifiRiskTransitionAlert](../-wifi-risk-transition-alert/index.md)?&gt;<br>LiveData triggered when the Wi-Fi risk level changes significantly. |
| [wifiSafetyAssessment](wifi-safety-assessment.md) | [androidJvm]<br>val [wifiSafetyAssessment](wifi-safety-assessment.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[WifiSafetyAssessment](../-wifi-safety-assessment/index.md)&gt;<br>LiveData containing the detailed security score and risk level for the current network. |
| [wifiSecurityAlert](wifi-security-alert.md) | [androidJvm]<br>val [wifiSecurityAlert](wifi-security-alert.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[WifiSecurityAlert](../-wifi-security-alert/index.md)&gt;<br>LiveData containing the current high-level Wi-Fi security alert status. |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](index.md#264516373%2FFunctions%2F1606452474) | [androidJvm]<br>open fun [addCloseable](index.md#264516373%2FFunctions%2F1606452474)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)closeable: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [clearAutoVpnActionMessage](clear-auto-vpn-action-message.md) | [androidJvm]<br>fun [clearAutoVpnActionMessage](clear-auto-vpn-action-message.md)()<br>Clears the current auto-VPN action feedback message. |
| [clearRiskTransitionAlert](clear-risk-transition-alert.md) | [androidJvm]<br>fun [clearRiskTransitionAlert](clear-risk-transition-alert.md)()<br>Clears the current risk transition alert. |
| [connectVpnIfNeeded](connect-vpn-if-needed.md) | [androidJvm]<br>fun [connectVpnIfNeeded](connect-vpn-if-needed.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Ensures the VPN is connected. |
| [disconnectVpnIfConnected](disconnect-vpn-if-connected.md) | [androidJvm]<br>fun [disconnectVpnIfConnected](disconnect-vpn-if-connected.md)()<br>Ensures the VPN is disconnected. |
| [fetchLocationData](fetch-location-data.md) | [androidJvm]<br>fun [fetchLocationData](fetch-location-data.md)()<br>Fetches public IP and location metadata from an external service. Updates [ipAddress](ip-address.md) and [locationString](location-string.md) LiveData. |
| [getApplication](index.md#1696759283%2FFunctions%2F1606452474) | [androidJvm]<br>open fun &lt;[T](index.md#1696759283%2FFunctions%2F1606452474) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](index.md#1696759283%2FFunctions%2F1606452474)(): [T](index.md#1696759283%2FFunctions%2F1606452474) |
| [logout](logout.md) | [androidJvm]<br>fun [logout](logout.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Performs a complete logout by disconnecting VPN, clearing all stored credentials, resetting local preferences, and purging the local database. |
| [refreshWifiSecurityAlert](refresh-wifi-security-alert.md) | [androidJvm]<br>fun [refreshWifiSecurityAlert](refresh-wifi-security-alert.md)(lightweight: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false)<br>Triggers a manual refresh of the Wi-Fi security alert and safety assessment. |
| [removeTrustedNetwork](remove-trusted-network.md) | [androidJvm]<br>fun [removeTrustedNetwork](remove-trusted-network.md)(ssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Removes a specific network from the trusted baselines. |
| [setAutoProtectUnknownWifi](set-auto-protect-unknown-wifi.md) | [androidJvm]<br>fun [setAutoProtectUnknownWifi](set-auto-protect-unknown-wifi.md)(enabled: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html))<br>Sets whether unknown Wi-Fi networks (where security type cannot be fully detected) should trigger an automatic VPN connection. |
| [setAutoVpnDisconnectOnSafe](set-auto-vpn-disconnect-on-safe.md) | [androidJvm]<br>fun [setAutoVpnDisconnectOnSafe](set-auto-vpn-disconnect-on-safe.md)(enabled: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html))<br>Toggles whether the VPN should automatically disconnect when moving from a risky network to a safe one. |
| [setAutoVpnEnabled](set-auto-vpn-enabled.md) | [androidJvm]<br>fun [setAutoVpnEnabled](set-auto-vpn-enabled.md)(enabled: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html))<br>Sets and persists the automatic VPN activation policy. |
| [setBackgroundScanEnabled](set-background-scan-enabled.md) | [androidJvm]<br>fun [setBackgroundScanEnabled](set-background-scan-enabled.md)(enabled: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html))<br>Enables or disables periodic background Wi-Fi scanning. |
| [startWifiMonitoring](start-wifi-monitoring.md) | [androidJvm]<br>fun [startWifiMonitoring](start-wifi-monitoring.md)()<br>Registers a network callback to monitor connectivity changes and triggers Wi-Fi security refreshes. |
| [stopWifiMonitoring](stop-wifi-monitoring.md) | [androidJvm]<br>fun [stopWifiMonitoring](stop-wifi-monitoring.md)()<br>Unregisters the network callback and stops Wi-Fi monitoring. |
| [toggleCurrentNetworkTrusted](toggle-current-network-trusted.md) | [androidJvm]<br>fun [toggleCurrentNetworkTrusted](toggle-current-network-trusted.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Toggles the &quot;trusted&quot; status of the network the device is currently connected to. Saving a trusted baseline allows the app to detect unauthorized access point replacements (Evil Twins). |
| [toggleTrustedNetwork](toggle-trusted-network.md) | [androidJvm]<br>fun [toggleTrustedNetwork](toggle-trusted-network.md)(ssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), bssid: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?, securityProfile: [WifiSecurityProfile](../../com.resistine.android.ui.wifi/-wifi-security-profile/index.md)?, frequencyMhz: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)?, isCurrent: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), currentlyTrusted: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Toggles trust for a network, typically called from a list of nearby networks. |
| [toggleVpn](toggle-vpn.md) | [androidJvm]<br>fun [toggleVpn](toggle-vpn.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Toggles the VPN state. Connects if disconnected, and vice-versa. |