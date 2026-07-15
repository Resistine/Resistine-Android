package com.resistine.android.ui.home

data class HomeUiState(
    val isProtected: Boolean,
    val securityScore: Int,
    val threatCount: Int,
    val lastScanLabel: String,
    val cards: List<HomeCardItem>
)
