package com.resistine.android.ui.apps

data class AppsUiState(
    val apps: List<AppEntry> = emptyList(),
    val summary: ScanSummary = ScanSummary(
        total = 0,
        noConcern = 0,
        review = 0,
        urgentReview = 0,
        lastScanAt = null,
        isScanned = false
    ),
    val isScanning: Boolean = false,
    val scanProgress: Int? = null,
    val showSystemApps: Boolean = false
)
