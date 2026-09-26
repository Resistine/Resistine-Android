//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhRemoteReadiness](index.md)

# WazuhRemoteReadiness

[androidJvm]\
data class [WazuhRemoteReadiness](index.md)(val ready: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val issues: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;)

Result of validating whether a remote Wazuh manager endpoint is ready for communication.

## Constructors

| | |
|---|---|
| [WazuhRemoteReadiness](-wazuh-remote-readiness.md) | [androidJvm]<br>constructor(ready: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), issues: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;) |

## Properties

| Name | Summary |
|---|---|
| [issues](issues.md) | [androidJvm]<br>val [issues](issues.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt;<br>List of validation issue descriptions, if any. |
| [ready](ready.md) | [androidJvm]<br>val [ready](ready.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>True if the endpoint is fully valid and ready; false otherwise. |