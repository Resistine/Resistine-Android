//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowAggregator](index.md)/[flushExpired](flush-expired.md)

# flushExpired

[androidJvm]\
fun [flushExpired](flush-expired.md)(nowMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;

Flushes flows that have exceeded their idle or hard timeouts.

#### Return

List of expired [FlowRecord](../-flow-record/index.md) items.

#### Parameters

androidJvm

| | |
|---|---|
| nowMillis | Current timestamp in milliseconds. |