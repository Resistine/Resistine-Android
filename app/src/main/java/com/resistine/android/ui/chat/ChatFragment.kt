package com.resistine.android.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.resistine.android.R
import com.resistine.android.databinding.FragmentChatBinding
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin

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

        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = false
        }
        adapter = ChatAdapter(
            markwon = Markwon.builder(requireContext())
                .usePlugin(StrikethroughPlugin.create())
                .build(),
            onCopy = ::copyResponse
        )
        binding.recyclerViewChat.layoutManager = layoutManager
        binding.recyclerViewChat.adapter = adapter

        binding.buttonSend.setOnClickListener {
            sendCurrentMessage()
        }
        binding.buttonStop.setOnClickListener { viewModel.cancelResponse() }
        binding.buttonNewChat.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.chat_new_conversation)
                .setMessage(R.string.chat_new_conversation_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.chat_clear_action) { _, _ -> viewModel.clearHistory() }
                .show()
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
            val wasNearBottom = layoutManager.findLastVisibleItemPosition() >= adapter.itemCount - 2
            adapter.submitState(state)
            binding.buttonSend.visibility = if (state.isTyping) View.GONE else View.VISIBLE
            binding.buttonStop.visibility = if (state.isTyping) View.VISIBLE else View.GONE
            binding.editTextMessage.isEnabled = !state.isTyping
            binding.quickPrompts.visibility =
                if (state.messages.size <= 1 && !state.isTyping) View.VISIBLE else View.GONE
            binding.messageInputLayout.hint = getString(
                if (state.isTyping) R.string.chat_thinking else R.string.chat_message_hint
            )
            if (wasNearBottom || state.isTyping) {
                binding.recyclerViewChat.post {
                    val lastPosition = adapter.itemCount - 1
                    if (lastPosition >= 0) {
                        binding.recyclerViewChat.scrollToPosition(lastPosition)
                    }
                }
            }
            state.errorMessage?.let { message ->
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.chat_retry) { viewModel.retryLastResponse() }
                    .show()
                viewModel.consumeError()
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

    private fun copyResponse(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.chat_copy_label), text))
        Snackbar.make(binding.root, R.string.chat_copied, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
