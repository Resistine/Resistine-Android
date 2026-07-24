package com.resistine.android.network.forwarding

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject

object FlowTelemetryDiagnosticsMonitor {
    private const val PREFERENCES = "flow_telemetry_diagnostics"
    private const val LAST_SNAPSHOT = "last_pipeline_snapshot"

    private val mutableSnapshot = MutableLiveData(PacketPipelineSnapshot.empty())
    val snapshot: LiveData<PacketPipelineSnapshot> = mutableSnapshot

    @Synchronized
    fun initialize(context: Context) {
        if (mutableSnapshot.value != PacketPipelineSnapshot.empty()) return
        mutableSnapshot.value = load(context)
    }

    @Synchronized
    fun beginSession(context: Context) {
        publish(context, PacketPipelineSnapshot.empty())
    }

    @Synchronized
    fun publish(context: Context, snapshot: PacketPipelineSnapshot) {
        mutableSnapshot.postValue(snapshot)
        context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(LAST_SNAPSHOT, snapshot.toJson().toString())
            .apply()
    }

    fun load(context: Context): PacketPipelineSnapshot {
        val encoded = context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(LAST_SNAPSHOT, null)
            ?: return PacketPipelineSnapshot.empty()
        return runCatching { JSONObject(encoded).toPipelineSnapshot() }
            .getOrDefault(PacketPipelineSnapshot.empty())
    }

    private fun PacketPipelineSnapshot.toJson() = JSONObject().apply {
        put("packetsRead", packetsRead)
        put("bytesRead", bytesRead)
        put("forwardQueueDepth", forwardQueueDepth)
        put("segmentQueueDepth", segmentQueueDepth)
        put("wazuhQueueDepth", wazuhQueueDepth)
        put("nativeTelemetryQueueDepth", nativeTelemetryQueueDepth)
        put("nativeTelemetryQueueHighWater", nativeTelemetryQueueHighWater)
        put("forwardQueueDropped", forwardQueueDropped)
        put("forwardQueueDiscardedOnStop", forwardQueueDiscardedOnStop)
        put("parserSuccess", parserSuccess)
        put("parserFailure", parserFailure)
        put("flowsFlushed", flowsFlushed)
        put("segmentQueueDropped", segmentQueueDropped)
        put("segmentWriteFailures", segmentWriteFailures)
        put("wazuhQueueDropped", wazuhQueueDropped)
        put("packetsRejectedAfterClose", packetsRejectedAfterClose)
        put("nativeTelemetryDropped", nativeTelemetryDropped)
        put("telemetryReaderFailures", telemetryReaderFailures)
    }

    private fun JSONObject.toPipelineSnapshot() = PacketPipelineSnapshot(
        packetsRead = optLong("packetsRead"),
        bytesRead = optLong("bytesRead"),
        forwardQueueDepth = optInt("forwardQueueDepth"),
        segmentQueueDepth = optInt("segmentQueueDepth"),
        wazuhQueueDepth = optInt("wazuhQueueDepth"),
        nativeTelemetryQueueDepth = optLong("nativeTelemetryQueueDepth"),
        nativeTelemetryQueueHighWater = optLong("nativeTelemetryQueueHighWater"),
        forwardQueueDropped = optLong("forwardQueueDropped"),
        forwardQueueDiscardedOnStop = optLong("forwardQueueDiscardedOnStop"),
        parserSuccess = optLong("parserSuccess"),
        parserFailure = optLong("parserFailure"),
        flowsFlushed = optLong("flowsFlushed"),
        segmentQueueDropped = optLong("segmentQueueDropped"),
        segmentWriteFailures = optLong("segmentWriteFailures"),
        wazuhQueueDropped = optLong("wazuhQueueDropped"),
        packetsRejectedAfterClose = optLong("packetsRejectedAfterClose"),
        nativeTelemetryDropped = optLong("nativeTelemetryDropped"),
        telemetryReaderFailures = optLong("telemetryReaderFailures")
    )
}
