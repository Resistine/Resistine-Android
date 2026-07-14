package com.resistine.android.network.flow

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.EOFException

class FlowSegmentStore(
    private val directory: File,
    private val maxSegmentBytes: Long = 5L * 1024L * 1024L,
    private val maxRetainedBytes: Long = 10L * 1024L * 1024L
) {
    init {
        directory.mkdirs()
    }

    @Synchronized
    fun append(record: FlowRecord): File {
        val encoded = FlowBinaryCodec.encodeRecord(record)
        val target = currentWritableSegment(encoded.size.toLong())
        DataOutputStream(BufferedOutputStream(FileOutputStream(target, true))).use { output ->
            output.write(encoded)
        }
        enforceRetention()
        return target
    }

    @Synchronized
    fun readAllRecords(): List<FlowRecord> {
        return segmentFiles().flatMap { readRecords(it) }
    }

    @Synchronized
    fun segmentFiles(): List<File> {
        return directory.listFiles { file -> file.isFile && file.name.matches(SEGMENT_REGEX) }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    fun readRecords(segment: File): List<FlowRecord> {
        if (!segment.exists() || segment.length() < FlowBinaryCodec.FILE_HEADER_BYTES) {
            return emptyList()
        }
        val records = mutableListOf<FlowRecord>()
        DataInputStream(BufferedInputStream(FileInputStream(segment))).use { input ->
            val magic = input.readInt()
            require(magic == FlowBinaryCodec.FILE_MAGIC) { "Invalid flow segment magic: ${segment.name}" }
            input.readShort()
            input.readShort()
            input.readLong()
            while (true) {
                try {
                    val recordType = input.readByte()
                    val recordVersion = input.readByte()
                    val payloadLength = input.readInt()
                    require(payloadLength >= 0) { "Invalid record length in ${segment.name}" }
                    val payload = ByteArray(payloadLength)
                    input.readFully(payload)
                    if (recordType == FlowBinaryCodec.RECORD_TYPE_FLOW && recordVersion == FlowBinaryCodec.RECORD_VERSION) {
                        records += FlowBinaryCodec.decodeRecord(payload)
                    }
                } catch (_: EOFException) {
                    break
                }
            }
        }
        return records
    }

    private fun currentWritableSegment(nextRecordBytes: Long): File {
        val existing = segmentFiles().lastOrNull()
        if (existing != null && existing.length() + nextRecordBytes <= maxSegmentBytes) {
            return existing
        }
        return createSegment(nextSegmentIndex(existing))
    }

    private fun createSegment(index: Int): File {
        val file = File(directory, SEGMENT_FORMAT.format(index))
        DataOutputStream(BufferedOutputStream(FileOutputStream(file))).use { output ->
            FlowBinaryCodec.writeFileHeader(output)
        }
        return file
    }

    private fun nextSegmentIndex(latest: File?): Int {
        val current = latest?.name?.removePrefix("flow-")?.removeSuffix(".bin")?.toIntOrNull()
        return (current ?: 0) + 1
    }

    private fun enforceRetention() {
        var files = segmentFiles()
        var totalBytes = files.sumOf { it.length() }
        while (totalBytes > maxRetainedBytes && files.size > 1) {
            val oldest = files.first()
            val deletedBytes = oldest.length()
            if (oldest.delete()) {
                totalBytes -= deletedBytes
            } else {
                break
            }
            files = segmentFiles()
        }
    }

    companion object {
        private const val SEGMENT_FORMAT = "flow-%06d.bin"
        private val SEGMENT_REGEX = Regex("""flow-\d{6}\.bin""")
    }
}

