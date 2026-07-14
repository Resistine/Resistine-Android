package com.resistine.android.network

import org.junit.Assert.assertEquals
import org.junit.Test

class WazuhRetryPolicyTest {
    @Test
    fun `uses bounded exponential backoff`() {
        val policy = WazuhRetryPolicy(initialDelayMillis = 1_000L, maximumDelayMillis = 8_000L)

        assertEquals(1_000L, policy.delayMillis(1))
        assertEquals(2_000L, policy.delayMillis(2))
        assertEquals(4_000L, policy.delayMillis(3))
        assertEquals(8_000L, policy.delayMillis(4))
        assertEquals(8_000L, policy.delayMillis(20))
    }
}
