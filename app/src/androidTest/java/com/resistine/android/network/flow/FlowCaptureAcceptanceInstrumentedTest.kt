package com.resistine.android.network.flow

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.resistine.android.database.AppDatabase
import com.resistine.android.network.forwarding.FlowTelemetryDiagnosticsMonitor
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlowCaptureAcceptanceInstrumentedTest {
    @Test
    fun reportsPersistedFlowsFromManualWireGuardSession() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(
            "Pass -e runFlowAcceptance true after completing the manual WireGuard traffic session",
            arguments.getString(ARG_RUN_ACCEPTANCE).toBoolean()
        )
        val flows = AppDatabase.getDatabase(context).logDao().getPendingFlowLogs().map { entry ->
            JSONObject(entry.message)
        }
        val diagnostics = FlowTelemetryDiagnosticsMonitor.load(context)

        assertTrue(
            "No resistine_flow records found. Use local-only delivery, generate traffic, wait for idle flush, and disconnect before running this check.",
            flows.isNotEmpty()
        )

        val protocols = flows.mapNotNull { it.optString("protocol").takeIf(String::isNotBlank) }.toSortedSet()
        val ipVersions = flows.mapNotNull { flow ->
            flow.optInt("ip_version").takeIf { it == 4 || it == 6 }
        }.toSortedSet()
        val packetsOut = flows.sumOf { it.optLong("packets_out") }
        val packetsIn = flows.sumOf { it.optLong("packets_in") }
        val bytesOut = flows.sumOf { it.optLong("bytes_out") }
        val bytesIn = flows.sumOf { it.optLong("bytes_in") }

        flows.forEach { flow ->
            assertTrue(flow.optString("flow_id").isNotBlank())
            assertTrue(flow.optString("src_ip").isNotBlank())
            assertTrue(flow.optString("dst_ip").isNotBlank())
            assertTrue(flow.optLong("packets_out") >= 0L)
            assertTrue(flow.optLong("packets_in") >= 0L)
            assertTrue(flow.optLong("bytes_out") >= 0L)
            assertTrue(flow.optLong("bytes_in") >= 0L)
            assertTrue(flow.optBoolean("vpn_active"))
        }
        assertTrue("Expected outbound packets in captured flows", packetsOut > 0L && bytesOut > 0L)
        assertTrue("Expected inbound packets in captured flows", packetsIn > 0L && bytesIn > 0L)
        assertTrue("No packets were observed in the completed telemetry session", diagnostics.packetsRead > 0L)
        assertTrue("No flows were generated in the completed telemetry session", diagnostics.flowsFlushed > 0L)

        val requiredProtocols = arguments.getString(ARG_REQUIRED_PROTOCOLS)
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotBlank)
            ?.toSet()
            ?: setOf("TCP", "UDP")
        assertTrue(
            "Missing required protocols. Required=$requiredProtocols observed=$protocols",
            protocols.containsAll(requiredProtocols)
        )
        val unresolvedNetworkTypes = setOf(
            FlowNetworkType.UNKNOWN.name.lowercase(),
            FlowNetworkType.VPN.name.lowercase()
        )
        assertTrue("No flow contained resolved underlying network metadata", flows.any {
            it.optString("network_type") !in unresolvedNetworkTypes
        })
        if (arguments.getString(ARG_REQUIRE_IPV6).toBoolean()) {
            assertTrue("IPv6 was required but no IPv6 flow was captured", 6 in ipVersions)
        }
        if (arguments.getString(ARG_REQUIRE_APP_ATTRIBUTION).toBoolean()) {
            assertTrue("App attribution was required but every app_uid was null", flows.any {
                !it.isNull("app_uid")
            })
        }

        assertEquals("Native telemetry queue did not drain", 0L, diagnostics.nativeTelemetryQueueDepth)
        assertEquals("Segment queue did not drain", 0, diagnostics.segmentQueueDepth)
        assertEquals("Wazuh persistence queue did not drain", 0, diagnostics.wazuhQueueDepth)
        assertEquals("Forwarding queue did not drain", 0, diagnostics.forwardQueueDepth)
        assertEquals("Native packet copies were dropped", 0L, diagnostics.nativeTelemetryDropped)
        assertEquals("Segment records were dropped", 0L, diagnostics.segmentQueueDropped)
        assertEquals("Segment writes failed", 0L, diagnostics.segmentWriteFailures)
        assertEquals("Wazuh queue records were dropped", 0L, diagnostics.wazuhQueueDropped)
        assertEquals("Telemetry reader failed", 0L, diagnostics.telemetryReaderFailures)
        assertEquals("Packets were rejected after pipeline close", 0L, diagnostics.packetsRejectedAfterClose)

        Log.i(
            TAG,
            "flows=${flows.size} protocols=$protocols ipVersions=$ipVersions " +
                "packetsOut=$packetsOut packetsIn=$packetsIn bytesOut=$bytesOut bytesIn=$bytesIn"
        )
        Log.i(TAG, "diagnostics=$diagnostics")
        flows.take(MAX_SAMPLE_FLOWS).forEachIndexed { index, flow ->
            Log.i(TAG, "sample[$index]=$flow")
        }
    }

    private companion object {
        private const val TAG = "FlowAcceptance"
        private const val MAX_SAMPLE_FLOWS = 10
        private const val ARG_RUN_ACCEPTANCE = "runFlowAcceptance"
        private const val ARG_REQUIRED_PROTOCOLS = "requiredProtocols"
        private const val ARG_REQUIRE_IPV6 = "requireIpv6"
        private const val ARG_REQUIRE_APP_ATTRIBUTION = "requireAppAttribution"
    }
}
