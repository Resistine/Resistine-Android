//[app](../../../index.md)/[com.resistine.android.database](../index.md)/[LogEntry](index.md)

# LogEntry

[androidJvm]\
data class [LogEntry](index.md)(val id: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0, val timestamp: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val retryCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0)

Room entity representing a log entry awaiting delivery to the Wazuh manager or local review.

## Constructors

| | |
|---|---|
| [LogEntry](-log-entry.md) | [androidJvm]<br>constructor(id: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0, timestamp: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), retryCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0) |

## Properties

| Name | Summary |
|---|---|
| [id](id.md) | [androidJvm]<br>val [id](id.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Auto-generated primary key identifier. |
| [message](message.md) | [androidJvm]<br>val [message](message.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Formatted JSON or plain text log payload. |
| [retryCount](retry-count.md) | [androidJvm]<br>val [retryCount](retry-count.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Number of upload retry attempts made for this entry. |
| [timestamp](timestamp.md) | [androidJvm]<br>val [timestamp](timestamp.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Epoch timestamp in milliseconds when the log occurred. |