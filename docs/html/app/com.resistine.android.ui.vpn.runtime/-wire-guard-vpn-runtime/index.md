//[app](../../../index.md)/[com.resistine.android.ui.vpn.runtime](../index.md)/[WireGuardVpnRuntime](index.md)

# WireGuardVpnRuntime

[androidJvm]\
class [WireGuardVpnRuntime](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), onStatusMessage: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onTunnelStarted: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onConnectionConfirmed: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onTunnelDown: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)) : [VpnRuntime](../-vpn-runtime/index.md)

## Constructors

| | |
|---|---|
| [WireGuardVpnRuntime](-wire-guard-vpn-runtime.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), onStatusMessage: ([String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onTunnelStarted: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onConnectionConfirmed: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html), onTunnelDown: () -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [mode](mode.md) | [androidJvm]<br>open override val [mode](mode.md): [VpnRuntimeMode](../-vpn-runtime-mode/index.md) |

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [androidJvm]<br>open override fun [close](close.md)() |
| [start](start.md) | [androidJvm]<br>open suspend override fun [start](start.md)() |
| [status](status.md) | [androidJvm]<br>open override fun [status](status.md)(): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[VpnRuntimeStatus](../-vpn-runtime-status/index.md)&gt; |
| [stop](stop.md) | [androidJvm]<br>open suspend override fun [stop](stop.md)() |