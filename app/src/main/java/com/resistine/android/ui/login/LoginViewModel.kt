package com.resistine.android.ui.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.resistine.android.R
import com.resistine.android.security.CryptoManager
import com.wireguard.config.Config
import com.wireguard.crypto.KeyPair

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    val email = MutableLiveData<String?>()
    val otpSent = MutableLiveData<Boolean>()
    val loginSuccess = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()
    val wireguardConfig = MutableLiveData<String?>()
    val isRegistrationSkipped = MutableLiveData<Boolean>()

    val uiState: LiveData<LoginUiState> = MediatorLiveData<LoginUiState>().apply {
        fun publish() {
            value = LoginUiState(
                email = email.value,
                isLoading = loading.value == true,
                isOtpSent = otpSent.value == true,
                isLoginSuccessful = loginSuccess.value == true,
                errorMessage = errorMessage.value,
                isRegistrationSkipped = isRegistrationSkipped.value == true
            )
        }
        addSource(email) { publish() }
        addSource(loading) { publish() }
        addSource(otpSent) { publish() }
        addSource(loginSuccess) { publish() }
        addSource(errorMessage) { publish() }
        addSource(isRegistrationSkipped) { publish() }
    }

    companion object {
        private const val VPN_NAME = "testVPNapi"
        private const val LOGIN_PREFS = "login_state"
        private const val KEY_WELCOME_FLOW_COMPLETED = "welcome_flow_completed"
        private const val KEY_REGISTRATION_SKIPPED = "registration_skipped"
        private const val TAG = "LoginProvisioning"

        fun hasCompletedWelcomeFlow(context: Context): Boolean {
            return context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_WELCOME_FLOW_COMPLETED, false)
        }

        fun loadSkippedRegistrationState(context: Context): Boolean {
            return context.getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_REGISTRATION_SKIPPED, false)
        }
    }

    init {
        isRegistrationSkipped.value = loadSkippedRegistrationState(getApplication())
    }

    fun skipRegistration() {
        persistWelcomeFlowState(
            completed = true,
            skipped = true
        )
        isRegistrationSkipped.postValue(true)
    }

    fun sendOtp(email: String) {
        loading.postValue(true)
        NetworkClient.sendOtp(email) { isSuccess, error ->
            loading.postValue(false)
            if (isSuccess) {
                otpSent.postValue(true)
            } else {
                errorMessage.postValue(error ?: getApplication<Application>().getString(R.string.failed_to_send_code))
            }
        }
    }

    fun verifyOtp(otp: String) {
        loading.postValue(true)
        val userEmail = email.value
        if (userEmail == null) {
            errorMessage.postValue(getApplication<Application>().getString(R.string.email_not_available))
            loading.postValue(false)
            return
        }

        val keyPair = KeyPair()
        val publicKey = keyPair.publicKey.toBase64()

        NetworkClient.verifyOtp(userEmail, otp, publicKey, VPN_NAME) { isSuccess, error, config ->
            loading.postValue(false)
            if (isSuccess && config != null) {
                try {
                    val formattedConfig = WireGuardProvisioningConfigFormatter.format(
                        config,
                        keyPair.privateKey.toBase64()
                    )
                    Config.parse(formattedConfig.byteInputStream(Charsets.UTF_8))
                    CryptoManager.saveEncryptedConfig(getApplication(), formattedConfig)
                    CryptoManager.saveRegistrationDefaultConfig(getApplication(), formattedConfig)
                    wireguardConfig.postValue(formattedConfig)
                    persistWelcomeFlowState(
                        completed = true,
                        skipped = false
                    )
                    loginSuccess.postValue(true)
                    isRegistrationSkipped.postValue(false)
                } catch (e: Exception) {
                    Log.e(TAG, "Could not process the returned WireGuard configuration", e)
                    errorMessage.postValue(getApplication<Application>().getString(R.string.error_processing_config, e.message))
                }
            } else {
                errorMessage.postValue(error ?: getApplication<Application>().getString(R.string.verification_failed))
            }
        }
    }

    fun consumeOtpSent(): Boolean {
        if (otpSent.value != true) return false
        otpSent.value = false
        return true
    }

    fun consumeLoginSuccess(): Boolean {
        if (loginSuccess.value != true) return false
        loginSuccess.value = false
        return true
    }

    fun consumeError(): String? {
        val currentError = errorMessage.value ?: return null
        errorMessage.value = null
        return currentError
    }

    fun clearEmail() = email.postValue(null)

    private fun persistWelcomeFlowState(
        completed: Boolean,
        skipped: Boolean
    ) {
        getApplication<Application>()
            .getSharedPreferences(LOGIN_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WELCOME_FLOW_COMPLETED, completed)
            .putBoolean(KEY_REGISTRATION_SKIPPED, skipped)
            .apply()
    }

}
