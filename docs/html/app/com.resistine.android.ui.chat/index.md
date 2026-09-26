//[app](../../index.md)/[com.resistine.android.ui.chat](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [ChatFragment](-chat-fragment/index.md) | [androidJvm]<br>class [ChatFragment](-chat-fragment/index.md) : [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html) |
| [ChatMessage](-chat-message/index.md) | [androidJvm]<br>data class [ChatMessage](-chat-message/index.md)(val id: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val text: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val isUser: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val timestamp: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis()) |
| [ChatUiState](-chat-ui-state/index.md) | [androidJvm]<br>data class [ChatUiState](-chat-ui-state/index.md)(val messages: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](-chat-message/index.md)&gt; = emptyList(), val isTyping: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false, val errorMessage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null) |
| [ChatViewModel](-chat-view-model/index.md) | [androidJvm]<br>class [ChatViewModel](-chat-view-model/index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html) |
| [OpenAiClient](-open-ai-client/index.md) | [androidJvm]<br>object [OpenAiClient](-open-ai-client/index.md) |