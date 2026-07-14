package com.resistine.android.network

import org.junit.Assert.assertTrue
import org.junit.Test

class WazuhRemoteReadinessTest {
    private val endpoint = WazuhManagerEndpoint("wazuh.example.com", 1515, 1514)

    @Test
    fun `accepts valid endpoint with system trust`() {
        assertTrue(WazuhRemoteReadinessValidator.validate(endpoint).ready)
    }
}
