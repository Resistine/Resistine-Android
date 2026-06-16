//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[AutoVpnPolicy](index.md)

# AutoVpnPolicy

[androidJvm]\
enum [AutoVpnPolicy](index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[AutoVpnPolicy](index.md)&gt; 

Policies for automatic VPN connection.

## Entries

| | |
|---|---|
| [OFF](-o-f-f/index.md) | [androidJvm]<br>[OFF](-o-f-f/index.md)<br>No automatic connection. |
| [CONNECT_ON_RISK](-c-o-n-n-e-c-t_-o-n_-r-i-s-k/index.md) | [androidJvm]<br>[CONNECT_ON_RISK](-c-o-n-n-e-c-t_-o-n_-r-i-s-k/index.md)<br>Connect when a risk is detected. |
| [CONNECT_AND_DISCONNECT_ON_SAFE](-c-o-n-n-e-c-t_-a-n-d_-d-i-s-c-o-n-n-e-c-t_-o-n_-s-a-f-e/index.md) | [androidJvm]<br>[CONNECT_AND_DISCONNECT_ON_SAFE](-c-o-n-n-e-c-t_-a-n-d_-d-i-s-c-o-n-n-e-c-t_-o-n_-s-a-f-e/index.md)<br>Connect on risk, and automatically disconnect when safe. |

## Properties

| Name | Summary |
|---|---|
| [entries](entries.md) | [androidJvm]<br>val [entries](entries.md): [EnumEntries](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.enums/-enum-entries/index.html)&lt;[AutoVpnPolicy](index.md)&gt;<br>Returns a representation of an immutable list of all enum entries, in the order they're declared. |
| [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474) | [androidJvm]<br>val [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474) | [androidJvm]<br>val [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [valueOf](value-of.md) | [androidJvm]<br>fun [valueOf](value-of.md)(value: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [AutoVpnPolicy](index.md)<br>Returns the enum constant of this type with the specified name. The string must match exactly an identifier used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.) |
| [values](values.md) | [androidJvm]<br>fun [values](values.md)(): [Array](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-array/index.html)&lt;[AutoVpnPolicy](index.md)&gt;<br>Returns an array containing the constants of this enum type, in the order they're declared. |