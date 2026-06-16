//[app](../../../index.md)/[com.resistine.android.ui.apps](../index.md)/[AppScanResult](index.md)

# AppScanResult

[androidJvm]\
data class [AppScanResult](index.md)(val score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val verdict: [RiskVerdict](../-risk-verdict/index.md), val badges: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Badge](../-badge/index.md)&gt;, val apkSha256: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;, val highRiskPermissions: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList())

## Constructors

| | |
|---|---|
| [AppScanResult](-app-scan-result.md) | [androidJvm]<br>constructor(score: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), verdict: [RiskVerdict](../-risk-verdict/index.md), badges: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Badge](../-badge/index.md)&gt;, apkSha256: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;, highRiskPermissions: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList()) |

## Properties

| Name | Summary |
|---|---|
| [apkSha256](apk-sha256.md) | [androidJvm]<br>val [apkSha256](apk-sha256.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [badges](badges.md) | [androidJvm]<br>val [badges](badges.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Badge](../-badge/index.md)&gt; |
| [highRiskPermissions](high-risk-permissions.md) | [androidJvm]<br>val [highRiskPermissions](high-risk-permissions.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [score](score.md) | [androidJvm]<br>val [score](score.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [verdict](verdict.md) | [androidJvm]<br>val [verdict](verdict.md): [RiskVerdict](../-risk-verdict/index.md) |