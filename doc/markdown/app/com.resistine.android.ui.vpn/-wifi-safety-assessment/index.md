//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[WifiSafetyAssessment](index.md)

# WifiSafetyAssessment

[androidJvm]\
data class [WifiSafetyAssessment](index.md)(val score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 100, val level: [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md) = WifiNetworkRiskLevel.SAFE, val summary: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = &quot;&quot;, val recommendationResId: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = R.string.wifi_security_recommendation_secure, val isLimitedData: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val isOnWifi: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val uncertainties: [Set](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-set/index.html)&lt;[WifiAssessmentUncertainty](../../com.resistine.android.ui.wifi/-wifi-assessment-uncertainty/index.md)&gt; = emptySet(), val dimensions: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[WifiScoreDimensionResult](../../com.resistine.android.ui.wifi/-wifi-score-dimension-result/index.md)&gt; = emptyList())

The results of a comprehensive safety assessment.

## Constructors

| | |
|---|---|
| [WifiSafetyAssessment](-wifi-safety-assessment.md) | [androidJvm]<br>constructor(score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 100, level: [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md) = WifiNetworkRiskLevel.SAFE, summary: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = &quot;&quot;, recommendationResId: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = R.string.wifi_security_recommendation_secure, isLimitedData: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, isOnWifi: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, uncertainties: [Set](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-set/index.html)&lt;[WifiAssessmentUncertainty](../../com.resistine.android.ui.wifi/-wifi-assessment-uncertainty/index.md)&gt; = emptySet(), dimensions: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[WifiScoreDimensionResult](../../com.resistine.android.ui.wifi/-wifi-score-dimension-result/index.md)&gt; = emptyList()) |

## Properties

| Name | Summary |
|---|---|
| [dimensions](dimensions.md) | [androidJvm]<br>val [dimensions](dimensions.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[WifiScoreDimensionResult](../../com.resistine.android.ui.wifi/-wifi-score-dimension-result/index.md)&gt; |
| [isLimitedData](is-limited-data.md) | [androidJvm]<br>val [isLimitedData](is-limited-data.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [isOnWifi](is-on-wifi.md) | [androidJvm]<br>val [isOnWifi](is-on-wifi.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [level](level.md) | [androidJvm]<br>val [level](level.md): [WifiNetworkRiskLevel](../-wifi-network-risk-level/index.md) |
| [recommendationResId](recommendation-res-id.md) | [androidJvm]<br>val [recommendationResId](recommendation-res-id.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [score](score.md) | [androidJvm]<br>val [score](score.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [summary](summary.md) | [androidJvm]<br>val [summary](summary.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [uncertainties](uncertainties.md) | [androidJvm]<br>val [uncertainties](uncertainties.md): [Set](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-set/index.html)&lt;[WifiAssessmentUncertainty](../../com.resistine.android.ui.wifi/-wifi-assessment-uncertainty/index.md)&gt; |