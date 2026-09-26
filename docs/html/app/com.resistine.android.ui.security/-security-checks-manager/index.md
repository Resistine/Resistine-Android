//[app](../../../index.md)/[com.resistine.android.ui.security](../index.md)/[SecurityChecksManager](index.md)

# SecurityChecksManager

[androidJvm]\
class [SecurityChecksManager](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

Utility class to perform system security inspections.

It gathers information about OS updates, lock screen settings, developer options, and radio states.

## Constructors

| | |
|---|---|
| [SecurityChecksManager](-security-checks-manager.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Functions

| Name | Summary |
|---|---|
| [calculateScore](calculate-score.md) | [androidJvm]<br>fun [calculateScore](calculate-score.md)(checks: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[SecurityCheckItem](../-security-check-item/index.md)&gt;): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Calculates an overall security score from 0 to 100 based on check results. |
| [performAllChecks](perform-all-checks.md) | [androidJvm]<br>fun [performAllChecks](perform-all-checks.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[SecurityCheckItem](../-security-check-item/index.md)&gt;<br>Executes all security checks and returns a list of items. |