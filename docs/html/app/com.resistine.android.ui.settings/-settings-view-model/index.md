//[app](../../../index.md)/[com.resistine.android.ui.settings](../index.md)/[SettingsViewModel](index.md)

# SettingsViewModel

[androidJvm]\
class [SettingsViewModel](index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

## Constructors

| | |
|---|---|
| [SettingsViewModel](-settings-view-model.md) | [androidJvm]<br>constructor(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Properties

| Name | Summary |
|---|---|
| [uiState](ui-state.md) | [androidJvm]<br>val [uiState](ui-state.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[SettingsUiState](../-settings-ui-state/index.md)&gt; |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474) | [androidJvm]<br>open fun [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)closeable: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [consumeMessage](consume-message.md) | [androidJvm]<br>fun [consumeMessage](consume-message.md)() |
| [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) | [androidJvm]<br>open fun &lt;[T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474)(): [T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) |
| [restoreRegistrationDefault](restore-registration-default.md) | [androidJvm]<br>fun [restoreRegistrationDefault](restore-registration-default.md)() |
| [saveConfiguration](save-configuration.md) | [androidJvm]<br>fun [saveConfiguration](save-configuration.md)(configuration: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |