//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowIngestContextResolver](index.md)

# FlowIngestContextResolver

fun interface [FlowIngestContextResolver](index.md)

Functional interface for resolving context metadata for captured network packets.

#### Inheritors

| |
|---|
| [AndroidFlowIngestContextResolver](../-android-flow-ingest-context-resolver/index.md) |

## Functions

| Name | Summary |
|---|---|
| [resolve](resolve.md) | [androidJvm]<br>abstract fun [resolve](resolve.md)(packet: [PacketMetadata](../-packet-metadata/index.md)): [FlowIngestContext](../-flow-ingest-context/index.md)<br>Resolves ingestion context for a given packet metadata. |