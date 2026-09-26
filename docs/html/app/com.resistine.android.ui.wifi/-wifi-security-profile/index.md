//[app](../../../index.md)/[com.resistine.android.ui.wifi](../index.md)/[WifiSecurityProfile](index.md)

# WifiSecurityProfile

[androidJvm]\
data class [WifiSecurityProfile](index.md)(val mode: [WifiSecurityMode](../-wifi-security-mode/index.md), val pmfState: [WifiPmfState](../-wifi-pmf-state/index.md) = WifiPmfState.UNKNOWN, val hasWeakCipher: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val hasWps: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false)

## Constructors

| | |
|---|---|
| [WifiSecurityProfile](-wifi-security-profile.md) | [androidJvm]<br>constructor(mode: [WifiSecurityMode](../-wifi-security-mode/index.md), pmfState: [WifiPmfState](../-wifi-pmf-state/index.md) = WifiPmfState.UNKNOWN, hasWeakCipher: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, hasWps: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false) |

## Properties

| Name | Summary |
|---|---|
| [broadType](broad-type.md) | [androidJvm]<br>val [broadType](broad-type.md): [WifiSecurityType](../../com.resistine.android.ui.vpn/-wifi-security-type/index.md) |
| [hasWeakCipher](has-weak-cipher.md) | [androidJvm]<br>val [hasWeakCipher](has-weak-cipher.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [hasWps](has-wps.md) | [androidJvm]<br>val [hasWps](has-wps.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [mode](mode.md) | [androidJvm]<br>val [mode](mode.md): [WifiSecurityMode](../-wifi-security-mode/index.md) |
| [pmfState](pmf-state.md) | [androidJvm]<br>val [pmfState](pmf-state.md): [WifiPmfState](../-wifi-pmf-state/index.md) |

## Functions

| Name | Summary |
|---|---|
| [equivalentTo](equivalent-to.md) | [androidJvm]<br>fun [equivalentTo](equivalent-to.md)(other: [WifiSecurityProfile](index.md)?): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [pmfRank](pmf-rank.md) | [androidJvm]<br>fun [pmfRank](pmf-rank.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [storageKey](storage-key.md) | [androidJvm]<br>fun [storageKey](storage-key.md)(): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [strengthRank](strength-rank.md) | [androidJvm]<br>fun [strengthRank](strength-rank.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |