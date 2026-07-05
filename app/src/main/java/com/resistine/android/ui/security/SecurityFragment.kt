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
import com.resistine.android.databinding.ItemSecurityCategoryBinding
import com.resistine.android.databinding.ItemSecurityCheckBinding
import com.resistine.android.ui.vpn.VpnViewModel

/**
 * Fragment that displays a detailed security and system status overview.
 *
 * It shows information about:
 * - Wazuh Agent connectivity
 * - Public IP and Geographic Location
 * - Device Hardware (Model, Android Version, Battery)
 * - Core Security Checks (Updates, Lock, Radio Surface, etc.)
 */
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

        // Basic Info Observations
        vpnViewModel.ipAddress.observe(viewLifecycleOwner) { binding.textViewIpAddress.text = it }
        vpnViewModel.locationString.observe(viewLifecycleOwner) { binding.textViewLocation.text = it }
//        vpnViewModel.deviceModel.observe(viewLifecycleOwner) { binding.textViewDeviceModel.text = it }
//        vpnViewModel.androidVersion.observe(viewLifecycleOwner) { binding.textViewAndroidVersion.text = it }
//        vpnViewModel.batteryLevel.observe(viewLifecycleOwner) { binding.textViewBatteryLevel.text = it }

        // Agent status observation
        vpnViewModel.isVpnConnectedLiveData.observe(viewLifecycleOwner) { isVpnUp ->
            updateWazuhStatus(isVpnUp)
        }

        // System Security Checks observation
        vpnViewModel.securityChecks.observe(viewLifecycleOwner) { checks ->
            updateSecurityChecksUI(checks)
        }
        
        // Refresh data on start
        vpnViewModel.refreshWifiSecurityAlert()
    }

    override fun onResume() {
        super.onResume()
        // Ensure checks are up to date when returning to screen
        vpnViewModel.refreshWifiSecurityAlert()
    }

    /**
     * Dynamically builds the UI rows for security checks grouped by category.
     */
    private fun updateSecurityChecksUI(checks: List<SecurityCheckItem>) {
        val container = binding.containerSecurityChecks
        container.removeAllViews()

        if (checks.isEmpty()) return

        var currentCategory: SecurityCategory? = null

        for (check in checks) {
            // Add Category Header if it changed
            if (check.category != currentCategory) {
                currentCategory = check.category
                val categoryBinding = ItemSecurityCategoryBinding.inflate(layoutInflater, container, false)
                categoryBinding.textCategoryTitle.text = check.category.title
                container.addView(categoryBinding.root)
            }

            // Add Check Row
            val checkBinding = ItemSecurityCheckBinding.inflate(layoutInflater, container, false)
            checkBinding.textTitle.text = check.title
            checkBinding.textValue.text = check.value
            checkBinding.textDescription.text = check.description
            
            val statusColor = when (check.status) {
                SecurityStatus.SAFE -> R.color.success_green
                SecurityStatus.WARNING -> R.color.wifi_risk_warning_text
                SecurityStatus.DANGER -> R.color.error_red
                SecurityStatus.INFO -> R.color.dark_blue
            }
            checkBinding.imageStatus.setColorFilter(ContextCompat.getColor(requireContext(), statusColor))
            
            container.addView(checkBinding.root)

            // Add simple divider
            val divider = View(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_grey))
                alpha = 0.5f
            }
            container.addView(divider)
        }
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
