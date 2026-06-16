//[app](../../index.md)/[com.resistine.android.ui.chat](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [ChatAdapter](-chat-adapter/index.md) | [androidJvm]<br>class [ChatAdapter](-chat-adapter/index.md)(messages: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](-chat-message/index.md)&gt;) : [RecyclerView.Adapter](https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.Adapter.html)&lt;[RecyclerView.ViewHolder](https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.ViewHolder.html)&gt; |
| [ChatFragment](-chat-fragment/index.md) | [androidJvm]<br>class [ChatFragment](-chat-fragment/index.md) : [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html) |
| [ChatMessage](-chat-message/index.md) | [androidJvm]<br>data class [ChatMessage](-chat-message/index.md)(val id: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0, val text: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val isUser: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val timestamp: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis()) |
| [ChatViewModel](-chat-view-model/index.md) | [androidJvm]<br>class [ChatViewModel](-chat-view-model/index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)<br>ViewModel for the AI Chat interface. |
| [OpenAiClient](-open-ai-client/index.md) | [androidJvm]<br>object [OpenAiClient](-open-ai-client/index.md) |