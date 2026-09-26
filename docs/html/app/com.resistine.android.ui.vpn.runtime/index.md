//[app](../../index.md)/[com.resistine.android.ui.vpn.runtime](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [VpnRuntime](-vpn-runtime/index.md) | [androidJvm]<br>interface [VpnRuntime](-vpn-runtime/index.md) |
| [VpnRuntimeController](-vpn-runtime-controller/index.md) | [androidJvm]<br>object [VpnRuntimeController](-vpn-runtime-controller/index.md) |
| [VpnRuntimeMode](-vpn-runtime-mode/index.md) | [androidJvm]<br>enum [VpnRuntimeMode](-vpn-runtime-mode/index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[VpnRuntimeMode](-vpn-runtime-mode/index.md)&gt; |
| [VpnRuntimeStatus](-vpn-runtime-status/index.md) | [androidJvm]<br>data class [VpnRuntimeStatus](-vpn-runtime-status/index.md)(val mode: [VpnRuntimeMode](-vpn-runtime-mode/index.md), val isRunning: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val label: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val detail: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val isConnecting: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = false) |
| [WireGuardConfigReader](-wire-guard-config-reader/index.md) | [androidJvm]<br>object [WireGuardConfigReader](-wire-guard-config-reader/index.md) |
| [WireGuardVpnRuntime](-wire-guard-vpn-runtime/index.md) | [androidJvm]<br>class [WireGuardVpnRuntime](-wire-guard-vpn-runtime/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), onStatusMessage: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onTunnelStarted: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onConnectionConfirmed: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onTunnelDown: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)) : [VpnRuntime](-vpn-runtime/index.md) |