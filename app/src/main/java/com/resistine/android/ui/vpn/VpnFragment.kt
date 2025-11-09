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
import com.resistine.android.R
import com.resistine.android.databinding.FragmentVpnBinding
import com.resistine.android.ui.login.LoginViewModel

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
                binding.textViewVpnStatus.text = getString(R.string.VPN_access_denied)
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

        loginViewModel.isRegistrationSkipped.observe(viewLifecycleOwner) { isSkipped ->
            if (isSkipped == true) {
                binding.buttonVpnToggle.isEnabled = false
                binding.textViewVpnStatus.text = "VPN is disabled. Please register to enable."
                Toast.makeText(context, "VPN is disabled. Please register to enable.", Toast.LENGTH_LONG).show()
            } else {
                binding.buttonVpnToggle.isEnabled = true
            }
        }

        binding.buttonVpnToggle.setOnClickListener {
            if (loginViewModel.isRegistrationSkipped.value == true) {
                Toast.makeText(requireContext(), "VPN is disabled. Please register to enable.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
            }
            binding.buttonVpnToggle.text =
                if (status.contains("VPN connected", true)) "Disconnect VPN"
                else "Connect VPN"
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
