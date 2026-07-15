package com.resistine.android.ui.settings

data class SettingsUiState(
    val configuration: String = "",
    val hasConfiguration: Boolean = false,
    val canRestoreRegistrationDefault: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
)
