package com.resistine.android.ui.vpn

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.resistine.android.R
import com.resistine.android.databinding.FragmentVpnBinding
import com.resistine.android.ui.login.LoginViewModel
import java.text.DateFormat
import java.util.Date

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
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.any { it.value } || hasLocationPermission()
            if (!granted) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.wifi_security_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
            vpnViewModel.refreshWifiSecurityAlert()
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
                if (status.contains(connectedKeyword, ignoreCase = true)) getString(R.string.disconnect_vpn)
                else getString(R.string.connect_vpn)
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

        binding.buttonRefreshNetworkScan.setOnClickListener {
            vpnViewModel.refreshWifiSecurityAlert()
            vpnViewModel.fetchLocationData()
        }
        binding.buttonGrantWifiPermission.setOnClickListener {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        vpnViewModel.wifiSecurityAlert.observe(viewLifecycleOwner) { alert ->
            val titleRes = when (alert.level) {
                WifiAlertLevel.SECURE -> R.string.wifi_security_title_secure
                WifiAlertLevel.WARNING -> R.string.wifi_security_title_warning
                WifiAlertLevel.INFO -> R.string.wifi_security_title_info
            }
            val recommendationRes = when (alert.level) {
                WifiAlertLevel.SECURE -> R.string.wifi_security_recommendation_secure
                WifiAlertLevel.WARNING -> R.string.wifi_security_recommendation_warning
                WifiAlertLevel.INFO -> R.string.wifi_security_recommendation_info
            }
            val (iconRes, iconDescRes) = when (alert.level) {
                WifiAlertLevel.SECURE -> Pair(
                    R.drawable.ic_wifi_status_safe,
                    R.string.wifi_status_icon_safe_desc
                )

                WifiAlertLevel.WARNING -> Pair(
                    R.drawable.ic_wifi_status_danger,
                    R.string.wifi_status_icon_danger_desc
                )

                WifiAlertLevel.INFO -> Pair(
                    R.drawable.ic_wifi_status_warning,
                    R.string.wifi_status_icon_warning_desc
                )
            }
            binding.imageViewWifiSecurityIcon.setImageResource(iconRes)
            binding.imageViewWifiSecurityIcon.contentDescription = getString(iconDescRes)
            binding.textViewWifiSecurityTitle.text = getString(titleRes)
            binding.textViewWifiSecurityBody.text = alert.message
            binding.textViewWifiSecurityType.text = getString(
                R.string.wifi_security_type_line,
                getString(securityTypeLabelRes(alert.securityType)),
            )
            binding.textViewWifiSecurityReason.text = getString(
                R.string.wifi_security_reason_line,
                getString(reasonLabelRes(alert.reason)),
            )
            binding.textViewWifiSecurityLastChecked.text = getString(
                R.string.wifi_security_last_checked_line,
                formatCheckedAt(alert.checkedAtMillis)
            )
            binding.textViewWifiSecurityRecommendation.text = getString(recommendationRes)
            binding.buttonGrantWifiPermission.visibility =
                if (alert.requiresLocationPermission) View.VISIBLE else View.GONE
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

    private fun hasLocationPermission(): Boolean {
        val context = requireContext()
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun securityTypeLabelRes(type: WifiSecurityType?): Int {
        return when (type) {
            WifiSecurityType.OPEN -> R.string.wifi_security_type_open
            WifiSecurityType.WEP -> R.string.wifi_security_type_wep
            WifiSecurityType.SECURE -> R.string.wifi_security_type_secure
            WifiSecurityType.UNKNOWN,
            null -> R.string.wifi_security_type_unknown
        }
    }

    private fun reasonLabelRes(reason: WifiAlertReason): Int {
        return when (reason) {
            WifiAlertReason.UNAVAILABLE -> R.string.wifi_reason_unavailable
            WifiAlertReason.NO_NETWORK -> R.string.wifi_reason_no_network
            WifiAlertReason.NOT_WIFI -> R.string.wifi_reason_not_wifi
            WifiAlertReason.CAPTIVE_PORTAL -> R.string.wifi_reason_captive_portal
            WifiAlertReason.UNVALIDATED -> R.string.wifi_reason_unvalidated
            WifiAlertReason.LEGACY_NO_SECURITY_TYPE -> R.string.wifi_reason_legacy
            WifiAlertReason.MISSING_PERMISSION -> R.string.wifi_reason_missing_permission
            WifiAlertReason.OPEN_OR_WEP -> R.string.wifi_reason_open_or_wep
            WifiAlertReason.UNKNOWN_SECURITY -> R.string.wifi_reason_unknown_security
            WifiAlertReason.SECURE -> R.string.wifi_reason_secure
        }
    }

    private fun formatCheckedAt(timestampMs: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date(timestampMs))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
