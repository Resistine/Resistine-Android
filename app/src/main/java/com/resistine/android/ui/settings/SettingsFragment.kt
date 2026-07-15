package com.resistine.android.ui.settings

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.resistine.android.R
import com.resistine.android.databinding.FragmentSettingsBinding
import com.resistine.android.network.flow.FlowWazuhDeliveryMode
import com.resistine.android.ui.vpn.VpnUiState
import com.resistine.android.ui.vpn.VpnViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val vpnViewModel: VpnViewModel by activityViewModels()
    private var renderingDeliveryState = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateEndpointFields()
        binding.editConfiguration.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.switchShowConfiguration.setOnCheckedChangeListener { _, isChecked ->
            val selection = binding.editConfiguration.selectionStart.coerceAtLeast(0)
            binding.editConfiguration.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
            binding.editConfiguration.setSelection(
                selection.coerceAtMost(binding.editConfiguration.length())
            )
        }

        binding.buttonSaveConfiguration.setOnClickListener {
            settingsViewModel.saveConfiguration(binding.editConfiguration.text?.toString().orEmpty())
        }
        binding.buttonRestoreConfiguration.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_restore_title)
                .setMessage(R.string.settings_restore_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.settings_restore_action) { _, _ ->
                    settingsViewModel.restoreRegistrationDefault()
                }
                .show()
        }

        binding.radioGroupFlowWazuhDelivery.setOnCheckedChangeListener { _, checkedId ->
            if (renderingDeliveryState) return@setOnCheckedChangeListener
            vpnViewModel.setFlowWazuhDeliveryMode(
                if (checkedId == R.id.radioFlowWazuhRemote) {
                    FlowWazuhDeliveryMode.REMOTE_MANAGER
                } else {
                    FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY
                }
            )
        }
        binding.buttonSaveWazuhEndpoint.setOnClickListener {
            val authPort = binding.editTextWazuhAuthPort.text?.toString()?.toIntOrNull()
            val logPort = binding.editTextWazuhLogPort.text?.toString()?.toIntOrNull()
            if (authPort == null || logPort == null) {
                Toast.makeText(requireContext(), R.string.flow_wazuh_invalid_port, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val error = vpnViewModel.updateWazuhManagerEndpoint(
                host = binding.editTextWazuhManagerHost.text?.toString().orEmpty(),
                authPort = authPort,
                logPort = logPort
            )
            Toast.makeText(
                requireContext(),
                error ?: getString(R.string.flow_wazuh_endpoint_saved),
                Toast.LENGTH_SHORT
            ).show()
        }

        settingsViewModel.uiState.observe(viewLifecycleOwner, ::renderConfiguration)
        vpnViewModel.uiState.observe(viewLifecycleOwner, ::renderDelivery)
    }

    private fun renderConfiguration(state: SettingsUiState) = with(binding) {
        if (!editConfiguration.hasFocus() && editConfiguration.text?.toString() != state.configuration) {
            editConfiguration.setText(state.configuration)
        }
        textConfigurationStatus.text = getString(
            if (state.hasConfiguration) {
                R.string.settings_config_ready
            } else {
                R.string.settings_config_missing
            }
        )
        buttonSaveConfiguration.isEnabled = !state.isSaving && state.hasConfiguration
        buttonRestoreConfiguration.isEnabled =
            !state.isSaving && state.canRestoreRegistrationDefault
        configurationProgress.visibility = if (state.isSaving) View.VISIBLE else View.GONE
        state.message?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            settingsViewModel.consumeMessage()
        }
    }

    private fun renderDelivery(state: VpnUiState) = with(binding) {
        renderingDeliveryState = true
        radioGroupFlowWazuhDelivery.check(
            if (state.deliveryMode == FlowWazuhDeliveryMode.REMOTE_MANAGER) {
                R.id.radioFlowWazuhRemote
            } else {
                R.id.radioFlowWazuhLocal
            }
        )
        renderingDeliveryState = false

        layoutRemoteWazuhEndpoint.visibility =
            if (state.deliveryMode == FlowWazuhDeliveryMode.REMOTE_MANAGER) View.VISIBLE else View.GONE
        textViewFlowWazuhStatus.text = when (state.deliveryMode) {
            FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY ->
                getString(R.string.flow_wazuh_local_status, state.queuedRecords)
            FlowWazuhDeliveryMode.REMOTE_MANAGER ->
                getString(R.string.flow_wazuh_remote_status_format, state.wazuhStatus)
        }
        radioFlowWazuhLocal.isEnabled = state.settingsEnabled
        radioFlowWazuhRemote.isEnabled = state.settingsEnabled
        editTextWazuhManagerHost.isEnabled = state.settingsEnabled
        editTextWazuhAuthPort.isEnabled = state.settingsEnabled
        editTextWazuhLogPort.isEnabled = state.settingsEnabled
        buttonSaveWazuhEndpoint.isEnabled = state.settingsEnabled
        textSettingsLocked.visibility = if (state.settingsEnabled) View.GONE else View.VISIBLE
    }

    private fun populateEndpointFields() {
        val endpoint = vpnViewModel.wazuhManagerEndpoint()
        binding.editTextWazuhManagerHost.setText(endpoint.host)
        binding.editTextWazuhAuthPort.setText(endpoint.authPort.toString())
        binding.editTextWazuhLogPort.setText(endpoint.logPort.toString())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
