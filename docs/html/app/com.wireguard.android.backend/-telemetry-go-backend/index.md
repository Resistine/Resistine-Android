//[app](../../../index.md)/[com.wireguard.android.backend](../index.md)/[TelemetryGoBackend](index.md)

# TelemetryGoBackend

[androidJvm]\
class [TelemetryGoBackend](index.md) : Backend

## Constructors

| | |
|---|---|
| [TelemetryGoBackend](-telemetry-go-backend.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), telemetrySink: [PacketTelemetrySink](../../com.resistine.android.network.flow/-packet-telemetry-sink/index.md), healthListener: [TelemetryGoBackend.TelemetryHealthListener](-telemetry-health-listener/index.md)) |

## Types

| Name | Summary |
|---|---|
| [AlwaysOnCallback](-always-on-callback/index.md) | [androidJvm]<br>interface [AlwaysOnCallback](-always-on-callback/index.md) |
| [TelemetryHealthListener](-telemetry-health-listener/index.md) | [androidJvm]<br>interface [TelemetryHealthListener](-telemetry-health-listener/index.md) |
| [VpnService](-vpn-service/index.md) | [androidJvm]<br>class [VpnService](-vpn-service/index.md) : [VpnService](https://developer.android.com/reference/kotlin/android/net/VpnService.html) |

## Functions

| Name | Summary |
|---|---|
| [getRunningTunnelNames](get-running-tunnel-names.md) | [androidJvm]<br>open fun [getRunningTunnelNames](get-running-tunnel-names.md)(): [Set](https://developer.android.com/reference/kotlin/java/util/Set.html)&lt;[String](https://developer.android.com/reference/kotlin/java/lang/String.html)&gt; |
| [getState](get-state.md) | [androidJvm]<br>open fun [getState](get-state.md)(tunnel: Tunnel): Tunnel.State |
| [getStatistics](get-statistics.md) | [androidJvm]<br>open fun [getStatistics](get-statistics.md)(tunnel: Tunnel): Statistics |
| [getTelemetryDrops](get-telemetry-drops.md) | [androidJvm]<br>open fun [getTelemetryDrops](get-telemetry-drops.md)(): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) |
| [getTelemetryQueueDepth](get-telemetry-queue-depth.md) | [androidJvm]<br>open fun [getTelemetryQueueDepth](get-telemetry-queue-depth.md)(): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) |
| [getTelemetryQueueHighWater](get-telemetry-queue-high-water.md) | [androidJvm]<br>open fun [getTelemetryQueueHighWater](get-telemetry-queue-high-water.md)(): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) |
| [getVersion](get-version.md) | [androidJvm]<br>open fun [getVersion](get-version.md)(): [String](https://developer.android.com/reference/kotlin/java/lang/String.html) |
| [isAlwaysOn](is-always-on.md) | [androidJvm]<br>open fun [isAlwaysOn](is-always-on.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [isLockdownEnabled](is-lockdown-enabled.md) | [androidJvm]<br>open fun [isLockdownEnabled](is-lockdown-enabled.md)(): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [setAlwaysOnCallback](set-always-on-callback.md) | [androidJvm]<br>open fun [setAlwaysOnCallback](set-always-on-callback.md)(callback: [TelemetryGoBackend.AlwaysOnCallback](-always-on-callback/index.md)) |
| [setState](set-state.md) | [androidJvm]<br>open fun [setState](set-state.md)(tunnel: Tunnel, state: Tunnel.State, @[Nullable](https://developer.android.com/reference/kotlin/androidx/annotation/Nullable.html)config: Config): Tunnel.State |