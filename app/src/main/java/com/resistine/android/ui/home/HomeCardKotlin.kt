package com.resistine.android.ui.home

data class HomeCardItem(
    val title: String,
    val summary: String,
    val status: String,
    val iconResId: Int,
    val destinationFragmentId: Int
)
