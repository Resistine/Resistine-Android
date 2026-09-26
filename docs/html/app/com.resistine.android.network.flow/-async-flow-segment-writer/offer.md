//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[AsyncFlowSegmentWriter](index.md)/[offer](offer.md)

# offer

[androidJvm]\
fun [offer](offer.md)(record: [FlowRecord](../-flow-record/index.md)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)

Offers a flow record to the queue for asynchronous disk writing.

#### Return

True if successfully queued; false if queue is full or stopped.

#### Parameters

androidJvm

| | |
|---|---|
| record | The [FlowRecord](../-flow-record/index.md) to persist. |