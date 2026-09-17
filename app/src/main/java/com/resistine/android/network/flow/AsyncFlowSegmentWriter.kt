package com.resistine.android.network.flow

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Asynchronous writer that buffers and persists flow records to a [FlowSegmentStore] on a background thread.
 *
 * @param store Underlying [FlowSegmentStore] for disk persistence.
 * @param capacity Maximum queue capacity for pending flow records.
 * @param onWriteFailure Callback invoked when a write failure occurs.
 */
class AsyncFlowSegmentWriter(
    private val store: FlowSegmentStore,
    capacity: Int = 2048,
    private val onWriteFailure: (Throwable) -> Unit = {}
) : AutoCloseable {
    private val running = AtomicBoolean(false)
    private val queue = LinkedBlockingQueue<FlowRecord>(capacity)
    private var worker: Thread? = null
    private val terminalFailure = AtomicReference<Throwable?>(null)

    /**
     * Starts the background writer thread.
     */
    fun start() {
        if (!running.compareAndSet(false, true)) return
        worker = Thread({ drainLoop() }, "resistine-flow-segment-writer").apply {
            isDaemon = true
            start()
        }
    }

    /**
     * Offers a flow record to the queue for asynchronous disk writing.
     *
     * @param record The [FlowRecord] to persist.
     * @return True if successfully queued; false if queue is full or stopped.
     */
    fun offer(record: FlowRecord): Boolean {
        if (!running.get() || terminalFailure.get() != null) return false
        return queue.offer(record)
    }

    /**
     * Returns the current number of flow records waiting in the queue.
     *
     * @return Queue depth integer.
     */
    fun queueDepth(): Int = queue.size

    /**
     * Stops the background writer and flushes all remaining queued records.
     */
    override fun close() {
        running.set(false)
        worker?.join(2_000L)
        worker = null
        drainRemaining()
    }

    /**
     * Background loop draining queued flow records and writing them to storage.
     */
    private fun drainLoop() {
        while (running.get() || queue.isNotEmpty()) {
            val record = queue.poll(500L, TimeUnit.MILLISECONDS) ?: continue
            if (!append(record)) break
        }
    }

    /**
     * Drains all remaining records in the queue during shutdown.
     */
    private fun drainRemaining() {
        while (true) {
            val record = queue.poll() ?: break
            if (!append(record)) break
        }
    }

    /**
     * Appends a flow record to the underlying store.
     *
     * @param record [FlowRecord] to append.
     * @return True if successful; false on failure.
     */
    private fun append(record: FlowRecord): Boolean {
        return runCatching {
            store.append(record)
        }.fold(
            onSuccess = { true },
            onFailure = { error ->
                if (terminalFailure.compareAndSet(null, error)) {
                    onWriteFailure(error)
                }
                running.set(false)
                false
            }
        )
    }
}
