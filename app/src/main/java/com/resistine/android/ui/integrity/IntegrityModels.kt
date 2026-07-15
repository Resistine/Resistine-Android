package com.resistine.android.ui.integrity

enum class IntegrityCheckStatus {
    NOT_RUN,
    CONFIGURATION_REQUIRED,
    RUNNING,
    TRUSTED,
    REVIEW,
    ERROR
}

data class IntegrityUiState(
    val status: IntegrityCheckStatus = IntegrityCheckStatus.NOT_RUN,
    val message: String = "Run a server-verified Play Integrity check for Play Protect, app recognition, and device integrity.",
    val appIntegrity: String? = null,
    val deviceIntegrity: List<String> = emptyList(),
    val playProtectVerdict: String? = null,
    val appAccessRisk: List<String> = emptyList(),
    val licensingVerdict: String? = null,
    val checkedAtMillis: Long? = null,
    val remediationDialogType: Int? = null
) {
    val isRunning: Boolean get() = status == IntegrityCheckStatus.RUNNING
    val canRemediate: Boolean get() = remediationDialogType != null
}

internal data class IntegrityAction(
    val actionId: String,
    val timestampMillis: Long,
    val requestHash: String
)
