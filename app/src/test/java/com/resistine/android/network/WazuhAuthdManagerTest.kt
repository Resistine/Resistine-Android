package com.resistine.android.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class WazuhAuthdManagerTest {
    @Test
    fun `payload omits null agent IP`() {
        val payload = WazuhAuthdManager.buildEnrollmentPayload(
            agentName = "android-test",
            agentGroup = "default",
            enrollmentPassword = "",
            agentIp = null
        )

        assertEquals("OSSEC A:'android-test' G:'default'\n", payload)
    }

    @Test
    fun `payload includes password and agent IP`() {
        val payload = WazuhAuthdManager.buildEnrollmentPayload(
            agentName = "android-test",
            agentGroup = "mobile",
            enrollmentPassword = "secret",
            agentIp = "10.0.0.2"
        )

        assertEquals(
            "OSSEC PASS: secret\nOSSEC A:'android-test' G:'mobile' IP:'10.0.0.2'\n",
            payload
        )
    }

    @Test
    fun `payload rejects line injection`() {
        assertThrows(IllegalArgumentException::class.java) {
            WazuhAuthdManager.buildEnrollmentPayload(
                agentName = "android\nOSSEC PASS: attacker",
                agentGroup = "default",
                enrollmentPassword = "",
                agentIp = null
            )
        }
    }

    @Test
    fun `response parser extracts ID and key`() {
        assertEquals(
            "007" to "base64-agent-key",
            WazuhAuthdManager.parseEnrollmentResponse(
                "OSSEC K:'007 android-test any base64-agent-key'"
            )
        )
    }

    @Test
    fun `response parser rejects malformed response`() {
        assertNull(WazuhAuthdManager.parseEnrollmentResponse("OSSEC K:'missing-fields'"))
        assertNull(WazuhAuthdManager.parseEnrollmentResponse("ERROR duplicate agent"))
    }
}
