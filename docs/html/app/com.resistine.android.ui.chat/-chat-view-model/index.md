//[app](../../../index.md)/[com.resistine.android.ui.chat](../index.md)/[ChatViewModel](index.md)

# ChatViewModel

[androidJvm]\
class [ChatViewModel](index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

## Constructors

| | |
|---|---|
| [ChatViewModel](-chat-view-model.md) | [androidJvm]<br>constructor(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Properties

| Name | Summary |
|---|---|
| [uiState](ui-state.md) | [androidJvm]<br>val [uiState](ui-state.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[ChatUiState](../-chat-ui-state/index.md)&gt; |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474) | [androidJvm]<br>open fun [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)closeable: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [cancelResponse](cancel-response.md) | [androidJvm]<br>fun [cancelResponse](cancel-response.md)() |
| [clearHistory](clear-history.md) | [androidJvm]<br>fun [clearHistory](clear-history.md)() |
| [consumeError](consume-error.md) | [androidJvm]<br>fun [consumeError](consume-error.md)() |
| [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) | [androidJvm]<br>open fun &lt;[T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474)(): [T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) |
| [retryLastResponse](retry-last-response.md) | [androidJvm]<br>fun [retryLastResponse](retry-last-response.md)() |
| [sendMessage](send-message.md) | [androidJvm]<br>fun [sendMessage](send-message.md)(userText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |