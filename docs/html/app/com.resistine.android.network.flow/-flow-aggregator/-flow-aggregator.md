//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowAggregator](index.md)/[FlowAggregator](-flow-aggregator.md)

# FlowAggregator

[androidJvm]\
constructor(tcpIdleTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), udpIdleTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), hardTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 10 * 60_000L, maxActiveFlows: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))

#### Parameters

androidJvm

| | |
|---|---|
| tcpIdleTimeoutMillis | Idle timeout for TCP flows in milliseconds. |
| udpIdleTimeoutMillis | Idle timeout for UDP flows in milliseconds. |
| hardTimeoutMillis | Maximum hard lifetime for active flows in milliseconds. |
| maxActiveFlows | Maximum number of concurrent active flows before LRU/capacity flushing. |