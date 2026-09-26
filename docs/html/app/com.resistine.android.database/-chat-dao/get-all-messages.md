//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[ChatDao](index.md)/[getAllMessages](get-all-messages.md)

# getAllMessages

[androidJvm]\
abstract fun [getAllMessages](get-all-messages.md)(): Flow&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md)&gt;&gt;

Observes all chat messages ordered by timestamp in ascending order.

#### Return

A Flow emitting lists of [ChatMessage](../../com.resistine.android.ui.chat/-chat-message/index.md).