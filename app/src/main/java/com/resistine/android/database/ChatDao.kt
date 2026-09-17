package com.resistine.android.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.resistine.android.ui.chat.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for interacting with stored chat messages.
 */
@Dao
interface ChatDao {

    /**
     * Observes all chat messages ordered by timestamp in ascending order.
     *
     * @return A [Flow] emitting lists of [ChatMessage].
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    /**
     * Retrieves a single chat message synchronously if available.
     *
     * @return A list containing up to one [ChatMessage].
     */
    @Query("SELECT * FROM chat_messages LIMIT 1")
    suspend fun getMessagesSync(): List<ChatMessage>

    /**
     * Retrieves all chat messages synchronously ordered by timestamp ascending.
     *
     * @return List of all [ChatMessage] entities.
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesSync(): List<ChatMessage>

    /**
     * Inserts a new chat message into the database.
     *
     * @param message The [ChatMessage] to insert.
     */
    @Insert
    suspend fun insertMessage(message: ChatMessage)

    /**
     * Deletes all chat messages from the database.
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}
