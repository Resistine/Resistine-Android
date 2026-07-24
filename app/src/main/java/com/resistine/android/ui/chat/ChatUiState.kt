package com.resistine.android.ui.chat

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val errorMessage: String? = null
)
