//[app](../../../index.md)/[com.resistine.android.ui.wifi](../index.md)/[WifiTrustAssessment](index.md)

# WifiTrustAssessment

[androidJvm]\
data class [WifiTrustAssessment](index.md)(val status: [WifiTrustBaselineStatus](../-wifi-trust-baseline-status/index.md), val observationCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val threshold: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0)

## Constructors

| | |
|---|---|
| [WifiTrustAssessment](-wifi-trust-assessment.md) | [androidJvm]<br>constructor(status: [WifiTrustBaselineStatus](../-wifi-trust-baseline-status/index.md), observationCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, threshold: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0) |

## Properties

| Name | Summary |
|---|---|
| [observationCount](observation-count.md) | [androidJvm]<br>val [observationCount](observation-count.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [status](status.md) | [androidJvm]<br>val [status](status.md): [WifiTrustBaselineStatus](../-wifi-trust-baseline-status/index.md) |
| [threshold](threshold.md) | [androidJvm]<br>val [threshold](threshold.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |

## Functions

| Name | Summary |
|---|---|
| [isStable](is-stable.md) | [androidJvm]<br>fun [isStable](is-stable.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [shouldFlagDowngrade](should-flag-downgrade.md) | [androidJvm]<br>fun [shouldFlagDowngrade](should-flag-downgrade.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [shouldFlagFingerprintChange](should-flag-fingerprint-change.md) | [androidJvm]<br>fun [shouldFlagFingerprintChange](should-flag-fingerprint-change.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |