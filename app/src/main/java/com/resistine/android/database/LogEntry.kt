package com.resistine.android.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a log entry awaiting delivery to the Wazuh manager or local review.
 *
 * @property id Auto-generated primary key identifier.
 * @property timestamp Epoch timestamp in milliseconds when the log occurred.
 * @property message Formatted JSON or plain text log payload.
 * @property retryCount Number of upload retry attempts made for this entry.
 */
@Entity(tableName = "pending_logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val message: String,
    val retryCount: Int = 0
)
