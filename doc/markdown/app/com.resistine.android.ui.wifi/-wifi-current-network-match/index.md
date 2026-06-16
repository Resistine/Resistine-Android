//[app](../../../index.md)/[com.resistine.android.ui.wifi](../index.md)/[WifiCurrentNetworkMatch](index.md)

# WifiCurrentNetworkMatch

[androidJvm]\
data class [WifiCurrentNetworkMatch](index.md)(val matchedSnapshot: [WifiScanNetworkSnapshot](../-wifi-scan-network-snapshot/index.md)?, val confidence: [WifiMatchConfidence](../-wifi-match-confidence/index.md), val detail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val sameSsidCandidates: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))

## Constructors

| | |
|---|---|
| [WifiCurrentNetworkMatch](-wifi-current-network-match.md) | [androidJvm]<br>constructor(matchedSnapshot: [WifiScanNetworkSnapshot](../-wifi-scan-network-snapshot/index.md)?, confidence: [WifiMatchConfidence](../-wifi-match-confidence/index.md), detail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), sameSsidCandidates: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [confidence](confidence.md) | [androidJvm]<br>val [confidence](confidence.md): [WifiMatchConfidence](../-wifi-match-confidence/index.md) |
| [detail](detail.md) | [androidJvm]<br>val [detail](detail.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [matchedSnapshot](matched-snapshot.md) | [androidJvm]<br>val [matchedSnapshot](matched-snapshot.md): [WifiScanNetworkSnapshot](../-wifi-scan-network-snapshot/index.md)? |
| [sameSsidCandidates](same-ssid-candidates.md) | [androidJvm]<br>val [sameSsidCandidates](same-ssid-candidates.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [isLimitedData](is-limited-data.md) | [androidJvm]<br>fun [isLimitedData](is-limited-data.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |