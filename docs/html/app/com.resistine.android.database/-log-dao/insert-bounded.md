//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[LogDao](index.md)/[insertBounded](insert-bounded.md)

# insertBounded

[androidJvm]\
open suspend fun [insertBounded](insert-bounded.md)(log: [LogEntry](../-log-entry/index.md), maxRows: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = MAX_PENDING_ROWS): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)

Inserts a log entry while maintaining a bounded maximum row count by dropping oldest entries if exceeded.

#### Return

Number of excess rows deleted.

#### Parameters

androidJvm

| | |
|---|---|
| log | The [LogEntry](../-log-entry/index.md) to insert. |
| maxRows | Maximum allowable rows in the pending logs table. |