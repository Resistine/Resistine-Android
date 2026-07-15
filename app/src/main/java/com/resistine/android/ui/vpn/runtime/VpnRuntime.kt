package com.resistine.android.ui.vpn.runtime

import androidx.lifecycle.LiveData

interface VpnRuntime {
    val mode: VpnRuntimeMode

    suspend fun start()

    suspend fun stop()

    fun close()

    fun status(): LiveData<VpnRuntimeStatus>
}

enum class VpnRuntimeMode {
    WIREGUARD_TELEMETRY
}

data class VpnRuntimeStatus(
    val mode: VpnRuntimeMode,
    val isRunning: Boolean,
    val label: String,
    val detail: String,
    val isConnecting: Boolean = false
) {
    val hasLocalTunnel: Boolean get() = isRunning || isConnecting
}

