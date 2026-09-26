//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[LogDao](index.md)/[getPendingLogs](get-pending-logs.md)

# getPendingLogs

[androidJvm]\
abstract suspend fun [getPendingLogs](get-pending-logs.md)(): [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[LogEntry](../-log-entry/index.md)&gt;

Retrieves a batch of up to 50 pending logs ordered by timestamp ascending.

#### Return

List of [LogEntry](../-log-entry/index.md) items.