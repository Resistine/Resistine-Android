package com.resistine.android.network

internal class WazuhRetryPolicy(
    private val initialDelayMillis: Long = 2_000L,
    private val maximumDelayMillis: Long = 60_000L
) {
    init {
        require(initialDelayMillis > 0L)
        require(maximumDelayMillis >= initialDelayMillis)
    }

    fun delayMillis(consecutiveFailures: Int): Long {
        val exponent = consecutiveFailures.coerceAtLeast(1).minus(1).coerceAtMost(30)
        var delay = initialDelayMillis
        repeat(exponent) {
            delay = (delay * 2L).coerceAtMost(maximumDelayMillis)
        }
        return delay
    }
}
