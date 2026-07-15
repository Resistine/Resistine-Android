package com.resistine.android.service

import com.resistine.android.database.LogEntry

internal object WazuhPendingLogUploader {
    suspend fun upload(
        entries: List<LogEntry>,
        acknowledgementBatchSize: Int,
        send: suspend (LogEntry) -> Boolean,
        acknowledge: suspend (List<Long>) -> Unit,
        throttle: suspend () -> Unit
    ): Boolean {
        require(acknowledgementBatchSize > 0)
        val acknowledgedIds = ArrayList<Long>(acknowledgementBatchSize)

        suspend fun commitAcknowledgements() {
            if (acknowledgedIds.isEmpty()) return
            acknowledge(acknowledgedIds.toList())
            acknowledgedIds.clear()
        }

        for (entry in entries) {
            if (!send(entry)) {
                commitAcknowledgements()
                return false
            }
            acknowledgedIds += entry.id
            if (acknowledgedIds.size >= acknowledgementBatchSize) {
                commitAcknowledgements()
            }
            throttle()
        }

        commitAcknowledgements()
        return true
    }
}
