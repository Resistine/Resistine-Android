//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowAggregator](index.md)

# FlowAggregator

class [FlowAggregator](index.md)(tcpIdleTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), udpIdleTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), hardTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 10 * 60_000L, maxActiveFlows: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))

Aggregates individual packet metadata into comprehensive network flow records.

#### Parameters

androidJvm

| | |
|---|---|
| tcpIdleTimeoutMillis | Idle timeout for TCP flows in milliseconds. |
| udpIdleTimeoutMillis | Idle timeout for UDP flows in milliseconds. |
| hardTimeoutMillis | Maximum hard lifetime for active flows in milliseconds. |
| maxActiveFlows | Maximum number of concurrent active flows before LRU/capacity flushing. |

## Constructors

| | |
|---|---|
| [FlowAggregator](-flow-aggregator.md) | [androidJvm]<br>constructor(tcpIdleTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), udpIdleTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), hardTimeoutMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 10 * 60_000L, maxActiveFlows: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [activeFlowCount](active-flow-count.md) | [androidJvm]<br>fun [activeFlowCount](active-flow-count.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Returns the number of currently active flows. |
| [flushAll](flush-all.md) | [androidJvm]<br>fun [flushAll](flush-all.md)(nowMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;<br>Flushes all active flows into completed [FlowRecord](../-flow-record/index.md) objects. |
| [flushExpired](flush-expired.md) | [androidJvm]<br>fun [flushExpired](flush-expired.md)(nowMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;<br>Flushes flows that have exceeded their idle or hard timeouts. |
| [ingest](ingest.md) | [androidJvm]<br>fun [ingest](ingest.md)(packet: [PacketMetadata](../-packet-metadata/index.md), context: [FlowIngestContext](../-flow-ingest-context/index.md) = FlowIngestContext()): [FlowIngestResult](../-flow-ingest-result/index.md)<br>Ingests a packet into the aggregator, updating or creating active flow accumulators. |