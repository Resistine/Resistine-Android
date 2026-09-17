package com.resistine.android.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for pending application logs awaiting upload or local review.
 */
@Dao
interface LogDao {

    /**
     * Inserts a single log entry into the pending logs table.
     *
     * @param log The [LogEntry] to insert.
     */
    @Insert
    suspend fun insert(log: LogEntry)

    /**
     * Counts the total number of pending logs in the database.
     *
     * @return The total count of pending logs.
     */
    @Query("SELECT COUNT(*) FROM pending_logs")
    suspend fun countAll(): Int

    /**
     * Deletes the oldest pending logs up to the specified count.
     *
     * @param count Number of oldest log entries to delete.
     */
    @Query(
        "DELETE FROM pending_logs WHERE id IN " +
            "(SELECT id FROM pending_logs ORDER BY timestamp ASC, id ASC LIMIT :count)"
    )
    suspend fun deleteOldest(count: Int)

    /**
     * Inserts a log entry while maintaining a bounded maximum row count by dropping oldest entries if exceeded.
     *
     * @param log The [LogEntry] to insert.
     * @param maxRows Maximum allowable rows in the pending logs table.
     * @return Number of excess rows deleted.
     */
    @Transaction
    suspend fun insertBounded(log: LogEntry, maxRows: Int = MAX_PENDING_ROWS): Int {
        require(maxRows > 0)
        insert(log)
        val excess = (countAll() - maxRows).coerceAtLeast(0)
        if (excess > 0) deleteOldest(excess)
        return excess
    }

    /**
     * Retrieves a batch of up to 50 pending logs ordered by timestamp ascending.
     *
     * @return List of [LogEntry] items.
     */
    @Query("SELECT * FROM pending_logs ORDER BY timestamp ASC LIMIT 50")
    suspend fun getPendingLogs(): List<LogEntry>

    /**
     * Counts pending logs associated with flow telemetry.
     *
     * @return Count of pending flow logs.
     */
    @Query("SELECT COUNT(*) FROM pending_logs WHERE message LIKE '%resistine_flow%'")
    suspend fun countPendingFlowLogs(): Int

    /**
     * Observes the count of pending flow logs in real time.
     *
     * @return A [Flow] emitting the count of pending flow logs.
     */
    @Query("SELECT COUNT(*) FROM pending_logs WHERE message LIKE '%resistine_flow%'")
    fun observePendingFlowLogCount(): Flow<Int>

    /**
     * Retrieves all pending flow log entries containing flow telemetry events.
     *
     * @return List of flow [LogEntry] items.
     */
    @Query(
        "SELECT * FROM pending_logs " +
            "WHERE message LIKE '%\"event_type\":\"resistine_flow\"%' " +
            "ORDER BY timestamp ASC"
    )
    suspend fun getPendingFlowLogs(): List<LogEntry>

    /**
     * Deletes a specific log entry from the database.
     *
     * @param log The [LogEntry] to delete.
     */
    @Delete
    suspend fun delete(log: LogEntry)

    /**
     * Deletes multiple log entries by their primary key IDs.
     *
     * @param ids List of log IDs to delete.
     */
    @Query("DELETE FROM pending_logs WHERE id IN (:ids)")
    suspend fun deleteLogsByIds(ids: List<Long>)

    companion object {
        /** Maximum allowed pending log rows before pruning. */
        const val MAX_PENDING_ROWS = 10_000
    }
}
