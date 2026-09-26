//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[AndroidFlowIngestContextResolver](index.md)

# AndroidFlowIngestContextResolver

class [AndroidFlowIngestContextResolver](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), cacheCapacity: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = DEFAULT_CACHE_CAPACITY, networkCacheMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = NETWORK_CACHE_MILLIS) : [FlowIngestContextResolver](../-flow-ingest-context-resolver/index.md)

Android-specific implementation of [FlowIngestContextResolver](../-flow-ingest-context-resolver/index.md) that resolves active network types and attributes socket connections to application UIDs and package names.

#### Parameters

androidJvm

| | |
|---|---|
| context | Application context. |
| cacheCapacity | Maximum number of cached app attributions. |
| networkCacheMillis | Cache duration for active network type resolution in milliseconds. |

## Constructors

| | |
|---|---|
| [AndroidFlowIngestContextResolver](-android-flow-ingest-context-resolver.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), cacheCapacity: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = DEFAULT_CACHE_CAPACITY, networkCacheMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = NETWORK_CACHE_MILLIS) |

## Functions

| Name | Summary |
|---|---|
| [resolve](resolve.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>open override fun [resolve](resolve.md)(packet: [PacketMetadata](../-packet-metadata/index.md)): [FlowIngestContext](../-flow-ingest-context/index.md)<br>Resolves ingestion context including network type, app UID, and package name for a packet. |