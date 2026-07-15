package com.resistine.android.service

import com.resistine.android.database.LogEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WazuhPendingLogUploaderTest {
    @Test
    fun `acknowledges successful uploads in bounded batches`() = runBlocking {
        val entries = entries(25)
        val acknowledged = mutableListOf<List<Long>>()
        var throttleCalls = 0

        val uploaded = WazuhPendingLogUploader.upload(
            entries = entries,
            acknowledgementBatchSize = 10,
            send = { true },
            acknowledge = { acknowledged += it },
            throttle = { throttleCalls++ }
        )

        assertTrue(uploaded)
        assertEquals(
            listOf((1L..10L).toList(), (11L..20L).toList(), (21L..25L).toList()),
            acknowledged
        )
        assertEquals(25, throttleCalls)
    }

    @Test
    fun `acknowledges progress before returning a send failure`() = runBlocking {
        val entries = entries(25)
        val acknowledged = mutableListOf<List<Long>>()
        var attempts = 0
        var throttleCalls = 0

        val uploaded = WazuhPendingLogUploader.upload(
            entries = entries,
            acknowledgementBatchSize = 10,
            send = {
                attempts++
                attempts != 13
            },
            acknowledge = { acknowledged += it },
            throttle = { throttleCalls++ }
        )

        assertFalse(uploaded)
        assertEquals(listOf((1L..10L).toList(), listOf(11L, 12L)), acknowledged)
        assertEquals(13, attempts)
        assertEquals(12, throttleCalls)
    }

    private fun entries(count: Int): List<LogEntry> {
        return (1L..count.toLong()).map { id ->
            LogEntry(id = id, timestamp = id, message = "flow-$id")
        }
    }
}
