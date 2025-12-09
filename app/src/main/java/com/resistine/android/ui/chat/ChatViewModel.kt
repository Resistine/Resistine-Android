package com.resistine.android.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<ChatMessage>> = _messages

    init {
        // Initial greeting message from the assistant
        _messages.value?.add(ChatMessage("Hello! I am Resistine, your AI security assistant. I'm actively monitoring your device for any security threats. How can I help you today?", false))
    }

    fun sendMessage(userText: String) {
        // Add user's message and a temporary typing indicator
        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add(ChatMessage(userText, true))
        currentMessages.add(ChatMessage("Typing...", false))
        _messages.value = currentMessages

        // Get the response from the AI
        OpenAiClient.getChatResponse(userText) { response ->
            // Find the typing message and replace it with the actual response
            val updatedMessages = _messages.value ?: mutableListOf()
            val typingMessageIndex = updatedMessages.indexOfLast { it.text == "Typing..." && !it.isUser }
            if (typingMessageIndex != -1) {
                updatedMessages[typingMessageIndex] = ChatMessage(response, false)
                _messages.postValue(updatedMessages) // Use postValue for background thread update
            } else {
                // Fallback in case typing message is not found
                updatedMessages.add(ChatMessage(response, false))
                _messages.postValue(updatedMessages)
            }
        }
    }
}
