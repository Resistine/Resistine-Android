//[app](../../../index.md)/[com.resistine.android.ui.integrity](../index.md)/[IntegrityUiState](index.md)

# IntegrityUiState

[androidJvm]\
data class [IntegrityUiState](index.md)(val status: [IntegrityCheckStatus](../-integrity-check-status/index.md) = IntegrityCheckStatus.NOT_RUN, val message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = &quot;Run a server-verified Play Integrity check for Play Protect, app recognition, and device integrity.&quot;, val appIntegrity: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val deviceIntegrity: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), val playProtectVerdict: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val appAccessRisk: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), val licensingVerdict: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val checkedAtMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null, val remediationDialogType: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null)

## Constructors

| | |
|---|---|
| [IntegrityUiState](-integrity-ui-state.md) | [androidJvm]<br>constructor(status: [IntegrityCheckStatus](../-integrity-check-status/index.md) = IntegrityCheckStatus.NOT_RUN, message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = &quot;Run a server-verified Play Integrity check for Play Protect, app recognition, and device integrity.&quot;, appIntegrity: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, deviceIntegrity: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), playProtectVerdict: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, appAccessRisk: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; = emptyList(), licensingVerdict: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, checkedAtMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? = null, remediationDialogType: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null) |

## Properties

| Name | Summary |
|---|---|
| [appAccessRisk](app-access-risk.md) | [androidJvm]<br>val [appAccessRisk](app-access-risk.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [appIntegrity](app-integrity.md) | [androidJvm]<br>val [appIntegrity](app-integrity.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [canRemediate](can-remediate.md) | [androidJvm]<br>val [canRemediate](can-remediate.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [checkedAtMillis](checked-at-millis.md) | [androidJvm]<br>val [checkedAtMillis](checked-at-millis.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)? |
| [deviceIntegrity](device-integrity.md) | [androidJvm]<br>val [deviceIntegrity](device-integrity.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [isRunning](is-running.md) | [androidJvm]<br>val [isRunning](is-running.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [licensingVerdict](licensing-verdict.md) | [androidJvm]<br>val [licensingVerdict](licensing-verdict.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [message](message.md) | [androidJvm]<br>val [message](message.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [playProtectVerdict](play-protect-verdict.md) | [androidJvm]<br>val [playProtectVerdict](play-protect-verdict.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [remediationDialogType](remediation-dialog-type.md) | [androidJvm]<br>val [remediationDialogType](remediation-dialog-type.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? |
| [status](status.md) | [androidJvm]<br>val [status](status.md): [IntegrityCheckStatus](../-integrity-check-status/index.md) |