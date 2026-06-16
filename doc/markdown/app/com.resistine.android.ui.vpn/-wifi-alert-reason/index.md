//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[WifiAlertReason](index.md)

# WifiAlertReason

[androidJvm]\
enum [WifiAlertReason](index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[WifiAlertReason](index.md)&gt; 

Specific reasons for a Wi-Fi security alert.

## Entries

| | |
|---|---|
| [UNAVAILABLE](-u-n-a-v-a-i-l-a-b-l-e/index.md) | [androidJvm]<br>[UNAVAILABLE](-u-n-a-v-a-i-l-a-b-l-e/index.md)<br>Monitoring service is unavailable. |
| [NO_NETWORK](-n-o_-n-e-t-w-o-r-k/index.md) | [androidJvm]<br>[NO_NETWORK](-n-o_-n-e-t-w-o-r-k/index.md)<br>No active network connection. |
| [NOT_WIFI](-n-o-t_-w-i-f-i/index.md) | [androidJvm]<br>[NOT_WIFI](-n-o-t_-w-i-f-i/index.md)<br>Device is on a non-Wi-Fi network (e.g., Cellular). |
| [CAPTIVE_PORTAL](-c-a-p-t-i-v-e_-p-o-r-t-a-l/index.md) | [androidJvm]<br>[CAPTIVE_PORTAL](-c-a-p-t-i-v-e_-p-o-r-t-a-l/index.md)<br>Network has a captive portal (sign-in required). |
| [UNVALIDATED](-u-n-v-a-l-i-d-a-t-e-d/index.md) | [androidJvm]<br>[UNVALIDATED](-u-n-v-a-l-i-d-a-t-e-d/index.md)<br>Connectivity is established but not yet validated. |
| [LEGACY_NO_SECURITY_TYPE](-l-e-g-a-c-y_-n-o_-s-e-c-u-r-i-t-y_-t-y-p-e/index.md) | [androidJvm]<br>[LEGACY_NO_SECURITY_TYPE](-l-e-g-a-c-y_-n-o_-s-e-c-u-r-i-t-y_-t-y-p-e/index.md)<br>Android version is too old for detailed security detection. |
| [MISSING_PERMISSION](-m-i-s-s-i-n-g_-p-e-r-m-i-s-s-i-o-n/index.md) | [androidJvm]<br>[MISSING_PERMISSION](-m-i-s-s-i-n-g_-p-e-r-m-i-s-s-i-o-n/index.md)<br>Location permission missing (required for SSID/BSSID). |
| [LOCATION_SERVICES_DISABLED](-l-o-c-a-t-i-o-n_-s-e-r-v-i-c-e-s_-d-i-s-a-b-l-e-d/index.md) | [androidJvm]<br>[LOCATION_SERVICES_DISABLED](-l-o-c-a-t-i-o-n_-s-e-r-v-i-c-e-s_-d-i-s-a-b-l-e-d/index.md)<br>Location services disabled (required for Wi-Fi scanning). |
| [OPEN_OR_WEP](-o-p-e-n_-o-r_-w-e-p/index.md) | [androidJvm]<br>[OPEN_OR_WEP](-o-p-e-n_-o-r_-w-e-p/index.md)<br>Network is open or uses obsolete WEP encryption. |
| [UNKNOWN_SECURITY](-u-n-k-n-o-w-n_-s-e-c-u-r-i-t-y/index.md) | [androidJvm]<br>[UNKNOWN_SECURITY](-u-n-k-n-o-w-n_-s-e-c-u-r-i-t-y/index.md)<br>Encryption details could not be determined. |
| [WEAK_LEGACY_CIPHER](-w-e-a-k_-l-e-g-a-c-y_-c-i-p-h-e-r/index.md) | [androidJvm]<br>[WEAK_LEGACY_CIPHER](-w-e-a-k_-l-e-g-a-c-y_-c-i-p-h-e-r/index.md)<br>Legacy weak ciphers (like TKIP) are in use. |
| [WPS_ENABLED](-w-p-s_-e-n-a-b-l-e-d/index.md) | [androidJvm]<br>[WPS_ENABLED](-w-p-s_-e-n-a-b-l-e-d/index.md)<br>WPS is enabled, which is a security vulnerability. |
| [TRUSTED_BSSID_MISMATCH](-t-r-u-s-t-e-d_-b-s-s-i-d_-m-i-s-m-a-t-c-h/index.md) | [androidJvm]<br>[TRUSTED_BSSID_MISMATCH](-t-r-u-s-t-e-d_-b-s-s-i-d_-m-i-s-m-a-t-c-h/index.md)<br>Current BSSID does not match the trusted baseline. |
| [TRUSTED_FINGERPRINT_CHANGED](-t-r-u-s-t-e-d_-f-i-n-g-e-r-p-r-i-n-t_-c-h-a-n-g-e-d/index.md) | [androidJvm]<br>[TRUSTED_FINGERPRINT_CHANGED](-t-r-u-s-t-e-d_-f-i-n-g-e-r-p-r-i-n-t_-c-h-a-n-g-e-d/index.md)<br>Security fingerprint has changed from the trusted baseline. |
| [TRUSTED_SECURITY_DOWNGRADE](-t-r-u-s-t-e-d_-s-e-c-u-r-i-t-y_-d-o-w-n-g-r-a-d-e/index.md) | [androidJvm]<br>[TRUSTED_SECURITY_DOWNGRADE](-t-r-u-s-t-e-d_-s-e-c-u-r-i-t-y_-d-o-w-n-g-r-a-d-e/index.md)<br>Security is weaker than what was recorded in the baseline. |
| [SECURE](-s-e-c-u-r-e/index.md) | [androidJvm]<br>[SECURE](-s-e-c-u-r-e/index.md)<br>Network appears secure and matches baseline (if any). |

## Properties

| Name | Summary |
|---|---|
| [entries](entries.md) | [androidJvm]<br>val [entries](entries.md): [EnumEntries](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.enums/-enum-entries/index.html)&lt;[WifiAlertReason](index.md)&gt;<br>Returns a representation of an immutable list of all enum entries, in the order they're declared. |
| [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474) | [androidJvm]<br>val [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474) | [androidJvm]<br>val [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [valueOf](value-of.md) | [androidJvm]<br>fun [valueOf](value-of.md)(value: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [WifiAlertReason](index.md)<br>Returns the enum constant of this type with the specified name. The string must match exactly an identifier used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.) |
| [values](values.md) | [androidJvm]<br>fun [values](values.md)(): [Array](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-array/index.html)&lt;[WifiAlertReason](index.md)&gt;<br>Returns an array containing the constants of this enum type, in the order they're declared. |