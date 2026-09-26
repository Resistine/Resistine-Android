//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowBinaryCodec](index.md)

# FlowBinaryCodec

[androidJvm]\
object [FlowBinaryCodec](index.md)

Binary codec for efficiently encoding and decoding flow segments and records.

## Properties

| Name | Summary |
|---|---|
| [FILE_HEADER_BYTES](-f-i-l-e_-h-e-a-d-e-r_-b-y-t-e-s.md) | [androidJvm]<br>const val [FILE_HEADER_BYTES](-f-i-l-e_-h-e-a-d-e-r_-b-y-t-e-s.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 16<br>Number of bytes in a flow segment file header. |
| [FILE_MAGIC](-f-i-l-e_-m-a-g-i-c.md) | [androidJvm]<br>const val [FILE_MAGIC](-f-i-l-e_-m-a-g-i-c.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = 1381189196<br>Magic number identifying Resistine flow segment files ('RSFL'). |
| [FILE_VERSION](-f-i-l-e_-v-e-r-s-i-o-n.md) | [androidJvm]<br>const val [FILE_VERSION](-f-i-l-e_-v-e-r-s-i-o-n.md): [Short](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-short/index.html) = 1<br>File format version. |
| [RECORD_TYPE_FLOW](-r-e-c-o-r-d_-t-y-p-e_-f-l-o-w.md) | [androidJvm]<br>const val [RECORD_TYPE_FLOW](-r-e-c-o-r-d_-t-y-p-e_-f-l-o-w.md): [Byte](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte/index.html) = 1<br>Record type code for flow records. |
| [RECORD_VERSION](-r-e-c-o-r-d_-v-e-r-s-i-o-n.md) | [androidJvm]<br>const val [RECORD_VERSION](-r-e-c-o-r-d_-v-e-r-s-i-o-n.md): [Byte](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte/index.html) = 1<br>Record version. |
| [SCHEMA_VERSION](-s-c-h-e-m-a_-v-e-r-s-i-o-n.md) | [androidJvm]<br>const val [SCHEMA_VERSION](-s-c-h-e-m-a_-v-e-r-s-i-o-n.md): [Short](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-short/index.html) = 1<br>Schema version. |

## Functions

| Name | Summary |
|---|---|
| [decodeRecord](decode-record.md) | [androidJvm]<br>fun [decodeRecord](decode-record.md)(bytes: [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html)): [FlowRecord](../-flow-record/index.md)<br>Decodes a binary payload into a [FlowRecord](../-flow-record/index.md). |
| [encodeRecord](encode-record.md) | [androidJvm]<br>fun [encodeRecord](encode-record.md)(record: [FlowRecord](../-flow-record/index.md)): [ByteArray](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-byte-array/index.html)<br>Encodes a [FlowRecord](../-flow-record/index.md) into a binary byte array. |
| [writeFileHeader](write-file-header.md) | [androidJvm]<br>fun [writeFileHeader](write-file-header.md)(output: [DataOutputStream](https://developer.android.com/reference/kotlin/java/io/DataOutputStream.html), createdAtMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis())<br>Writes the binary file header to the output stream. |