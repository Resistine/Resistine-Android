//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowSegmentStore](index.md)/[FlowSegmentStore](-flow-segment-store.md)

# FlowSegmentStore

[androidJvm]\
constructor(directory: [File](https://developer.android.com/reference/kotlin/java/io/File.html), maxSegmentBytes: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 5L * 1024L * 1024L, maxRetainedBytes: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 10L * 1024L * 1024L)

#### Parameters

androidJvm

| | |
|---|---|
| directory | Directory where segment files are stored. |
| maxSegmentBytes | Maximum size of a single segment file in bytes. |
| maxRetainedBytes | Maximum total retained storage size in bytes before older segments are purged. |