//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowSegmentStore](index.md)/[append](append.md)

# append

[androidJvm]\

@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)

fun [append](append.md)(record: [FlowRecord](../-flow-record/index.md)): [File](https://developer.android.com/reference/kotlin/java/io/File.html)

Appends a flow record to the current active segment file, enforcing retention limits.

#### Return

The [File](https://developer.android.com/reference/kotlin/java/io/File.html) where the record was appended.

#### Parameters

androidJvm

| | |
|---|---|
| record | The [FlowRecord](../-flow-record/index.md) to append. |