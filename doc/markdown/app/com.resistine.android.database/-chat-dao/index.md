//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[ChatDao](index.md)

# ChatDao

[androidJvm]\
interface [ChatDao](index.md)

## Functions

| Name | Summary |
|---|---|
| [deleteAllMessages](delete-all-messages.md) | [androidJvm]<br>abstract suspend fun [deleteAllMessages](delete-all-messages.md)() |
| [getAllMessages](get-all-messages.md) | [androidJvm]<br>abstract fun [getAllMessages](get-all-messages.md)(): Flow&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md)&gt;&gt; |
| [getMessagesSync](get-messages-sync.md) | [androidJvm]<br>abstract suspend fun [getMessagesSync](get-messages-sync.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md)&gt; |
| [insertMessage](insert-message.md) | [androidJvm]<br>abstract suspend fun [insertMessage](insert-message.md)(message: [ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md)) |