//[app](../../../index.md)/[com.resistine.android.ui.chat](../index.md)/[ChatViewModel](index.md)

# ChatViewModel

class [ChatViewModel](index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

ViewModel for the AI Chat interface.

It manages the persistence of chat messages in the local database and coordinates requests to the AI backend (OpenAiClient).

#### Parameters

androidJvm

| | |
|---|---|
| application | The application context. |

## Constructors

| | |
|---|---|
| [ChatViewModel](-chat-view-model.md) | [androidJvm]<br>constructor(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Properties

| Name | Summary |
|---|---|
| [isTyping](is-typing.md) | [androidJvm]<br>val [isTyping](is-typing.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt;<br>LiveData boolean indicating whether the AI is currently generating a response. |
| [messages](messages.md) | [androidJvm]<br>val [messages](messages.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](../-chat-message/index.md)&gt;&gt;<br>LiveData stream of all chat messages from the database, ordered by time. |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474) | [androidJvm]<br>open fun [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)closeable: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [clearHistory](clear-history.md) | [androidJvm]<br>fun [clearHistory](clear-history.md)()<br>Clears all messages from the chat history database. |
| [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) | [androidJvm]<br>open fun &lt;[T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474)(): [T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) |
| [sendMessage](send-message.md) | [androidJvm]<br>fun [sendMessage](send-message.md)(userText: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))<br>Sends a user message, saves it to the database, and fetches an AI response. |