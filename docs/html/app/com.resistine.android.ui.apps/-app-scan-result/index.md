//[app](../../../index.md)/[com.resistine.android.ui.apps](../index.md)/[AppScanResult](index.md)

# AppScanResult

[androidJvm]\
data class [AppScanResult](index.md)(val score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val verdict: [RiskVerdict](../-risk-verdict/index.md), val badges: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Badge](../-badge/index.md)&gt;, val highRiskPermissions: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), val provenance: [InstallProvenance](../-install-provenance/index.md) = InstallProvenance.UNKNOWN, val installerPackage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val identityConfidence: [AppIdentityConfidence](../-app-identity-confidence/index.md) = AppIdentityConfidence.UNVERIFIED, val scannedAtMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0)

## Constructors

| | |
|---|---|
| [AppScanResult](-app-scan-result.md) | [androidJvm]<br>constructor(score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), verdict: [RiskVerdict](../-risk-verdict/index.md), badges: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Badge](../-badge/index.md)&gt;, highRiskPermissions: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), provenance: [InstallProvenance](../-install-provenance/index.md) = InstallProvenance.UNKNOWN, installerPackage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, identityConfidence: [AppIdentityConfidence](../-app-identity-confidence/index.md) = AppIdentityConfidence.UNVERIFIED, scannedAtMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0) |

## Properties

| Name | Summary |
|---|---|
| [badges](badges.md) | [androidJvm]<br>val [badges](badges.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Badge](../-badge/index.md)&gt; |
| [highRiskPermissions](high-risk-permissions.md) | [androidJvm]<br>val [highRiskPermissions](high-risk-permissions.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [identityConfidence](identity-confidence.md) | [androidJvm]<br>val [identityConfidence](identity-confidence.md): [AppIdentityConfidence](../-app-identity-confidence/index.md) |
| [installerPackage](installer-package.md) | [androidJvm]<br>val [installerPackage](installer-package.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [provenance](provenance.md) | [androidJvm]<br>val [provenance](provenance.md): [InstallProvenance](../-install-provenance/index.md) |
| [scannedAtMillis](scanned-at-millis.md) | [androidJvm]<br>val [scannedAtMillis](scanned-at-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) |
| [score](score.md) | [androidJvm]<br>val [score](score.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [verdict](verdict.md) | [androidJvm]<br>val [verdict](verdict.md): [RiskVerdict](../-risk-verdict/index.md) |