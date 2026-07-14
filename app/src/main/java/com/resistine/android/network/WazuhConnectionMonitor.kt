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
    val detail: String
)

object WazuhConnectionMonitor {
    private val mutableStatus = MutableLiveData(
        WazuhConnectionStatus(WazuhConnectionState.STOPPED, "Uploader stopped")
    )

    val status: LiveData<WazuhConnectionStatus> = mutableStatus

    fun update(state: WazuhConnectionState, detail: String) {
        mutableStatus.postValue(WazuhConnectionStatus(state, detail))
    }
}
