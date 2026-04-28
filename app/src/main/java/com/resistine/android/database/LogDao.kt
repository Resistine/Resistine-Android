package com.resistine.android.database

import androidx.room.*

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogEntry)

    @Query("SELECT * FROM pending_logs ORDER BY timestamp ASC LIMIT 50")
    suspend fun getPendingLogs(): List<LogEntry>

    @Delete
    suspend fun delete(log: LogEntry)

    @Query("DELETE FROM pending_logs WHERE id IN (:ids)")
    suspend fun deleteLogsByIds(ids: List<Long>)
}