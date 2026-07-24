package com.resistine.android.network.forwarding

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

data class PacketPipelineSnapshot(
    val packetsRead: Long,
    val bytesRead: Long,
    val forwardQueueDepth: Int,
    val segmentQueueDepth: Int,
    val wazuhQueueDepth: Int,
    val nativeTelemetryQueueDepth: Long,
    val nativeTelemetryQueueHighWater: Long,
    val forwardQueueDropped: Long,
    val forwardQueueDiscardedOnStop: Long,
    val parserSuccess: Long,
    val parserFailure: Long,
    val flowsFlushed: Long,
    val segmentQueueDropped: Long,
    val segmentWriteFailures: Long,
    val wazuhQueueDropped: Long,
    val packetsRejectedAfterClose: Long,
    val nativeTelemetryDropped: Long,
    val telemetryReaderFailures: Long
) {
    companion object {
        fun empty() = PacketPipelineSnapshot(
            packetsRead = 0L,
            bytesRead = 0L,
            forwardQueueDepth = 0,
            segmentQueueDepth = 0,
            wazuhQueueDepth = 0,
            nativeTelemetryQueueDepth = 0L,
            nativeTelemetryQueueHighWater = 0L,
            forwardQueueDropped = 0L,
            forwardQueueDiscardedOnStop = 0L,
            parserSuccess = 0L,
            parserFailure = 0L,
            flowsFlushed = 0L,
            segmentQueueDropped = 0L,
            segmentWriteFailures = 0L,
            wazuhQueueDropped = 0L,
            packetsRejectedAfterClose = 0L,
            nativeTelemetryDropped = 0L,
            telemetryReaderFailures = 0L
        )
    }
}

class PacketPipelineStats {
    private val packetsRead = AtomicLong(0L)
    private val bytesRead = AtomicLong(0L)
    private val forwardQueueDepth = AtomicInteger(0)
    private val segmentQueueDepth = AtomicInteger(0)
    private val wazuhQueueDepth = AtomicInteger(0)
    private val nativeTelemetryQueueDepth = AtomicLong(0L)
    private val nativeTelemetryQueueHighWater = AtomicLong(0L)
    private val forwardQueueDropped = AtomicLong(0L)
    private val forwardQueueDiscardedOnStop = AtomicLong(0L)
    private val parserSuccess = AtomicLong(0L)
    private val parserFailure = AtomicLong(0L)
    private val flowsFlushed = AtomicLong(0L)
    private val segmentQueueDropped = AtomicLong(0L)
    private val segmentWriteFailures = AtomicLong(0L)
    private val wazuhQueueDropped = AtomicLong(0L)
    private val packetsRejectedAfterClose = AtomicLong(0L)
    private val nativeTelemetryDropped = AtomicLong(0L)
    private val telemetryReaderFailures = AtomicLong(0L)

    fun recordRead(bytes: Int) {
        packetsRead.incrementAndGet()
        bytesRead.addAndGet(bytes.toLong().coerceAtLeast(0L))
    }

    fun recordForwardEnqueued() {
        forwardQueueDepth.incrementAndGet()
    }

    fun recordForwardDequeued() {
        decrement(forwardQueueDepth)
    }

    fun recordForwardDropped() {
        forwardQueueDropped.incrementAndGet()
    }

    fun recordForwardDiscardedOnStop(count: Int) {
        if (count <= 0) return
        forwardQueueDiscardedOnStop.addAndGet(count.toLong())
        repeat(count) { decrement(forwardQueueDepth) }
    }

    fun recordForwardDiscardedAfterDequeue() {
        forwardQueueDiscardedOnStop.incrementAndGet()
    }

    fun recordForwarderStopped() {
        val residualDepth = forwardQueueDepth.getAndSet(0)
        if (residualDepth > 0) {
            forwardQueueDiscardedOnStop.addAndGet(residualDepth.toLong())
        }
    }

    fun recordParseSuccess() {
        parserSuccess.incrementAndGet()
    }

    fun recordParseFailure() {
        parserFailure.incrementAndGet()
    }

    fun recordFlowsFlushed(count: Int) {
        flowsFlushed.addAndGet(count.toLong().coerceAtLeast(0L))
    }

    fun updateSegmentQueueDepth(depth: Int) {
        segmentQueueDepth.set(depth.coerceAtLeast(0))
    }

    fun recordWazuhQueued() {
        wazuhQueueDepth.incrementAndGet()
    }

    fun recordWazuhDequeued() {
        decrement(wazuhQueueDepth)
    }

    fun updateNativeQueueStats(depth: Long, highWater: Long) {
        nativeTelemetryQueueDepth.set(depth.coerceAtLeast(0L))
        updateMaximum(nativeTelemetryQueueHighWater, highWater.coerceAtLeast(0L))
    }

    fun recordSegmentQueueDropped() {
        segmentQueueDropped.incrementAndGet()
    }

    fun recordSegmentWriteFailure() {
        segmentWriteFailures.incrementAndGet()
    }

    fun recordWazuhQueueDropped(count: Int = 1) {
        wazuhQueueDropped.addAndGet(count.toLong().coerceAtLeast(0L))
    }

    fun recordPacketRejectedAfterClose() {
        packetsRejectedAfterClose.incrementAndGet()
    }

    fun recordNativeTelemetryDropped(count: Long) {
        nativeTelemetryDropped.addAndGet(count.coerceAtLeast(0L))
    }

    fun updateNativeTelemetryDropped(count: Long) {
        updateMaximum(nativeTelemetryDropped, count.coerceAtLeast(0L))
    }

    fun recordTelemetryReaderFailure() {
        telemetryReaderFailures.incrementAndGet()
    }

    fun snapshot(): PacketPipelineSnapshot {
        return PacketPipelineSnapshot(
            packetsRead = packetsRead.get(),
            bytesRead = bytesRead.get(),
            forwardQueueDepth = forwardQueueDepth.get(),
            segmentQueueDepth = segmentQueueDepth.get(),
            wazuhQueueDepth = wazuhQueueDepth.get(),
            nativeTelemetryQueueDepth = nativeTelemetryQueueDepth.get(),
            nativeTelemetryQueueHighWater = nativeTelemetryQueueHighWater.get(),
            forwardQueueDropped = forwardQueueDropped.get(),
            forwardQueueDiscardedOnStop = forwardQueueDiscardedOnStop.get(),
            parserSuccess = parserSuccess.get(),
            parserFailure = parserFailure.get(),
            flowsFlushed = flowsFlushed.get(),
            segmentQueueDropped = segmentQueueDropped.get(),
            segmentWriteFailures = segmentWriteFailures.get(),
            wazuhQueueDropped = wazuhQueueDropped.get(),
            packetsRejectedAfterClose = packetsRejectedAfterClose.get(),
            nativeTelemetryDropped = nativeTelemetryDropped.get(),
            telemetryReaderFailures = telemetryReaderFailures.get()
        )
    }

    private fun decrement(counter: AtomicInteger) {
        while (true) {
            val current = counter.get()
            if (current <= 0) return
            if (counter.compareAndSet(current, current - 1)) return
        }
    }

    private fun updateMaximum(counter: AtomicLong, value: Long) {
        while (true) {
            val current = counter.get()
            if (value <= current || counter.compareAndSet(current, value)) return
        }
    }
}
