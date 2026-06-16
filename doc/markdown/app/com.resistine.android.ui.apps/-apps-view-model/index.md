//[app](../../../index.md)/[com.resistine.android.ui.apps](../index.md)/[AppsViewModel](index.md)

# AppsViewModel

[androidJvm]\
class [AppsViewModel](index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

## Constructors

| | |
|---|---|
| [AppsViewModel](-apps-view-model.md) | [androidJvm]<br>constructor(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Properties

| Name | Summary |
|---|---|
| [apps](apps.md) | [androidJvm]<br>val [apps](apps.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[AppEntry](../-app-entry/index.md)&gt;&gt; |
| [isScanning](is-scanning.md) | [androidJvm]<br>val [isScanning](is-scanning.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt; |
| [scanSummary](scan-summary.md) | [androidJvm]<br>val [scanSummary](scan-summary.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[ScanSummary](../-scan-summary/index.md)&gt; |
| [showSystemApps](show-system-apps.md) | [androidJvm]<br>val [showSystemApps](show-system-apps.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt; |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474) | [androidJvm]<br>open fun [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)closeable: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) | [androidJvm]<br>open fun &lt;[T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474)(): [T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) |
| [setShowSystemApps](set-show-system-apps.md) | [androidJvm]<br>fun [setShowSystemApps](set-show-system-apps.md)(enabled: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)) |
| [startScan](start-scan.md) | [androidJvm]<br>fun [startScan](start-scan.md)() |