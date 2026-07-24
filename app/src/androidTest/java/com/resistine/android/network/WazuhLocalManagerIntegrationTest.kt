package com.resistine.android.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WazuhLocalManagerIntegrationTest {
    @Test
    fun enrollsAndSendsSyntheticFlowToDisposableManager() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val arguments = InstrumentationRegistry.getArguments()
        val managerHost = arguments.getString("wazuh_host")
        assumeTrue(
            "Pass -e wazuh_host with the disposable manager hostname or IP",
            !managerHost.isNullOrBlank()
        )
        val authPort = arguments.getString("wazuh_auth_port")?.toIntOrNull() ?: 1515
        val logPort = arguments.getString("wazuh_log_port")?.toIntOrNull() ?: 1514
        val agentGroup = arguments.getString("wazuh_agent_group") ?: "default"
        val endpoint = WazuhManagerEndpoint(managerHost!!, authPort, logPort)
        val readiness = WazuhRemoteReadinessValidator.validate(endpoint)
        require(readiness.ready) { readiness.issues.joinToString("; ") }
        val suffix = System.currentTimeMillis().toString().takeLast(8)
        val agentName = "resistine-android-test-$suffix"
        val eventId = "resistine-flow-test-$suffix"

        val (agentId, agentKey) = WazuhAuthdManager().registerAndGetKey(
            context = context,
            serverIp = endpoint.host,
            authPort = endpoint.authPort,
            agentName = agentName,
            agentGroup = agentGroup,
            agentIp = "any"
        )
        assertTrue(agentId.isNotBlank())
        assertTrue(agentKey.isNotBlank())
        delay(5_000)

        val logger = WazuhLogger(context)
        try {
            assertTrue(
                logger.connect(
                    serverIp = endpoint.host,
                    agentPort = endpoint.logPort,
                    agentId = agentId,
                    rawAgentKey = agentKey,
                    onStatusUpdate = {}
                )
            )
            assertTrue(
                logger.sendSingleLog(
                    agentId = agentId,
                    rawAgentKey = agentKey,
                    logMessage =
                        """{"event_type":"resistine_flow_test","event_id":"$eventId","synthetic":true,"source":"android_instrumentation"}"""
                )
            )
            delay(2_000)
        } finally {
            logger.disconnect()
        }
    }
}
