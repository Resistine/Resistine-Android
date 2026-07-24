package com.resistine.android.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogEntry)

    @Query("SELECT COUNT(*) FROM pending_logs")
    suspend fun countAll(): Int

    @Query(
        "DELETE FROM pending_logs WHERE id IN " +
            "(SELECT id FROM pending_logs ORDER BY timestamp ASC, id ASC LIMIT :count)"
    )
    suspend fun deleteOldest(count: Int)

    @Transaction
    suspend fun insertBounded(log: LogEntry, maxRows: Int = MAX_PENDING_ROWS): Int {
        require(maxRows > 0)
        insert(log)
        val excess = (countAll() - maxRows).coerceAtLeast(0)
        if (excess > 0) deleteOldest(excess)
        return excess
    }

    @Query("SELECT * FROM pending_logs ORDER BY timestamp ASC LIMIT 50")
    suspend fun getPendingLogs(): List<LogEntry>

    @Query("SELECT COUNT(*) FROM pending_logs WHERE message LIKE '%resistine_flow%'")
    suspend fun countPendingFlowLogs(): Int

    @Query("SELECT COUNT(*) FROM pending_logs WHERE message LIKE '%resistine_flow%'")
    fun observePendingFlowLogCount(): Flow<Int>

    @Query(
        "SELECT * FROM pending_logs " +
            "WHERE message LIKE '%\"event_type\":\"resistine_flow\"%' " +
            "ORDER BY timestamp ASC"
    )
    suspend fun getPendingFlowLogs(): List<LogEntry>

    @Delete
    suspend fun delete(log: LogEntry)

    @Query("DELETE FROM pending_logs WHERE id IN (:ids)")
    suspend fun deleteLogsByIds(ids: List<Long>)

    companion object {
        const val MAX_PENDING_ROWS = 10_000
    }
}
