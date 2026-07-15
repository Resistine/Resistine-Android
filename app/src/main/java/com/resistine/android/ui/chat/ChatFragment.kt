package com.resistine.android.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.resistine.android.R
import com.resistine.android.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatAdapter()
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = false
        }
        binding.recyclerViewChat.adapter = adapter

        binding.buttonSend.setOnClickListener {
            sendCurrentMessage()
        }
        binding.promptVpn.setOnClickListener { sendPrompt(getString(R.string.chat_prompt_vpn)) }
        binding.promptWifi.setOnClickListener { sendPrompt(getString(R.string.chat_prompt_wifi)) }
        binding.promptPriority.setOnClickListener { sendPrompt(getString(R.string.chat_prompt_priority)) }
        binding.editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.submitState(state)
            binding.buttonSend.isEnabled = !state.isTyping
            binding.quickPrompts.visibility =
                if (state.messages.size <= 1 && !state.isTyping) View.VISIBLE else View.GONE
            binding.messageInputLayout.hint = getString(
                if (state.isTyping) R.string.chat_thinking else R.string.chat_message_hint
            )
            if (state.messages.isNotEmpty() || state.isTyping) {
                binding.recyclerViewChat.post {
                    val lastPosition = adapter.itemCount - 1
                    if (lastPosition >= 0) {
                        binding.recyclerViewChat.smoothScrollToPosition(lastPosition)
                    }
                }
            }
        }
    }

    private fun sendCurrentMessage() {
            val text = binding.editTextMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.editTextMessage.text?.clear()
            }
    }

    private fun sendPrompt(prompt: String) {
        binding.editTextMessage.setText(prompt)
        sendCurrentMessage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
