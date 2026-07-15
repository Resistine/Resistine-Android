package com.resistine.android.ui.vpn.runtime

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WireGuardHandshakePolicyTest {
    @Test
    fun `zero handshake timestamp is not connected`() {
        assertFalse(
            WireGuardHandshakePolicy.isConfirmed(
                latestHandshakeEpochMillis = 0L,
                attemptStartedEpochMillis = 100_000L
            )
        )
    }

    @Test
    fun `handshake from current attempt confirms connection`() {
        assertTrue(
            WireGuardHandshakePolicy.isConfirmed(
                latestHandshakeEpochMillis = 101_000L,
                attemptStartedEpochMillis = 100_000L
            )
        )
    }

    @Test
    fun `stale handshake does not confirm a new attempt`() {
        assertFalse(
            WireGuardHandshakePolicy.isConfirmed(
                latestHandshakeEpochMillis = 90_000L,
                attemptStartedEpochMillis = 100_000L
            )
        )
    }
}
