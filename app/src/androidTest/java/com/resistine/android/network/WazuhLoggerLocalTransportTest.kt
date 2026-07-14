package com.resistine.android.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WazuhLoggerLocalTransportTest {
    @Test
    fun sendsSyntheticFlowEventToLocalReceiver() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val logger = WazuhLogger(context)
        val fakeAgentId = "999"
        val fakeAgentKey = "local-test-key-not-used-by-any-manager"

        try {
            assertTrue(
                logger.connect(
                    serverIp = "10.0.2.2",
                    agentPort = 1514,
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
