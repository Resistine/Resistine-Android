package com.resistine.android.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.resistine.android.ui.chat.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages LIMIT 1")
    suspend fun getMessagesSync(): List<ChatMessage>

    @Insert
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}
