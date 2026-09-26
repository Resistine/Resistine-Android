//[app](../../../index.md)/[com.resistine.android.ui.wifi](../index.md)/[WifiScanSnapshot](index.md)

# WifiScanSnapshot

[androidJvm]\
data class [WifiScanSnapshot](index.md)(val results: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ScanResult](https://developer.android.com/reference/kotlin/android/net/wifi/ScanResult.html)&gt;, val freshness: [WifiScanFreshness](../-wifi-scan-freshness/index.md), val ageMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)?, val freshScanRequested: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val freshScanAccepted: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val resultsUpdated: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val requestStatus: [WifiScanRequestStatus](../-wifi-scan-request-status/index.md) = WifiScanRequestStatus.NOT_REQUESTED, val cooldownRemainingMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0, val metrics: [WifiScanEffectivenessMetrics](../-wifi-scan-effectiveness-metrics/index.md) = WifiScanEffectivenessMetrics())

## Constructors

| | |
|---|---|
| [WifiScanSnapshot](-wifi-scan-snapshot.md) | [androidJvm]<br>constructor(results: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ScanResult](https://developer.android.com/reference/kotlin/android/net/wifi/ScanResult.html)&gt;, freshness: [WifiScanFreshness](../-wifi-scan-freshness/index.md), ageMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)?, freshScanRequested: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), freshScanAccepted: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), resultsUpdated: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), requestStatus: [WifiScanRequestStatus](../-wifi-scan-request-status/index.md) = WifiScanRequestStatus.NOT_REQUESTED, cooldownRemainingMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0, metrics: [WifiScanEffectivenessMetrics](../-wifi-scan-effectiveness-metrics/index.md) = WifiScanEffectivenessMetrics()) |

## Properties

| Name | Summary |
|---|---|
| [ageMillis](age-millis.md) | [androidJvm]<br>val [ageMillis](age-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? |
| [cooldownRemainingMillis](cooldown-remaining-millis.md) | [androidJvm]<br>val [cooldownRemainingMillis](cooldown-remaining-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) |
| [freshness](freshness.md) | [androidJvm]<br>val [freshness](freshness.md): [WifiScanFreshness](../-wifi-scan-freshness/index.md) |
| [freshScanAccepted](fresh-scan-accepted.md) | [androidJvm]<br>val [freshScanAccepted](fresh-scan-accepted.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [freshScanRequested](fresh-scan-requested.md) | [androidJvm]<br>val [freshScanRequested](fresh-scan-requested.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [metrics](metrics.md) | [androidJvm]<br>val [metrics](metrics.md): [WifiScanEffectivenessMetrics](../-wifi-scan-effectiveness-metrics/index.md) |
| [requestStatus](request-status.md) | [androidJvm]<br>val [requestStatus](request-status.md): [WifiScanRequestStatus](../-wifi-scan-request-status/index.md) |
| [results](results.md) | [androidJvm]<br>val [results](results.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ScanResult](https://developer.android.com/reference/kotlin/android/net/wifi/ScanResult.html)&gt; |
| [resultsUpdated](results-updated.md) | [androidJvm]<br>val [resultsUpdated](results-updated.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |