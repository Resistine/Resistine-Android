//[app](../../../index.md)/[com.resistine.android.ui.vpn.runtime](../index.md)/[VpnRuntimeController](index.md)

# VpnRuntimeController

[androidJvm]\
object [VpnRuntimeController](index.md)

## Properties

| Name | Summary |
|---|---|
| [ACTION_CONNECT](-a-c-t-i-o-n_-c-o-n-n-e-c-t.md) | [androidJvm]<br>const val [ACTION_CONNECT](-a-c-t-i-o-n_-c-o-n-n-e-c-t.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [ACTION_DISCONNECT](-a-c-t-i-o-n_-d-i-s-c-o-n-n-e-c-t.md) | [androidJvm]<br>const val [ACTION_DISCONNECT](-a-c-t-i-o-n_-d-i-s-c-o-n-n-e-c-t.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html) |
| [message](message.md) | [androidJvm]<br>val [message](message.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [status](status.md) | [androidJvm]<br>val [status](status.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[VpnRuntimeStatus](../-vpn-runtime-status/index.md)&gt; |

## Functions

| Name | Summary |
|---|---|
| [connect](connect.md) | [androidJvm]<br>fun [connect](connect.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |
| [disconnect](disconnect.md) | [androidJvm]<br>fun [disconnect](disconnect.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |
| [onRevoke](on-revoke.md) | [androidJvm]<br>@[JvmStatic](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-jvm-static/index.html)<br>fun [onRevoke](on-revoke.md)(service: [TelemetryGoBackend.VpnService](../../com.wireguard.android.backend/-telemetry-go-backend/-vpn-service/index.md)) |
| [onServiceCreated](on-service-created.md) | [androidJvm]<br>@[JvmStatic](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-jvm-static/index.html)<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [onServiceCreated](on-service-created.md)(createdService: [TelemetryGoBackend.VpnService](../../com.wireguard.android.backend/-telemetry-go-backend/-vpn-service/index.md)) |
| [onServiceDestroyed](on-service-destroyed.md) | [androidJvm]<br>@[JvmStatic](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-jvm-static/index.html)<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [onServiceDestroyed](on-service-destroyed.md)(destroyedService: [TelemetryGoBackend.VpnService](../../com.wireguard.android.backend/-telemetry-go-backend/-vpn-service/index.md)) |
| [onStartCommand](on-start-command.md) | [androidJvm]<br>@[JvmStatic](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-jvm-static/index.html)<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [onStartCommand](on-start-command.md)(startedService: [TelemetryGoBackend.VpnService](../../com.wireguard.android.backend/-telemetry-go-backend/-vpn-service/index.md), intent: [Intent](https://developer.android.com/reference/kotlin/android/content/Intent.html)?): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) |