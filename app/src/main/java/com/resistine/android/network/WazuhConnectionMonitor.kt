package com.resistine.android.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Enumeration of possible Wazuh connection and telemetry session states.
 */
enum class WazuhConnectionState {
    LOCAL_ONLY,
    STOPPED,
    MISSING_CREDENTIALS,
    WAITING_FOR_VPN,
    ENROLLING,
    CONNECTING,
    CONNECTED,
    ERROR
}

/**
 * Data class representing the current status of the Wazuh connection.
 *
 * @property state Current [WazuhConnectionState].
 * @property detail Descriptive text explaining the current state or error.
 * @property flowRecordsDelivered Total number of flow telemetry records successfully delivered.
 * @property lastError Most recent error message encountered, if any.
 */
data class WazuhConnectionStatus(
    val state: WazuhConnectionState,
    val detail: String,
    val flowRecordsDelivered: Long = 0L,
    val lastError: String? = null
)

/**
 * Singleton monitor tracking Wazuh connection state and telemetry delivery progress.
 */
object WazuhConnectionMonitor {
    private var currentStatus =
        WazuhConnectionStatus(WazuhConnectionState.STOPPED, "Uploader stopped")
    private val mutableStatus = MutableLiveData(currentStatus)

    /** LiveData stream observing the current Wazuh connection status. */
    val status: LiveData<WazuhConnectionStatus> = mutableStatus

    /**
     * Begins a new telemetry session, resetting delivered flow counts and errors.
     */
    @Synchronized
    fun beginTelemetrySession() {
        currentStatus = currentStatus.copy(flowRecordsDelivered = 0L, lastError = null)
        mutableStatus.postValue(currentStatus)
    }

    /**
     * Updates the connection state and detail message.
     *
     * @param state New [WazuhConnectionState].
     * @param detail Descriptive message.
     */
    @Synchronized
    fun update(state: WazuhConnectionState, detail: String) {
        currentStatus = currentStatus.copy(
            state = state,
            detail = detail,
            lastError = if (state == WazuhConnectionState.ERROR) {
                detail
            } else {
                currentStatus.lastError
            }
        )
        mutableStatus.postValue(currentStatus)
    }

    /**
     * Records that a flow telemetry record has been successfully delivered.
     */
    @Synchronized
    fun recordFlowDelivered() {
        currentStatus = currentStatus.copy(
            flowRecordsDelivered = currentStatus.flowRecordsDelivered + 1L
        )
        mutableStatus.postValue(currentStatus)
    }
}
