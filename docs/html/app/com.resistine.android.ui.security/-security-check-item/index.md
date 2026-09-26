//[app](../../../index.md)/[com.resistine.android.ui.security](../index.md)/[SecurityCheckItem](index.md)

# SecurityCheckItem

[androidJvm]\
data class [SecurityCheckItem](index.md)(val id: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val category: [SecurityCategory](../-security-category/index.md), val title: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val value: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val status: [SecurityStatus](../-security-status/index.md), val description: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null)

Data model for a single security check item.

## Constructors

| | |
|---|---|
| [SecurityCheckItem](-security-check-item.md) | [androidJvm]<br>constructor(id: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), category: [SecurityCategory](../-security-category/index.md), title: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), value: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), status: [SecurityStatus](../-security-status/index.md), description: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [category](category.md) | [androidJvm]<br>val [category](category.md): [SecurityCategory](../-security-category/index.md)<br>The group this check belongs to. |
| [description](description.md) | [androidJvm]<br>val [description](description.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Optional detailed explanation or remediation steps. |
| [id](id.md) | [androidJvm]<br>val [id](id.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Unique identifier for the check. |
| [status](status.md) | [androidJvm]<br>val [status](status.md): [SecurityStatus](../-security-status/index.md)<br>Severity level of the current state. |
| [title](title.md) | [androidJvm]<br>val [title](title.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Human-readable name of the check. |
| [value](value.md) | [androidJvm]<br>val [value](value.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Current detected value or state description. |