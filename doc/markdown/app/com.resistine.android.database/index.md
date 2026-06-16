//[app](../../index.md)/[com.resistine.android.database](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [AppDatabase](-app-database/index.md) | [androidJvm]<br>abstract class [AppDatabase](-app-database/index.md) : [RoomDatabase](https://developer.android.com/reference/kotlin/androidx/room/RoomDatabase.html) |
| [ChatDao](-chat-dao/index.md) | [androidJvm]<br>interface [ChatDao](-chat-dao/index.md) |
| [LogDao](-log-dao/index.md) | [androidJvm]<br>interface [LogDao](-log-dao/index.md) |
| [LogEntry](-log-entry/index.md) | [androidJvm]<br>data class [LogEntry](-log-entry/index.md)(val id: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = 0, val timestamp: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val message: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val retryCount: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 0) |