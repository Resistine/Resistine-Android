package com.resistine.android.network.flow

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.resistine.android.database.AppDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlowCaptureAcceptanceInstrumentedTest {
    @Test
    fun reportsPersistedFlowsFromManualWireGuardSession() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val flows = AppDatabase.getDatabase(context).logDao().getPendingFlowLogs().map { entry ->
            JSONObject(entry.message)
        }

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

        Log.i(
            TAG,
            "flows=${flows.size} protocols=$protocols ipVersions=$ipVersions " +
                "packetsOut=$packetsOut packetsIn=$packetsIn bytesOut=$bytesOut bytesIn=$bytesIn"
        )
        flows.take(MAX_SAMPLE_FLOWS).forEachIndexed { index, flow ->
            Log.i(TAG, "sample[$index]=$flow")
        }
    }

    private companion object {
        private const val TAG = "FlowAcceptance"
        private const val MAX_SAMPLE_FLOWS = 10
    }
}
