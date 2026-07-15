package com.resistine.android.ui.chat

import android.app.Application
import androidx.lifecycle.*
import com.resistine.android.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the AI Chat interface.
 * 
 * It manages the persistence of chat messages in the local database and
 * coordinates requests to the AI backend (OpenAiClient).
 * 
 * @param application The application context.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatDao = AppDatabase.getDatabase(application).chatDao()
    
    /**
     * LiveData stream of all chat messages from the database, ordered by time.
     */
    val messages: LiveData<List<ChatMessage>> = chatDao.getAllMessages().asLiveData()

    private val _isTyping = MutableLiveData(false)
    /**
     * LiveData boolean indicating whether the AI is currently generating a response.
     */
    val isTyping: LiveData<Boolean> = _isTyping

    val uiState: LiveData<ChatUiState> = MediatorLiveData<ChatUiState>().apply {
        fun publish() {
            value = ChatUiState(
                messages = messages.value.orEmpty(),
                isTyping = isTyping.value == true
            )
        }
        addSource(messages) { publish() }
        addSource(isTyping) { publish() }
    }

    init {
        checkAndAddGreeting()
    }

    /**
     * Checks if the chat history is empty and adds an initial greeting if needed.
     */
    private fun checkAndAddGreeting() {
        viewModelScope.launch(Dispatchers.IO) {
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

    /**
     * Sends a user message, saves it to the database, and fetches an AI response.
     * 
     * @param userText The text entered by the user.
     */
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            // 1. Save user message to DB
            chatDao.insertMessage(ChatMessage(text = userText, isUser = true))
            
            // 2. Show typing indicator
            _isTyping.postValue(true)

            // 3. Call AI backend
            OpenAiClient.getChatResponse(userText) { response ->
                viewModelScope.launch(Dispatchers.IO) {
                    // 4. Save AI response (or error message) to DB
                    chatDao.insertMessage(ChatMessage(text = response, isUser = false))
                    
                    // 5. Hide typing indicator
                    _isTyping.postValue(false)
                }
            }
        }
    }
    
    /**
     * Clears all messages from the chat history database.
     */
    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteAllMessages()
        }
    }
}
