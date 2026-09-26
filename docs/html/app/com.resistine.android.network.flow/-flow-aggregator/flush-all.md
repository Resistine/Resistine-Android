//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowAggregator](index.md)/[flushAll](flush-all.md)

# flushAll

[androidJvm]\
fun [flushAll](flush-all.md)(nowMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;

Flushes all active flows into completed [FlowRecord](../-flow-record/index.md) objects.

#### Return

List of flushed [FlowRecord](../-flow-record/index.md) items.

#### Parameters

androidJvm

| | |
|---|---|
| nowMillis | Current timestamp in milliseconds. |