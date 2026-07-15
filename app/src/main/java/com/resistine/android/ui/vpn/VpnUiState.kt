package com.resistine.android.ui.vpn

import com.resistine.android.network.WazuhConnectionState
import com.resistine.android.network.flow.FlowWazuhDeliveryMode
import com.resistine.android.ui.vpn.runtime.VpnRuntimeStatus

data class VpnUiState(
    val isConnected: Boolean,
    val statusMessage: String,
    val runtimeStatus: VpnRuntimeStatus,
    val deliveryMode: FlowWazuhDeliveryMode,
    val queuedRecords: Int,
    val wazuhStatus: String,
    val wazuhState: WazuhConnectionState,
    val ipAddress: String,
    val location: String,
    val deviceModel: String,
    val androidVersion: String,
    val batteryLevel: String
) {
    val settingsEnabled: Boolean get() = !runtimeStatus.hasLocalTunnel
}
