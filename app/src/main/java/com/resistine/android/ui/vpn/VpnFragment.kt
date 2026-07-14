package com.resistine.android.ui.vpn

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.resistine.android.R
import com.resistine.android.databinding.FragmentVpnBinding
import com.resistine.android.network.WazuhConnectionMonitor
import com.resistine.android.network.flow.FlowWazuhDeliveryMode
import com.resistine.android.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class VpnFragment : Fragment() {

    private var _binding: FragmentVpnBinding? = null
    private val binding get() = _binding!!
    private val vpnViewModel: VpnViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                vpnViewModel.toggleVpn(requireContext())
            } else {
                binding.textViewVpnStatus.text = getString(R.string.vpn_access_denied)
                binding.switchVpnToggle.isEnabled = true
                binding.switchVpnToggle.isChecked = false
            }
        }

    private val managerCaPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null || _binding == null) return@registerForActivityResult
            viewLifecycleOwner.lifecycleScope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        requireContext().contentResolver.openInputStream(uri)?.use(::readManagerCa)
                            ?: error("Could not open the selected CA file")
                    }
                }.mapCatching { pem ->
                    vpnViewModel.importWazuhManagerCa(pem)?.let(::error)
                }
                refreshWazuhSecurityStatus()
                Toast.makeText(
                    requireContext(),
                    result.exceptionOrNull()?.message
                        ?: getString(R.string.flow_wazuh_manager_ca_saved),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVpnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val disabledMessage = getString(R.string.vpn_disabled_register_required)

        loginViewModel.isRegistrationSkipped.observe(viewLifecycleOwner) { isSkipped ->
            if (isSkipped == true) {
                binding.switchVpnToggle.isEnabled = false
                binding.textViewVpnStatus.text = disabledMessage
                Toast.makeText(context, disabledMessage, Toast.LENGTH_LONG).show()
            } else {
                binding.switchVpnToggle.isEnabled = true
            }
        }

        binding.switchVpnToggle.setOnClickListener {
            if (
                loginViewModel.isRegistrationSkipped.value == true
            ) {
                binding.switchVpnToggle.isChecked = false
                Toast.makeText(requireContext(), disabledMessage, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lock the switch while state is changing
            binding.switchVpnToggle.isEnabled = false

            val intent = VpnService.prepare(requireContext())
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
            } else {
                vpnViewModel.toggleVpn(requireContext())
            }
        }

        val endpoint = vpnViewModel.wazuhManagerEndpoint()
        binding.editTextWazuhManagerHost.setText(endpoint.host)
        binding.editTextWazuhAuthPort.setText(endpoint.authPort.toString())
        binding.editTextWazuhLogPort.setText(endpoint.logPort.toString())

        binding.radioGroupFlowWazuhDelivery.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioFlowWazuhRemote -> FlowWazuhDeliveryMode.REMOTE_MANAGER
                else -> FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY
            }
            vpnViewModel.setFlowWazuhDeliveryMode(mode)
        }

        binding.buttonSaveWazuhEndpoint.setOnClickListener {
            val authPort = binding.editTextWazuhAuthPort.text?.toString()?.toIntOrNull()
            val logPort = binding.editTextWazuhLogPort.text?.toString()?.toIntOrNull()
            if (authPort == null || logPort == null) {
                Toast.makeText(
                    requireContext(),
                    R.string.flow_wazuh_invalid_port,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            var error = vpnViewModel.updateWazuhManagerEndpoint(
                host = binding.editTextWazuhManagerHost.text?.toString().orEmpty(),
                authPort = authPort,
                logPort = logPort
            )
            val enrollmentPassword =
                binding.editTextWazuhEnrollmentPassword.text?.toString().orEmpty()
            if (error == null && enrollmentPassword.isNotEmpty()) {
                error = vpnViewModel.saveWazuhEnrollmentPassword(enrollmentPassword)
                if (error == null) {
                    binding.editTextWazuhEnrollmentPassword.text?.clear()
                }
            }
            refreshWazuhSecurityStatus()
            Toast.makeText(
                requireContext(),
                error ?: getString(R.string.flow_wazuh_endpoint_saved),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.buttonClearWazuhEnrollmentPassword.setOnClickListener {
            val error = vpnViewModel.clearWazuhEnrollmentPassword()
            binding.editTextWazuhEnrollmentPassword.text?.clear()
            refreshWazuhSecurityStatus()
            Toast.makeText(
                requireContext(),
                error ?: getString(R.string.flow_wazuh_enrollment_password_cleared),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.buttonImportWazuhCa.setOnClickListener {
            managerCaPicker.launch(
                arrayOf(
                    "application/x-pem-file",
                    "application/x-x509-ca-cert",
                    "text/plain",
                    "application/octet-stream"
                )
            )
        }

        binding.buttonClearWazuhCa.setOnClickListener {
            val error = vpnViewModel.clearWazuhManagerCa()
            refreshWazuhSecurityStatus()
            Toast.makeText(
                requireContext(),
                error ?: getString(R.string.flow_wazuh_manager_ca_cleared),
                Toast.LENGTH_SHORT
            ).show()
        }

        refreshWazuhSecurityStatus()

        binding.layoutFlowWazuhSettings.visibility = View.VISIBLE
        vpnViewModel.refreshPendingFlowLogCount()

        vpnViewModel.flowWazuhDeliveryMode.observe(viewLifecycleOwner) { mode ->
            val checkedId = when (mode) {
                FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY -> R.id.radioFlowWazuhLocal
                FlowWazuhDeliveryMode.REMOTE_MANAGER -> R.id.radioFlowWazuhRemote
            }
            if (binding.radioGroupFlowWazuhDelivery.checkedRadioButtonId != checkedId) {
                binding.radioGroupFlowWazuhDelivery.check(checkedId)
            }
            binding.layoutRemoteWazuhEndpoint.visibility =
                if (mode == FlowWazuhDeliveryMode.REMOTE_MANAGER) View.VISIBLE else View.GONE
            updateFlowWazuhStatus(mode, vpnViewModel.pendingFlowLogCount.value ?: 0)
        }

        vpnViewModel.pendingFlowLogCount.observe(viewLifecycleOwner) { count ->
            updateFlowWazuhStatus(
                vpnViewModel.flowWazuhDeliveryMode.value
                    ?: FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY,
                count
            )
        }

        WazuhConnectionMonitor.status.observe(viewLifecycleOwner) {
            updateFlowWazuhStatus(
                vpnViewModel.flowWazuhDeliveryMode.value
                    ?: FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY,
                vpnViewModel.pendingFlowLogCount.value ?: 0
            )
        }

        vpnViewModel.vpnRuntimeStatus.observe(viewLifecycleOwner) { status ->
            binding.textViewRuntimeStatus.text = getString(
                R.string.vpn_runtime_status_format,
                status.label,
                status.detail
            )
            binding.switchVpnToggle.text = when {
                status.isRunning ->
                    getString(R.string.disconnect_vpn)
                else ->
                    getString(R.string.connect_vpn)
            }
            val settingsEnabled = !status.isRunning
            binding.radioFlowWazuhLocal.isEnabled = settingsEnabled
            binding.radioFlowWazuhRemote.isEnabled = settingsEnabled
            binding.editTextWazuhManagerHost.isEnabled = settingsEnabled
            binding.editTextWazuhAuthPort.isEnabled = settingsEnabled
            binding.editTextWazuhLogPort.isEnabled = settingsEnabled
            binding.editTextWazuhEnrollmentPassword.isEnabled = settingsEnabled
            binding.buttonSaveWazuhEndpoint.isEnabled = settingsEnabled
            binding.buttonClearWazuhEnrollmentPassword.isEnabled = settingsEnabled
            binding.buttonImportWazuhCa.isEnabled = settingsEnabled
            binding.buttonClearWazuhCa.isEnabled = settingsEnabled
            if (!status.isRunning) {
                vpnViewModel.refreshPendingFlowLogCount()
            }
        }

        vpnViewModel.isVpnConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
            binding.switchVpnToggle.isChecked = isConnected
            binding.switchVpnToggle.isEnabled = loginViewModel.isRegistrationSkipped.value != true
        }

        vpnViewModel.vpnStatus.observe(viewLifecycleOwner) { status ->
            if (
                loginViewModel.isRegistrationSkipped.value != true
            ) {
                binding.textViewVpnStatus.text = status
            }
        }

        vpnViewModel.ipAddress.observe(viewLifecycleOwner) {
            binding.textViewIpAddress.text = it
        }

        vpnViewModel.deviceModel.observe(viewLifecycleOwner) {
            binding.textViewDeviceModel.text = it
        }

        vpnViewModel.androidVersion.observe(viewLifecycleOwner) {
            binding.textViewAndroidVersion.text = it
        }

        vpnViewModel.batteryLevel.observe(viewLifecycleOwner) {
            binding.textViewBatteryLevel.text = it
        }

        vpnViewModel.locationString.observe(viewLifecycleOwner) {
            binding.textViewLocation.text = it
        }

        vpnViewModel.refreshWifiSecurityAlert()
    }

    private fun updateFlowWazuhStatus(mode: FlowWazuhDeliveryMode, queuedCount: Int) {
        binding.textViewFlowWazuhStatus.text = when (mode) {
            FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY ->
                getString(R.string.flow_wazuh_local_status, queuedCount)
            FlowWazuhDeliveryMode.REMOTE_MANAGER ->
                getString(
                    R.string.flow_wazuh_remote_status_format,
                    WazuhConnectionMonitor.status.value?.detail
                        ?: getString(R.string.flow_wazuh_remote_status)
                )
        }
    }

    private fun refreshWazuhSecurityStatus() {
        binding.textViewWazuhEnrollmentPasswordStatus.setText(
            if (vpnViewModel.hasWazuhEnrollmentPassword()) {
                R.string.flow_wazuh_enrollment_password_set
            } else {
                R.string.flow_wazuh_enrollment_password_not_set
            }
        )
        binding.textViewWazuhCaStatus.setText(
            if (vpnViewModel.hasWazuhManagerCa()) {
                R.string.flow_wazuh_manager_ca_set
            } else {
                R.string.flow_wazuh_manager_ca_not_set
            }
        )
    }

    private fun readManagerCa(input: java.io.InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            output.write(buffer, 0, read)
            require(output.size() <= MAX_MANAGER_CA_BYTES) {
                getString(R.string.flow_wazuh_manager_ca_too_large)
            }
        }
        return output.toString(Charsets.US_ASCII.name())
    }

    override fun onStart() {
        super.onStart()
        vpnViewModel.startWifiMonitoring()
    }

    override fun onStop() {
        vpnViewModel.stopWifiMonitoring()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        vpnViewModel.refreshWifiSecurityAlert()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        private const val MAX_MANAGER_CA_BYTES = 256 * 1024
    }
}
