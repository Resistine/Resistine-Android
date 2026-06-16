//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[LogDao](index.md)

# LogDao

[androidJvm]\
interface [LogDao](index.md)

## Functions

| Name | Summary |
|---|---|
| [delete](delete.md) | [androidJvm]<br>abstract suspend fun [delete](delete.md)(log: [LogEntry](../-log-entry/index.md)) |
| [deleteLogsByIds](delete-logs-by-ids.md) | [androidJvm]<br>abstract suspend fun [deleteLogsByIds](delete-logs-by-ids.md)(ids: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)&gt;) |
| [getPendingLogs](get-pending-logs.md) | [androidJvm]<br>abstract suspend fun [getPendingLogs](get-pending-logs.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[LogEntry](../-log-entry/index.md)&gt; |
| [insert](insert.md) | [androidJvm]<br>abstract suspend fun [insert](insert.md)(log: [LogEntry](../-log-entry/index.md)) |