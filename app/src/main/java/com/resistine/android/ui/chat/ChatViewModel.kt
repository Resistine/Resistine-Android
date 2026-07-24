package com.resistine.android.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.resistine.android.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val chatDao = AppDatabase.getDatabase(application).chatDao()
    private val messages: LiveData<List<ChatMessage>> = chatDao.getAllMessages().asLiveData()
    private val isTyping = MutableLiveData(false)
    private val errorMessage = MutableLiveData<String?>(null)
    private var activeCall: Call? = null

    val uiState: LiveData<ChatUiState> = MediatorLiveData<ChatUiState>().apply {
        fun publish() {
            value = ChatUiState(
                messages = messages.value.orEmpty(),
                isTyping = isTyping.value == true,
                errorMessage = errorMessage.value
            )
        }
        addSource(messages) { publish() }
        addSource(isTyping) { publish() }
        addSource(errorMessage) { publish() }
    }

    init {
        ensureGreeting()
    }

    fun sendMessage(userText: String) {
        val normalized = userText.trim()
        if (normalized.isEmpty() || isTyping.value == true) return
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.insertMessage(ChatMessage(text = normalized, isUser = true))
            requestResponse()
        }
    }

    fun retryLastResponse() {
        if (isTyping.value == true) return
        viewModelScope.launch(Dispatchers.IO) {
            if (chatDao.getAllMessagesSync().lastOrNull()?.isUser == true) {
                requestResponse()
            }
        }
    }

    fun cancelResponse() {
        activeCall?.cancel()
        activeCall = null
        isTyping.postValue(false)
    }

    fun clearHistory() {
        cancelResponse()
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteAllMessages()
            insertGreeting()
        }
    }

    fun consumeError() {
        errorMessage.value = null
    }

    private suspend fun requestResponse() {
        val history = chatDao.getAllMessagesSync()
        if (history.lastOrNull()?.isUser != true) return
        errorMessage.postValue(null)
        isTyping.postValue(true)
        activeCall = OpenAiClient.getChatResponse(history) { result ->
            viewModelScope.launch(Dispatchers.IO) {
                result.onSuccess { response ->
                    chatDao.insertMessage(ChatMessage(text = response, isUser = false))
                }.onFailure { error ->
                    errorMessage.postValue(
                        error.message ?: "The security assistant is unavailable. Try again."
                    )
                }
                activeCall = null
                isTyping.postValue(false)
            }
        }
    }

    private fun ensureGreeting() {
        viewModelScope.launch(Dispatchers.IO) {
            if (chatDao.getMessagesSync().isEmpty()) insertGreeting()
        }
    }

    private suspend fun insertGreeting() {
        chatDao.insertMessage(
            ChatMessage(
                text = "Hello. I am **Resistine**, your security assistant. Ask me about your " +
                    "VPN, Wi-Fi risk, installed apps, or what to review first.",
                isUser = false
            )
        )
    }

    override fun onCleared() {
        activeCall?.cancel()
        super.onCleared()
    }
}
