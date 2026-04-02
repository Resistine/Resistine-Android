package com.resistine.android.ui.agent

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.resistine.android.R
import com.resistine.android.network.WazuhAuthdManager
import com.resistine.android.network.WazuhLogger
import com.resistine.android.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AgentViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData("")
    val text: LiveData<String> = _text

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRegistered = MutableLiveData(false)
    val isRegistered: LiveData<Boolean> = _isRegistered

    val userEmail = MutableLiveData<String?>()

    private val serverIp = "10.0.0.28" // Ujisti se, že tu máš správnou IP Managera

    private val authdManager = WazuhAuthdManager(serverIp, 1515)
    private val logger = WazuhLogger(application, serverIp, 1514)

    private val prefs = application.getSharedPreferences("wazuh_prefs", Context.MODE_PRIVATE)
    private var currentAgentName: String = ""

    init {
        _text.value = application.getString(R.string.agent_status_ready)
        userEmail.value = CryptoManager.loadDecryptedEmail(application)
        val savedId = prefs.getString("agent_id", null)
        val savedName = prefs.getString("agent_name", null)

        if (savedId != null && savedName != null) {
            currentAgentName = savedName
            _isRegistered.value = true
            _text.value = application.getString(R.string.agent_registered_id, savedId)
        }
    }

    private fun generateAgentName(email: String): String {
        val sanitizedModel = Build.MODEL.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val sanitizedEmail = email.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val randomSuffix = (1000..9999).random()
        return "$sanitizedModel-$sanitizedEmail-$randomSuffix"
    }

    fun registerAgent(email: String) {
        currentAgentName = generateAgentName(email)
        _isLoading.value = true
        _text.value = getApplication<Application>().getString(R.string.agent_registering, currentAgentName)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (agentId, key) = authdManager.registerAndGetKey(getApplication(), currentAgentName)

                prefs.edit().apply {
                    putString("agent_id", agentId)
                    putString("agent_key", key)
                    putString("agent_name", currentAgentName)
                    apply()
                }

                _isRegistered.postValue(true)
                _text.postValue(getApplication<Application>().getString(R.string.agent_registration_success, agentId))
            } catch (e: Exception) {
                _text.postValue(getApplication<Application>().getString(R.string.agent_registration_failed, e.message ?: ""))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun connectAgent() {
        _isLoading.value = true
        _text.value = getApplication<Application>().getString(R.string.agent_initiating_connection)

        viewModelScope.launch {
            logger.connectAndStartKeepalive(
                prefs.getString("agent_id", "")!!,
                prefs.getString("agent_key", "")!!,
                currentAgentName,
                onStatusUpdate = { status ->
                    _text.postValue(status)
                },
                onConnected = {
                    _isLoading.postValue(false) // Zastaví načítací kolečko
                }
            )
        }
    }

    fun sendManualLog() {
        viewModelScope.launch {
//            val logText = "7:sshd[1234]: Failed password for invalid user admin from 10.0.0.50 port 5678 (Manuální test)\n"
            val logText = "1:secure:Mar 26 19:00:00 localhost sshd[1234]: Failed password for invalid user admin from 10.0.0.50 port 5678 ssh2\n"
            logger.sendSingleLog(
                prefs.getString("agent_id", "")!!,
                prefs.getString("agent_key", "")!!,
                logText
            )
            _text.postValue(getApplication<Application>().getString(R.string.agent_log_sent))
        }
    }
}