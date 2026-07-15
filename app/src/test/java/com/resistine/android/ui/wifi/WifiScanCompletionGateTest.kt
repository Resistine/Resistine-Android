package com.resistine.android.ui.wifi

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiScanCompletionGateTest {

    @Test
    fun onlyFirstCompletionWins() {
        val gate = WifiScanCompletionGate()

        assertTrue(gate.tryComplete())
        repeat(10) {
            assertFalse(gate.tryComplete())
        }
    }
}
