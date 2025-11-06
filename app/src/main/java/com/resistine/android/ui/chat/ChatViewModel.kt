package com.resistine.android.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<ChatMessage>> = _messages
    init {
        _messages.value?.add(ChatMessage("Hello! I am Resistine, you AI security assistant. I'm Actively monitoring your device for any security threats. How can I help you today?", false))
    }
    fun sendMessage(userText: String) {
        val list = _messages.value ?: mutableListOf()
        // přidáme zprávu uživatele
        list.add(ChatMessage(userText, true))
        // odpověď modelu (zatím natvrdo Hello world!)
        list.add(ChatMessage("Hello world!", false))
        _messages.value = list
    }

}
