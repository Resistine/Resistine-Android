package com.resistine.android.security

import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class WazuhCryptoTest {
    @Test
    fun `builds encrypted packet with wazuh agent prefix`() {
        val packet = WazuhCrypto.buildPacket(
            agentId = "007",
            rawSharedKey = "test-agent-key",
            message = "1:WazuhAgent: test",
            globalCount = 123L
        )

        val prefix = "!007!#AES:".toByteArray(Charsets.UTF_8)
        assertTrue(packet.copyOfRange(0, prefix.size).contentEquals(prefix))
        assertTrue(packet.size > "!007!#AES:".length)
    }

    @Test
    fun `rejects invalid identifiers and counters`() {
        assertThrows(IllegalArgumentException::class.java) {
            WazuhCrypto.buildPacket("bad-id", "key", "message", 1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            WazuhCrypto.buildPacket("001", "key", "message", 10_000_000_000L)
        }
    }
}
