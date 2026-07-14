package com.resistine.android.network.flow

import android.content.Context
import android.content.SharedPreferences

enum class FlowWazuhDeliveryMode {
    LOCAL_QUEUE_ONLY,
    REMOTE_MANAGER
}

class FlowWazuhDeliveryStore(
    private val values: FlowWazuhDeliveryValues
) {
    fun load(): FlowWazuhDeliveryMode {
        val raw = values.getString(KEY_DELIVERY_MODE)
        return FlowWazuhDeliveryMode.entries.firstOrNull { it.name == raw }
            ?: FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY
    }

    fun save(mode: FlowWazuhDeliveryMode) {
        values.putString(KEY_DELIVERY_MODE, mode.name)
    }

    companion object {
        private const val PREFS_NAME = "flow_wazuh_delivery"
        internal const val KEY_DELIVERY_MODE = "delivery_mode"

        fun fromContext(context: Context): FlowWazuhDeliveryStore {
            return FlowWazuhDeliveryStore(
                SharedPreferencesFlowWazuhDeliveryValues(
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                )
            )
        }
    }
}

interface FlowWazuhDeliveryValues {
    fun getString(key: String): String?

    fun putString(key: String, value: String)
}

private class SharedPreferencesFlowWazuhDeliveryValues(
    private val prefs: SharedPreferences
) : FlowWazuhDeliveryValues {
    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
