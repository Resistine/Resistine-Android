package com.resistine.android.network

/**
 * Exponential backoff retry policy for handling connection and enrollment failures.
 *
 * @param initialDelayMillis Initial delay in milliseconds before the first retry.
 * @param maximumDelayMillis Maximum upper bound delay in milliseconds between retries.
 */
internal class WazuhRetryPolicy(
    private val initialDelayMillis: Long = 2_000L,
    private val maximumDelayMillis: Long = 60_000L
) {
    init {
        require(initialDelayMillis > 0L)
        require(maximumDelayMillis >= initialDelayMillis)
    }

    /**
     * Calculates the retry delay in milliseconds for the given number of consecutive failures.
     *
     * @param consecutiveFailures Number of consecutive failures encountered.
     * @return Delay duration in milliseconds.
     */
    fun delayMillis(consecutiveFailures: Int): Long {
        val exponent = consecutiveFailures.coerceAtLeast(1).minus(1).coerceAtMost(30)
        var delay = initialDelayMillis
        repeat(exponent) {
            delay = (delay * 2L).coerceAtMost(maximumDelayMillis)
        }
        return delay
    }
}
