//[app](../../../index.md)/[com.resistine.android.ui.integrity](../index.md)/[PlayIntegrityRepository](index.md)

# PlayIntegrityRepository

[androidJvm]\
class [PlayIntegrityRepository](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), cloudProjectNumber: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER, verificationUrl: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = BuildConfig.PLAY_INTEGRITY_VERIFICATION_URL)

## Constructors

| | |
|---|---|
| [PlayIntegrityRepository](-play-integrity-repository.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), cloudProjectNumber: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER, verificationUrl: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) = BuildConfig.PLAY_INTEGRITY_VERIFICATION_URL) |

## Properties

| Name | Summary |
|---|---|
| [isConfigured](is-configured.md) | [androidJvm]<br>val [isConfigured](is-configured.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |

## Functions

| Name | Summary |
|---|---|
| [check](check.md) | [androidJvm]<br>suspend fun [check](check.md)(): [IntegrityUiState](../-integrity-ui-state/index.md) |
| [initialState](initial-state.md) | [androidJvm]<br>fun [initialState](initial-state.md)(): [IntegrityUiState](../-integrity-ui-state/index.md) |
| [showRemediation](show-remediation.md) | [androidJvm]<br>suspend fun [showRemediation](show-remediation.md)(activity: [Activity](https://developer.android.com/reference/kotlin/android/app/Activity.html)): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |