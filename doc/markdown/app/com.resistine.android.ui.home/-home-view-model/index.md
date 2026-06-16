//[app](../../../index.md)/[com.resistine.android.ui.home](../index.md)/[HomeViewModel](index.md)

# HomeViewModel

class [HomeViewModel](index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

ViewModel for the Home screen dashboard.

It generates and updates the list of dashboard cards based on the current system state (VPN connectivity, Wi-Fi status, etc.).

#### Parameters

androidJvm

| | |
|---|---|
| application | The application context. |

## Constructors

| | |
|---|---|
| [HomeViewModel](-home-view-model.md) | [androidJvm]<br>constructor(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Properties

| Name | Summary |
|---|---|
| [cards](cards.md) | [androidJvm]<br>val [cards](cards.md): [LiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/LiveData.html)&lt;[List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[HomeCardItem](../-home-card-item/index.md)&gt;&gt;<br>LiveData holding the current list of [HomeCardItem](../-home-card-item/index.md) to be displayed in the dashboard. |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474) | [androidJvm]<br>open fun [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)closeable: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) | [androidJvm]<br>open fun &lt;[T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474)(): [T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) |
| [updateCards](update-cards.md) | [androidJvm]<br>fun [updateCards](update-cards.md)()<br>Refreshes the dashboard cards by checking the current connectivity state. Updates [cards](cards.md) with the new status and colors. |