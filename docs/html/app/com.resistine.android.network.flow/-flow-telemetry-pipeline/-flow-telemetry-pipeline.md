//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowTelemetryPipeline](index.md)/[FlowTelemetryPipeline](-flow-telemetry-pipeline.md)

# FlowTelemetryPipeline

[androidJvm]\
constructor(writer: [AsyncFlowSegmentWriter](../-async-flow-segment-writer/index.md), stats: [PacketPipelineStats](../../com.resistine.android.network.forwarding/-packet-pipeline-stats/index.md), parser: [TunPacketParser](../-tun-packet-parser/index.md) = TunPacketParser(), aggregator: [FlowAggregator](../-flow-aggregator/index.md) = FlowAggregator(), contextResolver: [FlowIngestContextResolver](../-flow-ingest-context-resolver/index.md) = FlowIngestContextResolver { FlowIngestContext() }, onFlowFlushed: ([FlowRecord](../-flow-record/index.md)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {})

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