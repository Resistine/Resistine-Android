package com.resistine.android.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
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

        adapter = ChatAdapter(emptyList())
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerViewChat.adapter = adapter

        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.editTextMessage.text.clear()
            }
        }

        // Sledování zpráv
        viewModel.messages.observe(viewLifecycleOwner) { list ->
            adapter.updateMessages(list, viewModel.isTyping.value ?: false)
            if (list.isNotEmpty() || viewModel.isTyping.value == true) {
                val scrollPos = if (viewModel.isTyping.value == true) list.size else list.size - 1
                if (scrollPos >= 0) {
                    binding.recyclerViewChat.smoothScrollToPosition(scrollPos)
                }
            }
        }
        
        // Sledování indikátoru psaní
        viewModel.isTyping.observe(viewLifecycleOwner) { isTyping ->
            // Aktualizovat adaptér, aby přidal/odebral bublinu
            adapter.updateMessages(viewModel.messages.value ?: emptyList(), isTyping)
            
            if (isTyping) {
                binding.buttonSend.isEnabled = false
                binding.editTextMessage.hint = "AI is thinking..."
                binding.recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
            } else {
                binding.buttonSend.isEnabled = true
                binding.editTextMessage.hint = "Type your message here"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
