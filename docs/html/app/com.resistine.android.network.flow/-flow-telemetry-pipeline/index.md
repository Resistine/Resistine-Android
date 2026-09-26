//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowTelemetryPipeline](index.md)

# FlowTelemetryPipeline

class [FlowTelemetryPipeline](index.md)(writer: [AsyncFlowSegmentWriter](../-async-flow-segment-writer/index.md), stats: [PacketPipelineStats](../../com.resistine.android.network.forwarding/-packet-pipeline-stats/index.md), parser: [TunPacketParser](../-tun-packet-parser/index.md) = TunPacketParser(), aggregator: [FlowAggregator](../-flow-aggregator/index.md) = FlowAggregator(), contextResolver: [FlowIngestContextResolver](../-flow-ingest-context-resolver/index.md) = FlowIngestContextResolver { FlowIngestContext() }, onFlowFlushed: ([FlowRecord](../-flow-record/index.md)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {}) : [PacketTelemetrySink](../-packet-telemetry-sink/index.md)

Pipeline coordinating packet ingestion, parsing, flow aggregation, and asynchronous persistence.

#### Parameters

androidJvm

| | |
|---|---|
| writer | [AsyncFlowSegmentWriter](../-async-flow-segment-writer/index.md) for disk persistence. |
| stats | [PacketPipelineStats](../../com.resistine.android.network.forwarding/-packet-pipeline-stats/index.md) tracking pipeline metrics. |
| parser | [TunPacketParser](../-tun-packet-parser/index.md) for parsing IP packets. |
| aggregator | [FlowAggregator](../-flow-aggregator/index.md) for aggregating packets into flows. |
| contextResolver | [FlowIngestContextResolver](../-flow-ingest-context-resolver/index.md) for resolving app attribution and network type. |
| onFlowFlushed | Callback invoked when a flow record is flushed. |

## Constructors

| | |
|---|---|
| [FlowTelemetryPipeline](-flow-telemetry-pipeline.md) | [androidJvm]<br>constructor(writer: [AsyncFlowSegmentWriter](../-async-flow-segment-writer/index.md), stats: [PacketPipelineStats](../../com.resistine.android.network.forwarding/-packet-pipeline-stats/index.md), parser: [TunPacketParser](../-tun-packet-parser/index.md) = TunPacketParser(), aggregator: [FlowAggregator](../-flow-aggregator/index.md) = FlowAggregator(), contextResolver: [FlowIngestContextResolver](../-flow-ingest-context-resolver/index.md) = FlowIngestContextResolver { FlowIngestContext() }, onFlowFlushed: ([FlowRecord](../-flow-record/index.md)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {}) |

## Functions

| Name | Summary |
|---|---|
| [flushExpired](flush-expired.md) | [androidJvm]<br>fun [flushExpired](flush-expired.md)(timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis()): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Flushes expired flows based on idle or hard timeouts. |
| [ingest](ingest.md) | [androidJvm]<br>open override fun [ingest](ingest.md)(packet: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html), length: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), direction: [PacketDirection](../-packet-direction/index.md), timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [PacketTelemetryResult](../-packet-telemetry-result/index.md)<br>Ingests a raw packet into the pipeline. |
| [shutdown](shutdown.md) | [androidJvm]<br>fun [shutdown](shutdown.md)(timestampMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis()): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Shuts down the telemetry pipeline, flushing all active flows and closing the writer. |