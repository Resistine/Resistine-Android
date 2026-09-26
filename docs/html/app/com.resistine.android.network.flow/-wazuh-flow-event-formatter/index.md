//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[WazuhFlowEventFormatter](index.md)

# WazuhFlowEventFormatter

[androidJvm]\
object [WazuhFlowEventFormatter](index.md)

Formatter for converting [FlowRecord](../-flow-record/index.md) objects into formatted JSON log event strings for Wazuh ingestion.

## Functions

| Name | Summary |
|---|---|
| [format](format.md) | [androidJvm]<br>fun [format](format.md)(record: [FlowRecord](../-flow-record/index.md)): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)<br>Formats a [FlowRecord](../-flow-record/index.md) into a JSON event string. |