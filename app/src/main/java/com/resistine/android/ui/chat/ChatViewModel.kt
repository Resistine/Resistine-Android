package com.resistine.android.ui.chat

import android.app.Application
import androidx.lifecycle.*
import com.resistine.android.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatDao = AppDatabase.getDatabase(application).chatDao()
    
    val messages: LiveData<List<ChatMessage>> = chatDao.getAllMessages().asLiveData()

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    init {
        checkAndAddGreeting()
    }

    private fun checkAndAddGreeting() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if there are any messages in DB
            val currentMessages = chatDao.getMessagesSync()
            if (currentMessages.isEmpty()) {
                val greeting = ChatMessage(
                    text = "Hello! I am Resistine, your AI security assistant. I'm actively monitoring your device for any security threats. How can I help you today?",
                    isUser = false
                )
                chatDao.insertMessage(greeting)
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            // 1. Uložit zprávu uživatele
            chatDao.insertMessage(ChatMessage(text = userText, isUser = true))
            
            // 2. Indikace psaní
            _isTyping.postValue(true)

            // 3. Volání AI
            OpenAiClient.getChatResponse(userText) { response ->
                viewModelScope.launch(Dispatchers.IO) {
                    // 4. Uložit odpověď (i kdyby to byla chyba, ať uživatel vidí zpětnou vazbu)
                    chatDao.insertMessage(ChatMessage(text = response, isUser = false))
                    _isTyping.postValue(false)
                }
            }
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteAllMessages()
        }
    }
}
