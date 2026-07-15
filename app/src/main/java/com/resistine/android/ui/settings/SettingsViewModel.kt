package com.resistine.android.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VpnConfigurationRepository(application)
    private val _uiState = MutableLiveData(SettingsUiState())
    val uiState: LiveData<SettingsUiState> = _uiState

    init {
        reload()
    }

    fun saveConfiguration(configuration: String) = runMutation(
        successMessage = "Configuration saved. Reconnect the VPN to apply it."
    ) {
        repository.save(configuration)
        repository.loadActive()
    }

    fun restoreRegistrationDefault() = runMutation(
        successMessage = "Registration configuration restored."
    ) {
        repository.restoreRegistrationDefault()
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value?.copy(message = null)
    }

    private fun reload() {
        val configuration = repository.loadActive()
        _uiState.value = SettingsUiState(
            configuration = configuration,
            hasConfiguration = configuration.isNotBlank(),
            canRestoreRegistrationDefault = repository.hasRegistrationDefault()
        )
    }

    private fun runMutation(
        successMessage: String,
        mutation: () -> String
    ) {
        if (_uiState.value?.isSaving == true) return
        _uiState.value = _uiState.value?.copy(isSaving = true, message = null)
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching(mutation)
            val configuration = result.getOrNull() ?: repository.loadActive()
            _uiState.postValue(
                SettingsUiState(
                    configuration = configuration,
                    hasConfiguration = configuration.isNotBlank(),
                    canRestoreRegistrationDefault = repository.hasRegistrationDefault(),
                    isSaving = false,
                    message = result.fold(
                        onSuccess = { successMessage },
                        onFailure = { it.message ?: "The configuration could not be saved." }
                    )
                )
            )
        }
    }
}
