package com.resistine.android.network.flow

import android.content.Context
import android.content.SharedPreferences

/**
 * Enumeration of delivery modes for flow telemetry events.
 */
enum class FlowWazuhDeliveryMode {
    LOCAL_QUEUE_ONLY,
    REMOTE_MANAGER
}

/**
 * Store managing the configured flow telemetry delivery mode.
 *
 * @property values Underlying [FlowWazuhDeliveryValues] backing storage.
 */
class FlowWazuhDeliveryStore(
    private val values: FlowWazuhDeliveryValues
) {
    /**
     * Loads the current flow Wazuh delivery mode.
     *
     * @return [FlowWazuhDeliveryMode].
     */
    fun load(): FlowWazuhDeliveryMode {
        val raw = values.getString(KEY_DELIVERY_MODE)
        return FlowWazuhDeliveryMode.entries.firstOrNull { it.name == raw }
            ?: FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY
    }

    /**
     * Saves the flow Wazuh delivery mode.
     *
     * @param mode The [FlowWazuhDeliveryMode] to save.
     */
    fun save(mode: FlowWazuhDeliveryMode) {
        values.putString(KEY_DELIVERY_MODE, mode.name)
    }

    companion object {
        private const val PREFS_NAME = "flow_wazuh_delivery"
        internal const val KEY_DELIVERY_MODE = "delivery_mode"

        /**
         * Creates a [FlowWazuhDeliveryStore] from an application [Context].
         *
         * @param context Application context.
         * @return [FlowWazuhDeliveryStore] instance.
         */
        fun fromContext(context: Context): FlowWazuhDeliveryStore {
            return FlowWazuhDeliveryStore(
                SharedPreferencesFlowWazuhDeliveryValues(
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                )
            )
        }
    }
}

/**
 * Interface abstraction for flow delivery mode key-value storage.
 */
interface FlowWazuhDeliveryValues {
    /**
     * Retrieves a string value for the given key.
     *
     * @param key Preference key.
     * @return String value or null.
     */
    fun getString(key: String): String?

    /**
     * Stores a string value for the given key.
     *
     * @param key Preference key.
     * @param value String value to store.
     */
    fun putString(key: String, value: String)
}

/**
 * [SharedPreferences]-backed implementation of [FlowWazuhDeliveryValues].
 *
 * @property prefs [SharedPreferences] instance.
 */
private class SharedPreferencesFlowWazuhDeliveryValues(
    private val prefs: SharedPreferences
) : FlowWazuhDeliveryValues {
    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
