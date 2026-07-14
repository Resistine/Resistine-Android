package com.resistine.android.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WazuhRemoteReadinessTest {
    private val endpoint = WazuhManagerEndpoint("wazuh.example.com", 1515, 1514)

    @Test
    fun `accepts valid endpoint with system trust`() {
        assertTrue(WazuhRemoteReadinessValidator.validate(endpoint, null).ready)
    }

    @Test
    fun `rejects malformed custom CA`() {
        val result = WazuhRemoteReadinessValidator.validate(
            endpoint,
            "-----BEGIN CERTIFICATE-----\ninvalid\n-----END CERTIFICATE-----"
        )

        assertFalse(result.ready)
        assertTrue(result.issues.any { it.contains("certificate") })
    }
}
