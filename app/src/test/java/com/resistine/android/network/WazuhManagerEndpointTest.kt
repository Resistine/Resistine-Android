package com.resistine.android.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WazuhManagerEndpointTest {
    @Test
    fun `accepts emulator host and standard wazuh ports`() {
        val endpoint = WazuhManagerEndpoint("10.0.2.2", 1515, 1514)

        assertEquals("10.0.2.2", endpoint.host)
    }

    @Test
    fun `rejects blank host`() {
        assertThrows(IllegalArgumentException::class.java) {
            WazuhManagerEndpoint(" ", 1515, 1514)
        }
    }

    @Test
    fun `rejects invalid ports`() {
        assertThrows(IllegalArgumentException::class.java) {
            WazuhManagerEndpoint("10.0.2.2", 0, 70000)
        }
    }

    @Test
    fun `rejects URLs paths and whitespace`() {
        listOf(
            "https://wazuh.example.com",
            "wazuh.example.com/path",
            "wazuh example.com",
            " wazuh.example.com"
        ).forEach { host ->
            assertThrows(IllegalArgumentException::class.java) {
                WazuhManagerEndpoint(host, 1515, 1514)
            }
        }
    }
}
