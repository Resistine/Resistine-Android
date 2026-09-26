//[app](../../../index.md)/[com.resistine.android.ui.apps](../index.md)/[AppsUiState](index.md)

# AppsUiState

[androidJvm]\
data class [AppsUiState](index.md)(val apps: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[AppEntry](../-app-entry/index.md)&gt; = emptyList(), val summary: [ScanSummary](../-scan-summary/index.md) = ScanSummary(
        total = 0,
        noConcern = 0,
        review = 0,
        urgentReview = 0,
        lastScanAt = null,
        isScanned = false
    ), val isScanning: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val scanProgress: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, val showSystemApps: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false)

## Constructors

| | |
|---|---|
| [AppsUiState](-apps-ui-state.md) | [androidJvm]<br>constructor(apps: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[AppEntry](../-app-entry/index.md)&gt; = emptyList(), summary: [ScanSummary](../-scan-summary/index.md) = ScanSummary(         total = 0,         noConcern = 0,         review = 0,         urgentReview = 0,         lastScanAt = null,         isScanned = false     ), isScanning: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, scanProgress: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, showSystemApps: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false) |

## Properties

| Name | Summary |
|---|---|
| [apps](apps.md) | [androidJvm]<br>val [apps](apps.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[AppEntry](../-app-entry/index.md)&gt; |
| [isScanning](is-scanning.md) | [androidJvm]<br>val [isScanning](is-scanning.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [scanProgress](scan-progress.md) | [androidJvm]<br>val [scanProgress](scan-progress.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? |
| [showSystemApps](show-system-apps.md) | [androidJvm]<br>val [showSystemApps](show-system-apps.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [summary](summary.md) | [androidJvm]<br>val [summary](summary.md): [ScanSummary](../-scan-summary/index.md) |