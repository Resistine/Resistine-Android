package com.resistine.android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.resistine.android.ui.chat.ChatMessage

/**
 * The Room database for the Resistine application.
 *
 * Manages local persistence for pending logs [LogEntry] and chat messages [ChatMessage].
 */
@Database(entities = [LogEntry::class, ChatMessage::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Retrieves the Data Access Object for pending logs.
     *
     * @return [LogDao] instance.
     */
    abstract fun logDao(): LogDao

    /**
     * Retrieves the Data Access Object for chat messages.
     *
     * @return [ChatDao] instance.
     */
    abstract fun chatDao(): ChatDao

    companion object {
        /** Singleton instance of [AppDatabase]. */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of [AppDatabase], initializing it if necessary.
         *
         * @param context Application context.
         * @return The singleton [AppDatabase] instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "resistine_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
