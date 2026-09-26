//[app](../../../index.md)/[com.resistine.android.network.forwarding](../index.md)/[PacketPipelineSnapshot](index.md)

# PacketPipelineSnapshot

[androidJvm]\
data class [PacketPipelineSnapshot](index.md)(val packetsRead: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val bytesRead: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val forwardQueueDepth: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val segmentQueueDepth: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val wazuhQueueDepth: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val nativeTelemetryQueueDepth: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val nativeTelemetryQueueHighWater: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val forwardQueueDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val forwardQueueDiscardedOnStop: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val parserSuccess: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val parserFailure: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val flowsFlushed: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val segmentQueueDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val segmentWriteFailures: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val wazuhQueueDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val packetsRejectedAfterClose: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val nativeTelemetryDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), val telemetryReaderFailures: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html))

Snapshot of packet pipeline statistics and counters.

## Constructors

| | |
|---|---|
| [PacketPipelineSnapshot](-packet-pipeline-snapshot.md) | [androidJvm]<br>constructor(packetsRead: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), bytesRead: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), forwardQueueDepth: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), segmentQueueDepth: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), wazuhQueueDepth: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), nativeTelemetryQueueDepth: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), nativeTelemetryQueueHighWater: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), forwardQueueDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), forwardQueueDiscardedOnStop: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), parserSuccess: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), parserFailure: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), flowsFlushed: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), segmentQueueDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), segmentWriteFailures: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), wazuhQueueDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), packetsRejectedAfterClose: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), nativeTelemetryDropped: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), telemetryReaderFailures: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Properties

| Name | Summary |
|---|---|
| [bytesRead](bytes-read.md) | [androidJvm]<br>val [bytesRead](bytes-read.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Total bytes read. |
| [flowsFlushed](flows-flushed.md) | [androidJvm]<br>val [flowsFlushed](flows-flushed.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Total flows flushed. |
| [forwardQueueDepth](forward-queue-depth.md) | [androidJvm]<br>val [forwardQueueDepth](forward-queue-depth.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Forwarding queue depth. |
| [forwardQueueDiscardedOnStop](forward-queue-discarded-on-stop.md) | [androidJvm]<br>val [forwardQueueDiscardedOnStop](forward-queue-discarded-on-stop.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Packets discarded on stop. |
| [forwardQueueDropped](forward-queue-dropped.md) | [androidJvm]<br>val [forwardQueueDropped](forward-queue-dropped.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Packets dropped from forward queue. |
| [nativeTelemetryDropped](native-telemetry-dropped.md) | [androidJvm]<br>val [nativeTelemetryDropped](native-telemetry-dropped.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Native telemetry dropped packet count. |
| [nativeTelemetryQueueDepth](native-telemetry-queue-depth.md) | [androidJvm]<br>val [nativeTelemetryQueueDepth](native-telemetry-queue-depth.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Native telemetry queue depth. |
| [nativeTelemetryQueueHighWater](native-telemetry-queue-high-water.md) | [androidJvm]<br>val [nativeTelemetryQueueHighWater](native-telemetry-queue-high-water.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Native telemetry queue high water mark. |
| [packetsRead](packets-read.md) | [androidJvm]<br>val [packetsRead](packets-read.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Total packets read. |
| [packetsRejectedAfterClose](packets-rejected-after-close.md) | [androidJvm]<br>val [packetsRejectedAfterClose](packets-rejected-after-close.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Packets rejected after pipeline close. |
| [parserFailure](parser-failure.md) | [androidJvm]<br>val [parserFailure](parser-failure.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Failed packet parse count. |
| [parserSuccess](parser-success.md) | [androidJvm]<br>val [parserSuccess](parser-success.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Successful packet parse count. |
| [segmentQueueDepth](segment-queue-depth.md) | [androidJvm]<br>val [segmentQueueDepth](segment-queue-depth.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Segment queue depth. |
| [segmentQueueDropped](segment-queue-dropped.md) | [androidJvm]<br>val [segmentQueueDropped](segment-queue-dropped.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Segment queue dropped count. |
| [segmentWriteFailures](segment-write-failures.md) | [androidJvm]<br>val [segmentWriteFailures](segment-write-failures.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Segment write failure count. |
| [telemetryReaderFailures](telemetry-reader-failures.md) | [androidJvm]<br>val [telemetryReaderFailures](telemetry-reader-failures.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Telemetry reader failure count. |
| [wazuhQueueDepth](wazuh-queue-depth.md) | [androidJvm]<br>val [wazuhQueueDepth](wazuh-queue-depth.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)<br>Wazuh persistence queue depth. |
| [wazuhQueueDropped](wazuh-queue-dropped.md) | [androidJvm]<br>val [wazuhQueueDropped](wazuh-queue-dropped.md): [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)<br>Wazuh queue dropped count. |