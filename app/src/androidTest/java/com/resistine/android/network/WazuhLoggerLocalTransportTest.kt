package com.resistine.android.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WazuhLoggerLocalTransportTest {
    @Test
    fun sendsSyntheticFlowEventToLocalReceiver() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(
            "Pass -e run_local_transport_test true when a host receiver is listening",
            arguments.getString("run_local_transport_test").toBoolean()
        )
        val logger = WazuhLogger(context)
        val fakeAgentId = "999"
        val fakeAgentKey = "local-test-key-not-used-by-any-manager"

        try {
            assertTrue(
                logger.connect(
                    serverIp = arguments.getString("local_transport_host") ?: "10.0.2.2",
                    agentPort = arguments.getString("local_transport_port")?.toIntOrNull() ?: 1514,
                    agentId = fakeAgentId,
                    rawAgentKey = fakeAgentKey,
                    onStatusUpdate = {}
                )
            )
            assertTrue(
                logger.sendSingleLog(
                    agentId = fakeAgentId,
                    rawAgentKey = fakeAgentKey,
                    logMessage = """{"event_type":"resistine_flow_test","synthetic":true}"""
                )
            )
        } finally {
            logger.disconnect()
        }
    }
}
