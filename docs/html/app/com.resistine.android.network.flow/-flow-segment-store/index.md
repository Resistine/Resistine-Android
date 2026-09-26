//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowSegmentStore](index.md)

# FlowSegmentStore

class [FlowSegmentStore](index.md)(directory: [File](https://developer.android.com/reference/kotlin/java/io/File.html), maxSegmentBytes: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 5L * 1024L * 1024L, maxRetainedBytes: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 10L * 1024L * 1024L)

Storage manager for persisting and retaining flow segment binary files on disk.

#### Parameters

androidJvm

| | |
|---|---|
| directory | Directory where segment files are stored. |
| maxSegmentBytes | Maximum size of a single segment file in bytes. |
| maxRetainedBytes | Maximum total retained storage size in bytes before older segments are purged. |

## Constructors

| | |
|---|---|
| [FlowSegmentStore](-flow-segment-store.md) | [androidJvm]<br>constructor(directory: [File](https://developer.android.com/reference/kotlin/java/io/File.html), maxSegmentBytes: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 5L * 1024L * 1024L, maxRetainedBytes: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 10L * 1024L * 1024L) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [append](append.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [append](append.md)(record: [FlowRecord](../-flow-record/index.md)): [File](https://developer.android.com/reference/kotlin/java/io/File.html)<br>Appends a flow record to the current active segment file, enforcing retention limits. |
| [readAllRecords](read-all-records.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [readAllRecords](read-all-records.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;<br>Reads all flow records across all stored segment files. |
| [readRecords](read-records.md) | [androidJvm]<br>fun [readRecords](read-records.md)(segment: [File](https://developer.android.com/reference/kotlin/java/io/File.html)): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[FlowRecord](../-flow-record/index.md)&gt;<br>Reads all flow records from a specific segment file. |
| [segmentFiles](segment-files.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [segmentFiles](segment-files.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[File](https://developer.android.com/reference/kotlin/java/io/File.html)&gt;<br>Returns a list of all segment files ordered by name. |