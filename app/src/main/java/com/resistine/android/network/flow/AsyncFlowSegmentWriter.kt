package com.resistine.android.network.flow

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AsyncFlowSegmentWriter(
    private val store: FlowSegmentStore,
    capacity: Int = 2048,
    private val onWriteFailure: (Throwable) -> Unit = {}
) : AutoCloseable {
    private val running = AtomicBoolean(false)
    private val queue = LinkedBlockingQueue<FlowRecord>(capacity)
    private var worker: Thread? = null
    private val terminalFailure = AtomicReference<Throwable?>(null)

    fun start() {
        if (!running.compareAndSet(false, true)) return
        worker = Thread({ drainLoop() }, "resistine-flow-segment-writer").apply {
            isDaemon = true
            start()
        }
    }

    fun offer(record: FlowRecord): Boolean {
        if (!running.get() || terminalFailure.get() != null) return false
        return queue.offer(record)
    }

    override fun close() {
        running.set(false)
        worker?.join(2_000L)
        worker = null
        drainRemaining()
    }

    private fun drainLoop() {
        while (running.get() || queue.isNotEmpty()) {
            val record = queue.poll(500L, TimeUnit.MILLISECONDS) ?: continue
            if (!append(record)) break
        }
    }

    private fun drainRemaining() {
        while (true) {
            val record = queue.poll() ?: break
            if (!append(record)) break
        }
    }

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

