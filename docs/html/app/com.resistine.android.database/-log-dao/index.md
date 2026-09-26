//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[LogDao](index.md)

# LogDao

[androidJvm]\
interface [LogDao](index.md)

Data Access Object for pending application logs awaiting upload or local review.

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [countAll](count-all.md) | [androidJvm]<br>abstract suspend fun [countAll](count-all.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Counts the total number of pending logs in the database. |
| [countPendingFlowLogs](count-pending-flow-logs.md) | [androidJvm]<br>abstract suspend fun [countPendingFlowLogs](count-pending-flow-logs.md)(): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Counts pending logs associated with flow telemetry. |
| [delete](delete.md) | [androidJvm]<br>abstract suspend fun [delete](delete.md)(log: [LogEntry](../-log-entry/index.md))<br>Deletes a specific log entry from the database. |
| [deleteLogsByIds](delete-logs-by-ids.md) | [androidJvm]<br>abstract suspend fun [deleteLogsByIds](delete-logs-by-ids.md)(ids: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)&gt;)<br>Deletes multiple log entries by their primary key IDs. |
| [deleteOldest](delete-oldest.md) | [androidJvm]<br>abstract suspend fun [deleteOldest](delete-oldest.md)(count: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))<br>Deletes the oldest pending logs up to the specified count. |
| [getPendingFlowLogs](get-pending-flow-logs.md) | [androidJvm]<br>abstract suspend fun [getPendingFlowLogs](get-pending-flow-logs.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[LogEntry](../-log-entry/index.md)&gt;<br>Retrieves all pending flow log entries containing flow telemetry events. |
| [getPendingLogs](get-pending-logs.md) | [androidJvm]<br>abstract suspend fun [getPendingLogs](get-pending-logs.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[LogEntry](../-log-entry/index.md)&gt;<br>Retrieves a batch of up to 50 pending logs ordered by timestamp ascending. |
| [insert](insert.md) | [androidJvm]<br>abstract suspend fun [insert](insert.md)(log: [LogEntry](../-log-entry/index.md))<br>Inserts a single log entry into the pending logs table. |
| [insertBounded](insert-bounded.md) | [androidJvm]<br>open suspend fun [insertBounded](insert-bounded.md)(log: [LogEntry](../-log-entry/index.md), maxRows: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = MAX_PENDING_ROWS): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Inserts a log entry while maintaining a bounded maximum row count by dropping oldest entries if exceeded. |
| [observePendingFlowLogCount](observe-pending-flow-log-count.md) | [androidJvm]<br>abstract fun [observePendingFlowLogCount](observe-pending-flow-log-count.md)(): Flow&lt;[Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)&gt;<br>Observes the count of pending flow logs in real time. |