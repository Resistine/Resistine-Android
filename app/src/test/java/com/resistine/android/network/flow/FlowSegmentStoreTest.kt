package com.resistine.android.network.flow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FlowSegmentStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `round trips binary flow records`() {
        val store = FlowSegmentStore(temporaryFolder.newFolder("flows"))
        val record = flowRecord(dstIp = "1.1.1.1")

        store.append(record)

        val records = store.readAllRecords()
        assertEquals(1, records.size)
        assertEquals(record.id, records.single().id)
        assertEquals(record.dstIp, records.single().dstIp)
        assertEquals(record.protocolEvidence.tlsSni, records.single().protocolEvidence.tlsSni)
    }

    @Test
    fun `rotates segments when size limit is reached`() {
        val store = FlowSegmentStore(
            directory = temporaryFolder.newFolder("rotating-flows"),
            maxSegmentBytes = FlowBinaryCodec.FILE_HEADER_BYTES + 240L,
            maxRetainedBytes = 10_000L
        )

        repeat(4) { index ->
            store.append(flowRecord(id = "flow-$index", dstIp = "192.0.2.$index"))
        }

        assertTrue(store.segmentFiles().size > 1)
        assertEquals(4, store.readAllRecords().size)
    }

    @Test
    fun `retains bounded segment storage`() {
        val store = FlowSegmentStore(
            directory = temporaryFolder.newFolder("retained-flows"),
            maxSegmentBytes = FlowBinaryCodec.FILE_HEADER_BYTES + 240L,
            maxRetainedBytes = 700L
        )

        repeat(8) { index ->
            store.append(flowRecord(id = "flow-$index", dstIp = "198.51.100.$index"))
        }

        assertTrue(store.segmentFiles().sumOf { it.length() } <= 700L)
        assertTrue(store.segmentFiles().isNotEmpty())
    }
}

