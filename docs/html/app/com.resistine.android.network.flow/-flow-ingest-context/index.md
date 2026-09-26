//[app](../../../index.md)/[com.resistine.android.network.flow](../index.md)/[FlowIngestContext](index.md)

# FlowIngestContext

[androidJvm]\
data class [FlowIngestContext](index.md)(val networkType: [FlowNetworkType](../-flow-network-type/index.md) = FlowNetworkType.UNKNOWN, val appUid: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, val appPackage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, val vpnActive: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = true)

Context provided when ingesting packets into the flow aggregator.

## Constructors

| | |
|---|---|
| [FlowIngestContext](-flow-ingest-context.md) | [androidJvm]<br>constructor(networkType: [FlowNetworkType](../-flow-network-type/index.md) = FlowNetworkType.UNKNOWN, appUid: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)? = null, appPackage: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)? = null, vpnActive: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) = true) |

## Properties

| Name | Summary |
|---|---|
| [appPackage](app-package.md) | [androidJvm]<br>val [appPackage](app-package.md): [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?<br>Application package name. |
| [appUid](app-uid.md) | [androidJvm]<br>val [appUid](app-uid.md): [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html)?<br>Application UID associated with the flow. |
| [networkType](network-type.md) | [androidJvm]<br>val [networkType](network-type.md): [FlowNetworkType](../-flow-network-type/index.md)<br>Active [FlowNetworkType](../-flow-network-type/index.md). |
| [vpnActive](vpn-active.md) | [androidJvm]<br>val [vpnActive](vpn-active.md): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)<br>Whether VPN is active. |