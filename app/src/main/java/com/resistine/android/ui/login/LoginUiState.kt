package com.resistine.android.ui.login

data class LoginUiState(
    val email: String? = null,
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSkipped: Boolean = false
)
