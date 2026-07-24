package com.resistine.android.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

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

data class WazuhConnectionStatus(
    val state: WazuhConnectionState,
    val detail: String,
    val flowRecordsDelivered: Long = 0L,
    val lastError: String? = null
)

object WazuhConnectionMonitor {
    private var currentStatus =
        WazuhConnectionStatus(WazuhConnectionState.STOPPED, "Uploader stopped")
    private val mutableStatus = MutableLiveData(currentStatus)

    val status: LiveData<WazuhConnectionStatus> = mutableStatus

    @Synchronized
    fun beginTelemetrySession() {
        currentStatus = currentStatus.copy(flowRecordsDelivered = 0L, lastError = null)
        mutableStatus.postValue(currentStatus)
    }

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

    @Synchronized
    fun recordFlowDelivered() {
        currentStatus = currentStatus.copy(
            flowRecordsDelivered = currentStatus.flowRecordsDelivered + 1L
        )
        mutableStatus.postValue(currentStatus)
    }
}
