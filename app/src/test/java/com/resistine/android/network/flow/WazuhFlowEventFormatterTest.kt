package com.resistine.android.network.flow

import org.junit.Assert.assertTrue
import org.junit.Test

class WazuhFlowEventFormatterTest {
    @Test
    fun `formats flow record as wazuh friendly json event`() {
        val event = WazuhFlowEventFormatter.format(
            flowRecord(
                appPackage = "com.example.browser",
                evidence = ProtocolEvidence(httpHost = "example.com", httpMethod = "GET")
            )
        )

        assertTrue(event.contains("\"event_type\":\"resistine_flow\""))
        assertTrue(event.contains("\"app_package\":\"com.example.browser\""))
        assertTrue(event.contains("\"http_host\":\"example.com\""))
        assertTrue(event.contains("\"http_method\":\"GET\""))
        assertTrue(event.contains("\"bytes_out\":512"))
    }

    @Test
    fun `omits transport ports for ICMP event`() {
        val event = WazuhFlowEventFormatter.format(
            flowRecord().copy(
                protocol = FlowProtocol.ICMP,
                srcPort = 0,
                dstPort = 0
            )
        )

        assertTrue(event.contains("\"protocol\":\"ICMP\""))
        assertTrue(event.contains("\"src_port\":null"))
        assertTrue(event.contains("\"dst_port\":null"))
    }
}
