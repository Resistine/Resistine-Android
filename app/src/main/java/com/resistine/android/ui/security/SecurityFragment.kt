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
import com.resistine.android.network.WazuhConnectionMonitor
import com.resistine.android.network.WazuhConnectionState
import com.resistine.android.network.WazuhConnectionStatus
import com.resistine.android.ui.vpn.VpnViewModel

/**
 * Fragment that displays a detailed security and system status overview.
 * 
 * It shows information about:
 * - Wazuh Agent connectivity
 * - Public IP and Geographic Location
 * - Device Hardware (Model, Android Version, Battery)
 */
class SecurityFragment : Fragment() {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!
    
    /**
     * Sharing the activity-scoped ViewModel to get real-time network and device data.
     */
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

        // Observe network and device data from VpnViewModel
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

        WazuhConnectionMonitor.status.observe(viewLifecycleOwner) { status ->
            updateWazuhStatus(status)
        }
        
        // Refresh Wi-Fi alerts to ensure we have fresh data
        vpnViewModel.refreshWifiSecurityAlert()
    }

    /**
     * Updates the UI representation of the Wazuh Agent status.
     * 
     * @param status Actual uploader socket state reported by WazuhService.
     */
    private fun updateWazuhStatus(status: WazuhConnectionStatus) {
        binding.textViewWazuhStatus.text = "Agent: ${status.detail}"
        binding.imageViewWazuhStatus.setImageResource(R.drawable.shield_with_star)
        val color = when (status.state) {
            WazuhConnectionState.CONNECTED -> R.color.success_green
            WazuhConnectionState.LOCAL_ONLY,
            WazuhConnectionState.ENROLLING,
            WazuhConnectionState.CONNECTING,
            WazuhConnectionState.WAITING_FOR_VPN -> R.color.dark_blue
            WazuhConnectionState.STOPPED,
            WazuhConnectionState.MISSING_CREDENTIALS,
            WazuhConnectionState.ERROR -> R.color.error_red
        }
        binding.imageViewWazuhStatus.setColorFilter(ContextCompat.getColor(requireContext(), color))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
