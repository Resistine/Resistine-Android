//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[AsyncFlowSegmentWriter](index.md)

# AsyncFlowSegmentWriter

class [AsyncFlowSegmentWriter](index.md)(store: [FlowSegmentStore](../-flow-segment-store/index.md), capacity: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 2048, onWriteFailure: ([Throwable](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-throwable/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {}) : [AutoCloseable](https://developer.android.com/reference/kotlin/java/lang/AutoCloseable.html)

Asynchronous writer that buffers and persists flow records to a [FlowSegmentStore](../-flow-segment-store/index.md) on a background thread.

#### Parameters

androidJvm

| | |
|---|---|
| store | Underlying [FlowSegmentStore](../-flow-segment-store/index.md) for disk persistence. |
| capacity | Maximum queue capacity for pending flow records. |
| onWriteFailure | Callback invoked when a write failure occurs. |

## Constructors

| | |
|---|---|
| [AsyncFlowSegmentWriter](-async-flow-segment-writer.md) | [androidJvm]<br>constructor(store: [FlowSegmentStore](../-flow-segment-store/index.md), capacity: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 2048, onWriteFailure: ([Throwable](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-throwable/index.html)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html) = {}) |

## Functions

| Name | Summary |
|---|---|
| [close](close.md) | [androidJvm]<br>open override fun [close](close.md)()<br>Stops the background writer and flushes all remaining queued records. |
| [offer](offer.md) | [androidJvm]<br>fun [offer](offer.md)(record: [FlowRecord](../-flow-record/index.md)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Offers a flow record to the queue for asynchronous disk writing. |
| [queueDepth](queue-depth.md) | [androidJvm]<br>fun [queueDepth](queue-depth.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Returns the current number of flow records waiting in the queue. |
| [start](start.md) | [androidJvm]<br>fun [start](start.md)()<br>Starts the background writer thread. |