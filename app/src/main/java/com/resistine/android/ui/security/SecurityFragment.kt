package com.resistine.android.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.resistine.android.R
import com.resistine.android.databinding.FragmentSecurityBinding
import com.resistine.android.ui.vpn.VpnViewModel

class SecurityFragment : Fragment() {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!
    private val vpnViewModel: VpnViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe common data from VpnViewModel
        vpnViewModel.ipAddress.observe(viewLifecycleOwner) {
            binding.textViewIpAddress.text = it
        }

        vpnViewModel.locationString.observe(viewLifecycleOwner) {
            binding.textViewLocation.text = it
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

        // Wazuh connection observation
        vpnViewModel.isVpnConnectedLiveData.observe(viewLifecycleOwner) { isVpnUp ->
            updateWazuhStatus(isVpnUp)
        }
        
        // Refresh data on start
        vpnViewModel.refreshWifiSecurityAlert()
    }

    private fun updateWazuhStatus(isVpnUp: Boolean) {
        if (isVpnUp) {
            binding.textViewWazuhStatus.text = "Agent: Active & Monitoring"
            binding.imageViewWazuhStatus.setImageResource(R.drawable.shield_with_star)
            binding.imageViewWazuhStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.success_green))
        } else {
            binding.textViewWazuhStatus.text = "Agent: Offline (Requires VPN)"
            binding.imageViewWazuhStatus.setImageResource(R.drawable.shield_with_star)
            binding.imageViewWazuhStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error_red))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
