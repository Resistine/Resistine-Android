//[app](../../index.md)/[com.resistine.android.ui.home](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [HomeCardAdapter](-home-card-adapter/index.md) | [androidJvm]<br>class [HomeCardAdapter](-home-card-adapter/index.md)(items: [List](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/-list/index.html)&lt;[HomeCardItem](-home-card-item/index.md)&gt;, onItemClick: ([HomeCardItem](-home-card-item/index.md)) -&gt; [Unit](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-unit/index.html)) : [RecyclerView.Adapter](https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.Adapter.html)&lt;[HomeCardAdapter.HomeCardViewHolder](-home-card-adapter/-home-card-view-holder/index.md)&gt; |
| [HomeCardItem](-home-card-item/index.md) | [androidJvm]<br>data class [HomeCardItem](-home-card-item/index.md)(val title: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val summary: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val status: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html), val iconResId: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val destinationFragmentId: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html), val statusColorResId: [Int](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-int/index.html) = com.resistine.android.R.color.success_green) |
| [HomeFragment](-home-fragment/index.md) | [androidJvm]<br>class [HomeFragment](-home-fragment/index.md) : [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html) |
| [HomeViewModel](-home-view-model/index.md) | [androidJvm]<br>class [HomeViewModel](-home-view-model/index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)<br>ViewModel for the Home screen dashboard. |