package com.resistine.android.ui.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.resistine.android.databinding.FragmentAgentBinding

class AgentFragment : Fragment() {

    private var _binding: FragmentAgentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val agentViewModel = ViewModelProvider(this).get(AgentViewModel::class.java)
        _binding = FragmentAgentBinding.inflate(inflater, container, false)

        agentViewModel.text.observe(viewLifecycleOwner) { binding.textAgent.text = it }

        agentViewModel.userEmail.observe(viewLifecycleOwner) { email ->
            if (!email.isNullOrEmpty() && binding.editAgentEmail.text.isEmpty()) {
                binding.editAgentEmail.setText(email)
            }
        }

        agentViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateUIState(isLoading, agentViewModel.isRegistered.value ?: false)
        }

        agentViewModel.isRegistered.observe(viewLifecycleOwner) { isRegistered ->
            updateUIState(agentViewModel.isLoading.value ?: false, isRegistered)
        }

        // Button 1: Registration
        binding.btnRegisterWazuh.setOnClickListener {
            val email = binding.editAgentEmail.text.toString()
            if (email.isNotBlank()) agentViewModel.registerAgent(email)
        }

        // Button 2: Connection (Start Keepalive)
        binding.btnConnectAgent.setOnClickListener {
            agentViewModel.connectAgent()
        }

        // Button 3: Manual log submission (New)
        binding.btnSendLog.setOnClickListener {
            agentViewModel.sendManualLog()
        }

        return binding.root
    }

    private fun updateUIState(isLoading: Boolean, isRegistered: Boolean) {
        binding.progressAgent.visibility = if (isLoading) View.VISIBLE else View.GONE

        binding.btnRegisterWazuh.isEnabled = !isLoading && !isRegistered
        binding.editAgentEmail.isEnabled = !isLoading && !isRegistered

        // Both buttons for communication with port 1514 are enabled only after registration
        binding.btnConnectAgent.isEnabled = !isLoading && isRegistered
        binding.btnSendLog.isEnabled = !isLoading && isRegistered
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}