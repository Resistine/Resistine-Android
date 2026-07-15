package com.resistine.android.ui.wifi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Coalesces connectivity, passive-scan, and manual refresh signals into one serialized pipeline.
 * A full refresh always wins over queued lightweight refreshes.
 */
internal class WifiRefreshCoordinator(
    private val scope: CoroutineScope,
    private val onRefreshingChanged: (Boolean) -> Unit,
    private val performRefresh: suspend (lightweight: Boolean) -> Unit
) {
    private val lock = Any()
    private var job: Job? = null
    private var pending = false
    private var fullRefreshPending = false

    fun request(lightweight: Boolean) {
        synchronized(lock) {
            pending = true
            if (!lightweight) fullRefreshPending = true
            if (job?.isActive == true) return
            job = scope.launch(Dispatchers.Default) {
                onRefreshingChanged(true)
                try {
                    while (true) {
                        val nextLightweight = synchronized(lock) {
                            pending = false
                            val runFullRefresh = fullRefreshPending
                            fullRefreshPending = false
                            !runFullRefresh
                        }
                        performRefresh(nextLightweight)
                        if (!synchronized(lock) { pending }) break
                    }
                } finally {
                    onRefreshingChanged(false)
                    val restart = synchronized(lock) {
                        job = null
                        pending
                    }
                    if (restart) request(lightweight = true)
                }
            }
        }
    }

    fun cancel() {
        synchronized(lock) {
            pending = false
            fullRefreshPending = false
            job?.cancel()
            job = null
        }
    }
}
