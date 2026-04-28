package com.resistine.android.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val message: String,
    val retryCount: Int = 0
)