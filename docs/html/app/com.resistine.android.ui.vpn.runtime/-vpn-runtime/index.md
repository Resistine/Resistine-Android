//[app](../../../index.md)/[com.resistine.android.ui.vpn.runtime](../index.md)/[VpnRuntime](index.md)

# VpnRuntime

interface [VpnRuntime](index.md)

#### Inheritors

| |
|---|
| [WireGuardVpnRuntime](../-wire-guard-vpn-runtime/index.md) |

## Properties

| Name | Summary |
|---|---|
| [mode](mode.md) | [androidJvm]<br>abstract val [mode](mode.md): [VpnRuntimeMode](../-vpn-runtime-mode/index.md) |

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [androidJvm]<br>abstract fun [close](close.md)() |
| [start](start.md) | [androidJvm]<br>abstract suspend fun [start](start.md)() |
| [status](status.md) | [androidJvm]<br>abstract fun [status](status.md)(): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[VpnRuntimeStatus](../-vpn-runtime-status/index.md)&gt; |
| [stop](stop.md) | [androidJvm]<br>abstract suspend fun [stop](stop.md)() |