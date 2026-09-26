//[app](../../../index.md)/[com.resistine.android.service](../index.md)/[WazuhService](index.md)/[onStartCommand](on-start-command.md)

# onStartCommand

[androidJvm]\
open override fun [onStartCommand](on-start-command.md)(intent: [Intent](https://developer.android.com/reference/kotlin/android/content/Intent.html)?, flags: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), startId: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)

Called when a command is sent to the service. Manages enrollment and log upload loops.

#### Return

Return value indicating how the system should handle service restarts.

#### Parameters

androidJvm

| | |
|---|---|
| intent | The Intent supplied to startService(). |
| flags | Additional data about this start request. |
| startId | A unique integer representing this specific request to start. |