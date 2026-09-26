//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[AndroidFlowIngestContextResolver](index.md)/[resolve](resolve.md)

# resolve

[androidJvm]\

@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)

open override fun [resolve](resolve.md)(packet: [PacketMetadata](../-packet-metadata/index.md)): [FlowIngestContext](../-flow-ingest-context/index.md)

Resolves ingestion context including network type, app UID, and package name for a packet.

#### Return

[FlowIngestContext](../-flow-ingest-context/index.md) containing resolved attributes.

#### Parameters

androidJvm

| | |
|---|---|
| packet | Packet metadata. |