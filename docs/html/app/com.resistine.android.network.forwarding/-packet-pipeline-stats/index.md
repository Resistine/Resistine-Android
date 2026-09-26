//[app](../../../index.md)/[com.resistine.android.network.forwarding](../index.md)/[PacketPipelineStats](index.md)

# PacketPipelineStats

[androidJvm]\
class [PacketPipelineStats](index.md)

Thread-safe statistics collector for the packet forwarding and telemetry pipeline.

## Constructors

| | |
|---|---|
| [PacketPipelineStats](-packet-pipeline-stats.md) | [androidJvm]<br>constructor() |

## Functions

| Name | Summary |
|---|---|
| [recordFlowsFlushed](record-flows-flushed.md) | [androidJvm]<br>fun [recordFlowsFlushed](record-flows-flushed.md)(count: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))<br>Records flushed flow records. |
| [recordForwardDequeued](record-forward-dequeued.md) | [androidJvm]<br>fun [recordForwardDequeued](record-forward-dequeued.md)()<br>Records a packet dequeued from forwarding. |
| [recordForwardDiscardedAfterDequeue](record-forward-discarded-after-dequeue.md) | [androidJvm]<br>fun [recordForwardDiscardedAfterDequeue](record-forward-discarded-after-dequeue.md)()<br>Records a packet discarded after dequeue. |
| [recordForwardDiscardedOnStop](record-forward-discarded-on-stop.md) | [androidJvm]<br>fun [recordForwardDiscardedOnStop](record-forward-discarded-on-stop.md)(count: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))<br>Records forwarding packets discarded on stop. |
| [recordForwardDropped](record-forward-dropped.md) | [androidJvm]<br>fun [recordForwardDropped](record-forward-dropped.md)()<br>Records a dropped forwarding packet. |
| [recordForwardEnqueued](record-forward-enqueued.md) | [androidJvm]<br>fun [recordForwardEnqueued](record-forward-enqueued.md)()<br>Records a packet enqueued for forwarding. |
| [recordForwarderStopped](record-forwarder-stopped.md) | [androidJvm]<br>fun [recordForwarderStopped](record-forwarder-stopped.md)()<br>Records forwarder stoppage, clearing residual queue depth. |
| [recordNativeTelemetryDropped](record-native-telemetry-dropped.md) | [androidJvm]<br>fun [recordNativeTelemetryDropped](record-native-telemetry-dropped.md)(count: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html))<br>Records dropped native telemetry packets. |
| [recordPacketRejectedAfterClose](record-packet-rejected-after-close.md) | [androidJvm]<br>fun [recordPacketRejectedAfterClose](record-packet-rejected-after-close.md)()<br>Records a packet rejected after pipeline closure. |
| [recordParseFailure](record-parse-failure.md) | [androidJvm]<br>fun [recordParseFailure](record-parse-failure.md)()<br>Records a failed packet parse. |
| [recordParseSuccess](record-parse-success.md) | [androidJvm]<br>fun [recordParseSuccess](record-parse-success.md)()<br>Records a successful packet parse. |
| [recordRead](record-read.md) | [androidJvm]<br>fun [recordRead](record-read.md)(bytes: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))<br>Records a packet read event. |
| [recordSegmentQueueDropped](record-segment-queue-dropped.md) | [androidJvm]<br>fun [recordSegmentQueueDropped](record-segment-queue-dropped.md)()<br>Records a dropped segment queue item. |
| [recordSegmentWriteFailure](record-segment-write-failure.md) | [androidJvm]<br>fun [recordSegmentWriteFailure](record-segment-write-failure.md)()<br>Records a segment write failure. |
| [recordTelemetryReaderFailure](record-telemetry-reader-failure.md) | [androidJvm]<br>fun [recordTelemetryReaderFailure](record-telemetry-reader-failure.md)()<br>Records a telemetry reader failure. |
| [recordWazuhDequeued](record-wazuh-dequeued.md) | [androidJvm]<br>fun [recordWazuhDequeued](record-wazuh-dequeued.md)()<br>Records a log dequeued from Wazuh upload queue. |
| [recordWazuhQueued](record-wazuh-queued.md) | [androidJvm]<br>fun [recordWazuhQueued](record-wazuh-queued.md)()<br>Records a log enqueued for Wazuh upload. |
| [recordWazuhQueueDropped](record-wazuh-queue-dropped.md) | [androidJvm]<br>fun [recordWazuhQueueDropped](record-wazuh-queue-dropped.md)(count: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 1)<br>Records dropped Wazuh queue items. |
| [snapshot](snapshot.md) | [androidJvm]<br>fun [snapshot](snapshot.md)(): [PacketPipelineSnapshot](../-packet-pipeline-snapshot/index.md)<br>Takes a snapshot of current pipeline statistics. |
| [updateNativeQueueStats](update-native-queue-stats.md) | [androidJvm]<br>fun [updateNativeQueueStats](update-native-queue-stats.md)(depth: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html), highWater: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html))<br>Updates native queue stats. |
| [updateNativeTelemetryDropped](update-native-telemetry-dropped.md) | [androidJvm]<br>fun [updateNativeTelemetryDropped](update-native-telemetry-dropped.md)(count: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html))<br>Updates native telemetry dropped count. |
| [updateSegmentQueueDepth](update-segment-queue-depth.md) | [androidJvm]<br>fun [updateSegmentQueueDepth](update-segment-queue-depth.md)(depth: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html))<br>Updates segment queue depth. |