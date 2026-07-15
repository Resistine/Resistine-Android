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
import androidx.navigation.fragment.findNavController
import com.resistine.android.R
import com.resistine.android.databinding.FragmentVpnBinding
import com.resistine.android.ui.login.LoginViewModel

class VpnFragment : Fragment() {

    private var _binding: FragmentVpnBinding? = null
    private val binding get() = _binding!!
    private val vpnViewModel: VpnViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private var registrationSkipped = false
    private var lastRenderedConnectionState: Boolean? = null

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                vpnViewModel.toggleVpn(requireContext())
            } else {
                binding.textViewVpnStatus.text = getString(R.string.vpn_access_denied)
                binding.buttonVpnToggle.isEnabled = true
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentVpnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val disabledMessage = getString(R.string.vpn_disabled_register_required)

        loginViewModel.isRegistrationSkipped.observe(viewLifecycleOwner) { skipped ->
            registrationSkipped = skipped == true
            if (registrationSkipped) {
                binding.buttonVpnToggle.isEnabled = false
                binding.textViewVpnStatus.text = disabledMessage
            }
        }

        configureActions(disabledMessage)
        vpnViewModel.uiState.observe(viewLifecycleOwner, ::render)
        vpnViewModel.refreshPendingFlowLogCount()
    }

    private fun configureActions(disabledMessage: String) = with(binding) {
        buttonVpnToggle.setOnClickListener {
            if (registrationSkipped) {
                Toast.makeText(requireContext(), disabledMessage, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            buttonVpnToggle.isEnabled = false
            val permissionIntent = VpnService.prepare(requireContext())
            if (permissionIntent != null) vpnPermissionLauncher.launch(permissionIntent)
            else vpnViewModel.toggleVpn(requireContext())
        }

        buttonOpenSettings.setOnClickListener {
            findNavController().navigate(R.id.nav_settings)
        }
    }

    private fun render(state: VpnUiState) = with(binding) {
        if (lastRenderedConnectionState != null && lastRenderedConnectionState != state.isConnected) {
            layoutRuntimeSelector.animate().cancel()
            layoutRuntimeSelector.scaleX = 0.985f
            layoutRuntimeSelector.scaleY = 0.985f
            layoutRuntimeSelector.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(220L)
                .start()
        }
        lastRenderedConnectionState = state.isConnected
        if (!registrationSkipped) textViewVpnStatus.text = state.statusMessage
        textViewRuntimeStatus.text = getString(
            R.string.vpn_runtime_status_format,
            state.runtimeStatus.label,
            state.runtimeStatus.detail
        )
        buttonVpnToggle.text = getString(
            if (state.isConnected) R.string.vpn_disconnect_action else R.string.vpn_connect_action
        )
        buttonVpnToggle.contentDescription = buttonVpnToggle.text
        buttonVpnToggle.isEnabled = !registrationSkipped

        textViewConnectionBadge.text = getString(
            if (state.isConnected) R.string.vpn_badge_on else R.string.vpn_badge_off
        )
        textViewConnectionBadge.setTextColor(
            resources.getColor(R.color.white, requireContext().theme)
        )

        textViewIpAddress.text = state.ipAddress
        textViewLocation.text = state.location
        textViewDeviceModel.text = state.deviceModel
        textViewAndroidVersion.text = state.androidVersion
        textViewBatteryLevel.text = state.batteryLevel
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
        _binding = null
        super.onDestroyView()
    }
}
