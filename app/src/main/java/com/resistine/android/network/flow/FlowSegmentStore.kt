package com.resistine.android.network.flow

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.EOFException

/**
 * Storage manager for persisting and retaining flow segment binary files on disk.
 *
 * @param directory Directory where segment files are stored.
 * @param maxSegmentBytes Maximum size of a single segment file in bytes.
 * @param maxRetainedBytes Maximum total retained storage size in bytes before older segments are purged.
 */
class FlowSegmentStore(
    private val directory: File,
    private val maxSegmentBytes: Long = 5L * 1024L * 1024L,
    private val maxRetainedBytes: Long = 10L * 1024L * 1024L
) {
    init {
        directory.mkdirs()
    }

    /**
     * Appends a flow record to the current active segment file, enforcing retention limits.
     *
     * @param record The [FlowRecord] to append.
     * @return The [File] where the record was appended.
     */
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

    /**
     * Reads all flow records across all stored segment files.
     *
     * @return List of all stored [FlowRecord] items.
     */
    @Synchronized
    fun readAllRecords(): List<FlowRecord> {
        return segmentFiles().flatMap { readRecords(it) }
    }

    /**
     * Returns a list of all segment files ordered by name.
     *
     * @return List of segment [File] objects.
     */
    @Synchronized
    fun segmentFiles(): List<File> {
        return directory.listFiles { file -> file.isFile && file.name.matches(SEGMENT_REGEX) }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    /**
     * Reads all flow records from a specific segment file.
     *
     * @param segment Segment [File].
     * @return List of [FlowRecord] items contained in the segment.
     */
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

    /**
     * Determines the current writable segment file, creating a new one if the latest has reached capacity.
     *
     * @param nextRecordBytes Size of the record to be written.
     * @return Writable segment [File].
     */
    private fun currentWritableSegment(nextRecordBytes: Long): File {
        val existing = segmentFiles().lastOrNull()
        if (existing != null && existing.length() + nextRecordBytes <= maxSegmentBytes) {
            return existing
        }
        return createSegment(nextSegmentIndex(existing))
    }

    /**
     * Creates a new segment file with the given index.
     *
     * @param index Segment index number.
     * @return Newly created segment [File].
     */
    private fun createSegment(index: Int): File {
        val file = File(directory, SEGMENT_FORMAT.format(index))
        DataOutputStream(BufferedOutputStream(FileOutputStream(file))).use { output ->
            FlowBinaryCodec.writeFileHeader(output)
        }
        return file
    }

    /**
     * Computes the next segment index.
     *
     * @param latest Latest existing segment file.
     * @return Next index integer.
     */
    private fun nextSegmentIndex(latest: File?): Int {
        val current = latest?.name?.removePrefix("flow-")?.removeSuffix(".bin")?.toIntOrNull()
        return (current ?: 0) + 1
    }

    /**
     * Enforces storage retention limits by deleting the oldest segment files if total size exceeds [maxRetainedBytes].
     */
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
