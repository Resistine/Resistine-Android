//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowIngestResult](index.md)

# FlowIngestResult

[androidJvm]\
data class [FlowIngestResult](index.md)(val flushed: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;, val createdNewFlow: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), val activeFlowCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))

Result of ingesting a packet into the flow aggregator.

## Constructors

| | |
|---|---|
| [FlowIngestResult](-flow-ingest-result.md) | [androidJvm]<br>constructor(flushed: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;, createdNewFlow: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), activeFlowCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [activeFlowCount](active-flow-count.md) | [androidJvm]<br>val [activeFlowCount](active-flow-count.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Total number of currently active flows. |
| [createdNewFlow](created-new-flow.md) | [androidJvm]<br>val [createdNewFlow](created-new-flow.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>True if a new active flow was created. |
| [flushed](flushed.md) | [androidJvm]<br>val [flushed](flushed.md): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;<br>List of flow records flushed due to capacity or eviction. |