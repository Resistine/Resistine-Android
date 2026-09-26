//[app](../../../index.md)/[com.resistine.android.ui.vpn](../index.md)/[VpnUiState](index.md)

# VpnUiState

[androidJvm]\
data class [VpnUiState](index.md)(val isConnected: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val statusMessage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val runtimeStatus: [VpnRuntimeStatus](../../com.resistine.android.ui.vpn.runtime/-vpn-runtime-status/index.md), val deliveryMode: [FlowWazuhDeliveryMode](../../com.resistine.android.network.flow/-flow-wazuh-delivery-mode/index.md), val queuedRecords: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val wazuhStatus: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val wazuhState: [WazuhConnectionState](../../com.resistine.android.network/-wazuh-connection-state/index.md), val wazuhRecordsDelivered: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val wazuhLastError: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?, val telemetryDiagnostics: [PacketPipelineSnapshot](../../com.resistine.android.network.forwarding/-packet-pipeline-snapshot/index.md), val ipAddress: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val location: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val deviceModel: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val androidVersion: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val batteryLevel: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html))

## Constructors

| | |
|---|---|
| [VpnUiState](-vpn-ui-state.md) | [androidJvm]<br>constructor(isConnected: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), statusMessage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), runtimeStatus: [VpnRuntimeStatus](../../com.resistine.android.ui.vpn.runtime/-vpn-runtime-status/index.md), deliveryMode: [FlowWazuhDeliveryMode](../../com.resistine.android.network.flow/-flow-wazuh-delivery-mode/index.md), queuedRecords: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), wazuhStatus: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), wazuhState: [WazuhConnectionState](../../com.resistine.android.network/-wazuh-connection-state/index.md), wazuhRecordsDelivered: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), wazuhLastError: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?, telemetryDiagnostics: [PacketPipelineSnapshot](../../com.resistine.android.network.forwarding/-packet-pipeline-snapshot/index.md), ipAddress: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), location: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), deviceModel: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), androidVersion: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), batteryLevel: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [androidVersion](android-version.md) | [androidJvm]<br>val [androidVersion](android-version.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [batteryLevel](battery-level.md) | [androidJvm]<br>val [batteryLevel](battery-level.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [deliveryMode](delivery-mode.md) | [androidJvm]<br>val [deliveryMode](delivery-mode.md): [FlowWazuhDeliveryMode](../../com.resistine.android.network.flow/-flow-wazuh-delivery-mode/index.md) |
| [deviceModel](device-model.md) | [androidJvm]<br>val [deviceModel](device-model.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [ipAddress](ip-address.md) | [androidJvm]<br>val [ipAddress](ip-address.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [isConnected](is-connected.md) | [androidJvm]<br>val [isConnected](is-connected.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [location](location.md) | [androidJvm]<br>val [location](location.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [queuedRecords](queued-records.md) | [androidJvm]<br>val [queuedRecords](queued-records.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |
| [runtimeStatus](runtime-status.md) | [androidJvm]<br>val [runtimeStatus](runtime-status.md): [VpnRuntimeStatus](../../com.resistine.android.ui.vpn.runtime/-vpn-runtime-status/index.md) |
| [settingsEnabled](settings-enabled.md) | [androidJvm]<br>val [settingsEnabled](settings-enabled.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [statusMessage](status-message.md) | [androidJvm]<br>val [statusMessage](status-message.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [telemetryDiagnostics](telemetry-diagnostics.md) | [androidJvm]<br>val [telemetryDiagnostics](telemetry-diagnostics.md): [PacketPipelineSnapshot](../../com.resistine.android.network.forwarding/-packet-pipeline-snapshot/index.md) |
| [wazuhLastError](wazuh-last-error.md) | [androidJvm]<br>val [wazuhLastError](wazuh-last-error.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? |
| [wazuhRecordsDelivered](wazuh-records-delivered.md) | [androidJvm]<br>val [wazuhRecordsDelivered](wazuh-records-delivered.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) |
| [wazuhState](wazuh-state.md) | [androidJvm]<br>val [wazuhState](wazuh-state.md): [WazuhConnectionState](../../com.resistine.android.network/-wazuh-connection-state/index.md) |
| [wazuhStatus](wazuh-status.md) | [androidJvm]<br>val [wazuhStatus](wazuh-status.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |