//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowBinaryCodec](index.md)/[writeFileHeader](write-file-header.md)

# writeFileHeader

[androidJvm]\
fun [writeFileHeader](write-file-header.md)(output: [DataOutputStream](https://developer.android.com/reference/kotlin/java/io/DataOutputStream.html), createdAtMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html) = System.currentTimeMillis())

Writes the binary file header to the output stream.

#### Parameters

androidJvm

| | |
|---|---|
| output | [DataOutputStream](https://developer.android.com/reference/kotlin/java/io/DataOutputStream.html) target. |
| createdAtMillis | Creation timestamp in milliseconds. |