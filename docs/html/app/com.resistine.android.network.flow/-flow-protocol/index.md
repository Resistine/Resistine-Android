//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowProtocol](index.md)

# FlowProtocol

[androidJvm]\
enum [FlowProtocol](index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[FlowProtocol](index.md)&gt; 

Enumeration of supported network transport protocols with their protocol numbers.

## Entries

| | |
|---|---|
| [TCP](-t-c-p/index.md) | [androidJvm]<br>[TCP](-t-c-p/index.md) |
| [UDP](-u-d-p/index.md) | [androidJvm]<br>[UDP](-u-d-p/index.md) |
| [ICMP](-i-c-m-p/index.md) | [androidJvm]<br>[ICMP](-i-c-m-p/index.md) |
| [ICMPV6](-i-c-m-p-v6/index.md) | [androidJvm]<br>[ICMPV6](-i-c-m-p-v6/index.md) |
| [UNKNOWN](-u-n-k-n-o-w-n/index.md) | [androidJvm]<br>[UNKNOWN](-u-n-k-n-o-w-n/index.md) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Properties

| Name | Summary |
|---|---|
| [code](code.md) | [androidJvm]<br>val [code](code.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Protocol number. |
| [entries](entries.md) | [androidJvm]<br>val [entries](entries.md): [EnumEntries](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.enums/-enum-entries/index.html)&lt;[FlowProtocol](index.md)&gt;<br>Returns a representation of an immutable list of all enum entries, in the order they're declared. |
| [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474) | [androidJvm]<br>val [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474) | [androidJvm]<br>val [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [valueOf](value-of.md) | [androidJvm]<br>fun [valueOf](value-of.md)(value: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [FlowProtocol](index.md)<br>Returns the enum constant of this type with the specified name. The string must match exactly an identifier used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.) |
| [values](values.md) | [androidJvm]<br>fun [values](values.md)(): [Array](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-array/index.html)&lt;[FlowProtocol](index.md)&gt;<br>Returns an array containing the constants of this enum type, in the order they're declared. |