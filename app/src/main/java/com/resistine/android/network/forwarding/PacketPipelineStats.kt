package com.resistine.android.network.forwarding

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Snapshot of packet pipeline statistics and counters.
 *
 * @property packetsRead Total packets read.
 * @property bytesRead Total bytes read.
 * @property forwardQueueDepth Forwarding queue depth.
 * @property segmentQueueDepth Segment queue depth.
 * @property wazuhQueueDepth Wazuh persistence queue depth.
 * @property nativeTelemetryQueueDepth Native telemetry queue depth.
 * @property nativeTelemetryQueueHighWater Native telemetry queue high water mark.
 * @property forwardQueueDropped Packets dropped from forward queue.
 * @property forwardQueueDiscardedOnStop Packets discarded on stop.
 * @property parserSuccess Successful packet parse count.
 * @property parserFailure Failed packet parse count.
 * @property flowsFlushed Total flows flushed.
 * @property segmentQueueDropped Segment queue dropped count.
 * @property segmentWriteFailures Segment write failure count.
 * @property wazuhQueueDropped Wazuh queue dropped count.
 * @property packetsRejectedAfterClose Packets rejected after pipeline close.
 * @property nativeTelemetryDropped Native telemetry dropped packet count.
 * @property telemetryReaderFailures Telemetry reader failure count.
 */
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
        /**
         * Creates an empty [PacketPipelineSnapshot] with all counters set to zero.
         *
         * @return Empty snapshot.
         */
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

/**
 * Thread-safe statistics collector for the packet forwarding and telemetry pipeline.
 */
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

    /**
     * Records a packet read event.
     *
     * @param bytes Number of bytes read.
     */
    fun recordRead(bytes: Int) {
        packetsRead.incrementAndGet()
        bytesRead.addAndGet(bytes.toLong().coerceAtLeast(0L))
    }

    /** Records a packet enqueued for forwarding. */
    fun recordForwardEnqueued() {
        forwardQueueDepth.incrementAndGet()
    }

    /** Records a packet dequeued from forwarding. */
    fun recordForwardDequeued() {
        decrement(forwardQueueDepth)
    }

    /** Records a dropped forwarding packet. */
    fun recordForwardDropped() {
        forwardQueueDropped.incrementAndGet()
    }

    /**
     * Records forwarding packets discarded on stop.
     *
     * @param count Number of discarded packets.
     */
    fun recordForwardDiscardedOnStop(count: Int) {
        if (count <= 0) return
        forwardQueueDiscardedOnStop.addAndGet(count.toLong())
        repeat(count) { decrement(forwardQueueDepth) }
    }

    /** Records a packet discarded after dequeue. */
    fun recordForwardDiscardedAfterDequeue() {
        forwardQueueDiscardedOnStop.incrementAndGet()
    }

    /** Records forwarder stoppage, clearing residual queue depth. */
    fun recordForwarderStopped() {
        val residualDepth = forwardQueueDepth.getAndSet(0)
        if (residualDepth > 0) {
            forwardQueueDiscardedOnStop.addAndGet(residualDepth.toLong())
        }
    }

    /** Records a successful packet parse. */
    fun recordParseSuccess() {
        parserSuccess.incrementAndGet()
    }

    /** Records a failed packet parse. */
    fun recordParseFailure() {
        parserFailure.incrementAndGet()
    }

    /**
     * Records flushed flow records.
     *
     * @param count Number of flows flushed.
     */
    fun recordFlowsFlushed(count: Int) {
        flowsFlushed.addAndGet(count.toLong().coerceAtLeast(0L))
    }

    /**
     * Updates segment queue depth.
     *
     * @param depth Queue depth.
     */
    fun updateSegmentQueueDepth(depth: Int) {
        segmentQueueDepth.set(depth.coerceAtLeast(0))
    }

    /** Records a log enqueued for Wazuh upload. */
    fun recordWazuhQueued() {
        wazuhQueueDepth.incrementAndGet()
    }

    /** Records a log dequeued from Wazuh upload queue. */
    fun recordWazuhDequeued() {
        decrement(wazuhQueueDepth)
    }

    /**
     * Updates native queue stats.
     *
     * @param depth Queue depth.
     * @param highWater High water mark.
     */
    fun updateNativeQueueStats(depth: Long, highWater: Long) {
        nativeTelemetryQueueDepth.set(depth.coerceAtLeast(0L))
        updateMaximum(nativeTelemetryQueueHighWater, highWater.coerceAtLeast(0L))
    }

    /** Records a dropped segment queue item. */
    fun recordSegmentQueueDropped() {
        segmentQueueDropped.incrementAndGet()
    }

    /** Records a segment write failure. */
    fun recordSegmentWriteFailure() {
        segmentWriteFailures.incrementAndGet()
    }

    /**
     * Records dropped Wazuh queue items.
     *
     * @param count Number dropped.
     */
    fun recordWazuhQueueDropped(count: Int = 1) {
        wazuhQueueDropped.addAndGet(count.toLong().coerceAtLeast(0L))
    }

    /** Records a packet rejected after pipeline closure. */
    fun recordPacketRejectedAfterClose() {
        packetsRejectedAfterClose.incrementAndGet()
    }

    /**
     * Records dropped native telemetry packets.
     *
     * @param count Number dropped.
     */
    fun recordNativeTelemetryDropped(count: Long) {
        nativeTelemetryDropped.addAndGet(count.coerceAtLeast(0L))
    }

    /**
     * Updates native telemetry dropped count.
     *
     * @param count Dropped count.
     */
    fun updateNativeTelemetryDropped(count: Long) {
        updateMaximum(nativeTelemetryDropped, count.coerceAtLeast(0L))
    }

    /** Records a telemetry reader failure. */
    fun recordTelemetryReaderFailure() {
        telemetryReaderFailures.incrementAndGet()
    }

    /**
     * Takes a snapshot of current pipeline statistics.
     *
     * @return [PacketPipelineSnapshot].
     */
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

    /**
     * Decrements an atomic integer ensuring it does not drop below zero.
     */
    private fun decrement(counter: AtomicInteger) {
        while (true) {
            val current = counter.get()
            if (current <= 0) return
            if (counter.compareAndSet(current, current - 1)) return
        }
    }

    /**
     * Updates an atomic long with the maximum value.
     */
    private fun updateMaximum(counter: AtomicLong, value: Long) {
        while (true) {
            val current = counter.get()
            if (value <= current || counter.compareAndSet(current, value)) return
        }
    }
}
