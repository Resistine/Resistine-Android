//[app](../../../index.md)/[com.resistine.android.ui.security](../index.md)/[SecurityStatus](index.md)

# SecurityStatus

[androidJvm]\
enum [SecurityStatus](index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[SecurityStatus](index.md)&gt; 

Represents the result of a single security check.

## Entries

| | |
|---|---|
| [SAFE](-s-a-f-e/index.md) | [androidJvm]<br>[SAFE](-s-a-f-e/index.md)<br>The check passed and the state is secure. |
| [WARNING](-w-a-r-n-i-n-g/index.md) | [androidJvm]<br>[WARNING](-w-a-r-n-i-n-g/index.md)<br>A potential risk or sub-optimal configuration was detected. |
| [DANGER](-d-a-n-g-e-r/index.md) | [androidJvm]<br>[DANGER](-d-a-n-g-e-r/index.md)<br>A significant security risk was detected. |
| [INFO](-i-n-f-o/index.md) | [androidJvm]<br>[INFO](-i-n-f-o/index.md)<br>The status could not be determined or requires user manual check. |

## Properties

| Name | Summary |
|---|---|
| [entries](entries.md) | [androidJvm]<br>val [entries](entries.md): [EnumEntries](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.enums/-enum-entries/index.html)&lt;[SecurityStatus](index.md)&gt;<br>Returns a representation of an immutable list of all enum entries, in the order they're declared. |
| [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474) | [androidJvm]<br>val [name](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-372974862%2FProperties%2F1606452474): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474) | [androidJvm]<br>val [ordinal](../../com.resistine.android.ui.wifi/-wifi-risk-dimension/-v-i-s-i-b-i-l-i-t-y/index.md#-739389684%2FProperties%2F1606452474): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [getPenalty](get-penalty.md) | [androidJvm]<br>fun [getPenalty](get-penalty.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Returns a penalty value associated with each status for scoring. |
| [valueOf](value-of.md) | [androidJvm]<br>fun [valueOf](value-of.md)(value: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)): [SecurityStatus](index.md)<br>Returns the enum constant of this type with the specified name. The string must match exactly an identifier used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.) |
| [values](values.md) | [androidJvm]<br>fun [values](values.md)(): [Array](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-array/index.html)&lt;[SecurityStatus](index.md)&gt;<br>Returns an array containing the constants of this enum type, in the order they're declared. |