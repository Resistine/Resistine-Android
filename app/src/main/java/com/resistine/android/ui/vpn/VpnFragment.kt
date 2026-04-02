package com.resistine.android.ui.vpn

import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.resistine.android.R
import com.resistine.android.databinding.FragmentVpnBinding
import com.resistine.android.ui.login.LoginViewModel

class VpnFragment : Fragment() {

    private var _binding: FragmentVpnBinding? = null
    private val binding get() = _binding!!
    private val vpnViewModel: VpnViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by activityViewModels()
    private var originalButtonBackground: Drawable? = null

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                vpnViewModel.toggleVpn(requireContext())
            } else {
                binding.textViewVpnStatus.text = getString(R.string.vpn_access_denied)
                setButtonState(binding.buttonVpnToggle, true)
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
        originalButtonBackground = binding.buttonVpnToggle.background
        val disabledMessage = getString(R.string.vpn_disabled_register_required)
        val connectedKeyword = getString(R.string.vpn_status_connected)

        loginViewModel.isRegistrationSkipped.observe(viewLifecycleOwner) { isSkipped ->
            if (isSkipped == true) {
                setButtonState(binding.buttonVpnToggle, false)
                binding.textViewVpnStatus.text = disabledMessage
                Toast.makeText(context, disabledMessage, Toast.LENGTH_LONG).show()
            } else {
                setButtonState(binding.buttonVpnToggle, true)
            }
        }

        binding.buttonVpnToggle.setOnClickListener {
            if (loginViewModel.isRegistrationSkipped.value == true) {
                Toast.makeText(requireContext(), disabledMessage, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setButtonState(binding.buttonVpnToggle, false)
            val intent = VpnService.prepare(requireContext())
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
            } else {
                vpnViewModel.toggleVpn(requireContext())
            }
        }

        vpnViewModel.vpnStatus.observe(viewLifecycleOwner) { status ->
            if (loginViewModel.isRegistrationSkipped.value != true) {
                binding.textViewVpnStatus.text = status
                setButtonState(binding.buttonVpnToggle, true)
            }
            binding.buttonVpnToggle.text =
                if (status.contains(connectedKeyword, ignoreCase = true)) {
                    getString(R.string.disconnect_vpn)
                } else {
                    getString(R.string.connect_vpn)
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

    private fun setButtonState(button: Button, isEnabled: Boolean) {
        button.isEnabled = isEnabled
        if (isEnabled) {
            button.background = originalButtonBackground
        } else {
            button.setBackgroundResource(R.drawable.button_background_disabled)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
