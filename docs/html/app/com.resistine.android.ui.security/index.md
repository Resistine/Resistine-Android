//[app](../../index.md)/[com.resistine.android.ui.security](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [SecurityCategory](-security-category/index.md) | [androidJvm]<br>enum [SecurityCategory](-security-category/index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[SecurityCategory](-security-category/index.md)&gt; <br>Categories for grouping security checks. |
| [SecurityCheckItem](-security-check-item/index.md) | [androidJvm]<br>data class [SecurityCheckItem](-security-check-item/index.md)(val id: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val category: [SecurityCategory](-security-category/index.md), val title: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val value: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val status: [SecurityStatus](-security-status/index.md), val description: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null)<br>Data model for a single security check item. |
| [SecurityChecksManager](-security-checks-manager/index.md) | [androidJvm]<br>class [SecurityChecksManager](-security-checks-manager/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Utility class to perform system security inspections. |
| [SecurityStatus](-security-status/index.md) | [androidJvm]<br>enum [SecurityStatus](-security-status/index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[SecurityStatus](-security-status/index.md)&gt; <br>Represents the result of a single security check. |