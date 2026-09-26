//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[ChatDao](index.md)

# ChatDao

[androidJvm]\
interface [ChatDao](index.md)

Data Access Object for interacting with stored chat messages.

## Functions

| Name | Summary |
|---|---|
| [deleteAllMessages](delete-all-messages.md) | [androidJvm]<br>abstract suspend fun [deleteAllMessages](delete-all-messages.md)()<br>Deletes all chat messages from the database. |
| [getAllMessages](get-all-messages.md) | [androidJvm]<br>abstract fun [getAllMessages](get-all-messages.md)(): Flow&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md)&gt;&gt;<br>Observes all chat messages ordered by timestamp in ascending order. |
| [getAllMessagesSync](get-all-messages-sync.md) | [androidJvm]<br>abstract suspend fun [getAllMessagesSync](get-all-messages-sync.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md)&gt;<br>Retrieves all chat messages synchronously ordered by timestamp ascending. |
| [getMessagesSync](get-messages-sync.md) | [androidJvm]<br>abstract suspend fun [getMessagesSync](get-messages-sync.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md)&gt;<br>Retrieves a single chat message synchronously if available. |
| [insertMessage](insert-message.md) | [androidJvm]<br>abstract suspend fun [insertMessage](insert-message.md)(message: [ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md))<br>Inserts a new chat message into the database. |