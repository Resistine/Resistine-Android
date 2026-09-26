//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowAggregator](index.md)/[ingest](ingest.md)

# ingest

[androidJvm]\
fun [ingest](ingest.md)(packet: [PacketMetadata](../-packet-metadata/index.md), context: [FlowIngestContext](../-flow-ingest-context/index.md) = FlowIngestContext()): [FlowIngestResult](../-flow-ingest-result/index.md)

Ingests a packet into the aggregator, updating or creating active flow accumulators.

#### Return

[FlowIngestResult](../-flow-ingest-result/index.md) detailing flushed records and flow status.

#### Parameters

androidJvm

| | |
|---|---|
| packet | [PacketMetadata](../-packet-metadata/index.md) of the parsed packet. |
| context | [FlowIngestContext](../-flow-ingest-context/index.md) for the packet. |