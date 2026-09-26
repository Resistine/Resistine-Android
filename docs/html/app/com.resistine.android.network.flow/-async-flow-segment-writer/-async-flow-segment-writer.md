//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[AsyncFlowSegmentWriter](index.md)/[AsyncFlowSegmentWriter](-async-flow-segment-writer.md)

# AsyncFlowSegmentWriter

[androidJvm]\
constructor(store: [FlowSegmentStore](../-flow-segment-store/index.md), capacity: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 2048, onWriteFailure: ([Throwable](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-throwable/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {})

#### Parameters

androidJvm

| | |
|---|---|
| store | Underlying [FlowSegmentStore](../-flow-segment-store/index.md) for disk persistence. |
| capacity | Maximum queue capacity for pending flow records. |
| onWriteFailure | Callback invoked when a write failure occurs. |