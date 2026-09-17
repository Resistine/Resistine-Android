package com.resistine.android.service

import com.resistine.android.database.LogEntry

/**
 * Utility responsible for uploading pending log entries to the Wazuh manager in batches with acknowledgements and throttling.
 */
internal object WazuhPendingLogUploader {

    /**
     * Uploads a list of pending log entries.
     *
     * @param entries List of [LogEntry] items to upload.
     * @param acknowledgementBatchSize Number of entries to batch before acknowledging.
     * @param send Suspending function to send a log entry, returning true on success.
     * @param acknowledge Suspending function to acknowledge successfully sent log IDs.
     * @param throttle Suspending function to throttle between uploads.
     * @return True if all entries uploaded successfully; false otherwise.
     */
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
