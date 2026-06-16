//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[WifiRiskTransitionAlert](index.md)

# WifiRiskTransitionAlert

[androidJvm]\
data class [WifiRiskTransitionAlert](index.md)(val id: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val fromLevel: [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md), val toLevel: [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md), val score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val summary: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val worsened: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html))

Data for an alert triggered by a change in risk level.

## Constructors

| | |
|---|---|
| [WifiRiskTransitionAlert](-wifi-risk-transition-alert.md) | [androidJvm]<br>constructor(id: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), fromLevel: [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md), toLevel: [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md), score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), summary: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), worsened: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [fromLevel](from-level.md) | [androidJvm]<br>val [fromLevel](from-level.md): [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md) |
| [id](id.md) | [androidJvm]<br>val [id](id.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) |
| [score](score.md) | [androidJvm]<br>val [score](score.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [summary](summary.md) | [androidJvm]<br>val [summary](summary.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [toLevel](to-level.md) | [androidJvm]<br>val [toLevel](to-level.md): [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md) |
| [worsened](worsened.md) | [androidJvm]<br>val [worsened](worsened.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |