//[app](../../../index.md)/[com.resistine.android.ui.wifi](../index.md)/[WifiScanEffectivenessMetrics](index.md)

# WifiScanEffectivenessMetrics

[androidJvm]\
data class [WifiScanEffectivenessMetrics](index.md)(val activeAttempts: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val updatedResults: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val rejectedRequests: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val timedOutRequests: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val cooldownDeferrals: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val lastRequestDurationMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null)

## Constructors

| | |
|---|---|
| [WifiScanEffectivenessMetrics](-wifi-scan-effectiveness-metrics.md) | [androidJvm]<br>constructor(activeAttempts: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, updatedResults: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, rejectedRequests: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, timedOutRequests: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, cooldownDeferrals: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, lastRequestDurationMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [activeAttempts](active-attempts.md) | [androidJvm]<br>val [activeAttempts](active-attempts.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [cooldownDeferrals](cooldown-deferrals.md) | [androidJvm]<br>val [cooldownDeferrals](cooldown-deferrals.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [lastRequestDurationMillis](last-request-duration-millis.md) | [androidJvm]<br>val [lastRequestDurationMillis](last-request-duration-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? |
| [rejectedRequests](rejected-requests.md) | [androidJvm]<br>val [rejectedRequests](rejected-requests.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [timedOutRequests](timed-out-requests.md) | [androidJvm]<br>val [timedOutRequests](timed-out-requests.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [updatedResults](updated-results.md) | [androidJvm]<br>val [updatedResults](updated-results.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [record](record.md) | [androidJvm]<br>fun [record](record.md)(status: [WifiScanRequestStatus](../-wifi-scan-request-status/index.md), durationMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [WifiScanEffectivenessMetrics](index.md) |