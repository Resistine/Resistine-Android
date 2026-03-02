package com.resistine.android.ui.wifi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.resistine.android.R
import com.resistine.android.databinding.FragmentWifiSecurityBinding
import com.resistine.android.ui.vpn.AutoVpnPolicy
import com.resistine.android.ui.vpn.TrustedWifiProfile
import com.resistine.android.ui.vpn.VpnViewModel
import com.resistine.android.ui.vpn.WifiAdvancedCheckItem
import com.resistine.android.ui.vpn.WifiAlertReason
import com.resistine.android.ui.vpn.WifiBackgroundScanState
import com.resistine.android.ui.vpn.WifiNearbyNetwork
import com.resistine.android.ui.vpn.WifiNearbyNetworksState
import com.resistine.android.ui.vpn.WifiNetworkRiskLevel
import com.resistine.android.ui.vpn.WifiRiskTransitionAlert
import com.resistine.android.ui.vpn.WifiSafetyAssessment
import com.resistine.android.ui.vpn.WifiSecurityAlert
import com.resistine.android.ui.vpn.WifiSecurityType
import java.text.DateFormat
import java.util.Date

class WifiSecurityFragment : Fragment() {

    private var _binding: FragmentWifiSecurityBinding? = null
    private val binding get() = _binding!!
    private val vpnViewModel: VpnViewModel by activityViewModels()

    private var isAdvancedChecksExpanded = false
    private var isNearbyNetworksExpanded = false
    private var isTrustedNetworksExpanded = false
    private var isCurrentWifiDetailsExpanded = true
    private var latestWifiAlert: WifiSecurityAlert? = null
    private var latestSafetyAssessment: WifiSafetyAssessment? = null
    private var latestAdvancedChecks: List<WifiAdvancedCheckItem> = emptyList()
    private var lastHandledRiskAlertId: Long = -1L
    private var isBindingPolicyControls = false

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
        _binding = FragmentWifiSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRefreshWifiSecurity.setOnClickListener {
            vpnViewModel.refreshWifiSecurityAlert()
        }

        binding.buttonGrantWifiPermission.setOnClickListener {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        binding.buttonToggleTrustedWifi.setOnClickListener {
            val messageResId = vpnViewModel.toggleCurrentNetworkTrusted()
            Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT).show()
        }

        binding.layoutCurrentWifiHeader.setOnClickListener {
            isCurrentWifiDetailsExpanded = !isCurrentWifiDetailsExpanded
            updateCurrentWifiDetailsVisibility()
        }

        binding.textViewWifiSafetyScore.setOnClickListener {
            showScoreDetailsDialog()
        }
        binding.textViewScoreBreakdownTitle.setOnClickListener {
            showScoreDetailsDialog()
        }

        binding.switchAutoVpnProtect.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingPolicyControls) return@setOnCheckedChangeListener
            vpnViewModel.setAutoVpnEnabled(isChecked)
        }
        binding.switchAutoVpnDisconnectSafe.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingPolicyControls) return@setOnCheckedChangeListener
            vpnViewModel.setAutoVpnDisconnectOnSafe(isChecked)
        }
        binding.buttonAutoVpnDisconnectNow.setOnClickListener {
            vpnViewModel.disconnectVpnIfConnected()
        }
        binding.switchBackgroundScan.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingPolicyControls) return@setOnCheckedChangeListener
            vpnViewModel.setBackgroundScanEnabled(isChecked)
        }

        binding.layoutAdvancedChecksHeader.setOnClickListener {
            isAdvancedChecksExpanded = !isAdvancedChecksExpanded
            updateAdvancedChecksSectionVisibility()
        }
        binding.layoutAvailableNetworksHeader.setOnClickListener {
            isNearbyNetworksExpanded = !isNearbyNetworksExpanded
            updateNearbyNetworksSectionVisibility()
        }
        binding.layoutTrustedNetworksHeader.setOnClickListener {
            isTrustedNetworksExpanded = !isTrustedNetworksExpanded
            updateTrustedNetworksSectionVisibility()
        }

        updateAdvancedChecksSectionVisibility()
        updateNearbyNetworksSectionVisibility()
        updateTrustedNetworksSectionVisibility()
        updateCurrentWifiDetailsVisibility()

        vpnViewModel.wifiSafetyAssessment.observe(viewLifecycleOwner) { assessment ->
            renderSafetyAssessment(assessment)
        }

        vpnViewModel.wifiRiskTransitionAlert.observe(viewLifecycleOwner) { alert ->
            handleRiskTransitionAlert(alert)
        }

        vpnViewModel.autoVpnActionMessageRes.observe(viewLifecycleOwner) { messageResId ->
            if (messageResId == null) return@observe
            Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT).show()
            vpnViewModel.clearAutoVpnActionMessage()
        }

        vpnViewModel.autoVpnPolicy.observe(viewLifecycleOwner) { policy ->
            renderAutoVpnPolicy(policy)
        }

        vpnViewModel.isVpnConnectedLiveData.observe(viewLifecycleOwner) { connected ->
            binding.buttonAutoVpnDisconnectNow.visibility = if (connected) View.VISIBLE else View.GONE
        }

        vpnViewModel.backgroundScanState.observe(viewLifecycleOwner) { state ->
            renderBackgroundScanState(state)
        }

        vpnViewModel.wifiSecurityAlert.observe(viewLifecycleOwner) { alert ->
            latestWifiAlert = alert
            binding.textViewWifiNetworkIdentity.text = getString(
                R.string.wifi_network_line,
                getNetworkIdentityText(alert)
            )
            binding.textViewWifiInternetAccess.text = getString(
                R.string.wifi_internet_access_line,
                getString(
                    internetAccessLabelRes(
                        hasInternetAccess = alert.hasInternetAccess,
                        notApplicable = alert.reason == WifiAlertReason.NOT_WIFI
                    )
                )
            )
            binding.textViewWifiSecurityType.text = getString(
                R.string.wifi_security_type_line,
                getString(securityTypeLabelRes(alert.securityType))
            )
            binding.textViewWifiSecurityReason.text = getString(
                R.string.wifi_security_reason_line,
                getString(reasonLabelRes(alert.reason))
            )
            binding.textViewWifiTrustStatus.text = getString(
                R.string.wifi_trust_status_line,
                getString(
                    if (alert.isTrustedNetwork) {
                        R.string.wifi_trust_status_trusted
                    } else {
                        R.string.wifi_trust_status_not_trusted
                    }
                )
            )
            binding.textViewWifiLastChecked.text = getString(
                R.string.wifi_security_last_checked_line,
                formatCheckedAt(alert.checkedAtMillis)
            )

            binding.buttonGrantWifiPermission.visibility =
                if (alert.requiresLocationPermission) View.VISIBLE else View.GONE
            binding.buttonToggleTrustedWifi.visibility =
                if (alert.canToggleTrust) View.VISIBLE else View.GONE
            if (alert.canToggleTrust) {
                binding.buttonToggleTrustedWifi.text = getString(
                    if (alert.isTrustedNetwork) {
                        R.string.wifi_trust_remove_button
                    } else {
                        R.string.wifi_trust_add_button
                    }
                )
            }

            renderTrustedNetworks(vpnViewModel.trustedWifiNetworks.value.orEmpty(), alert)
        }

        vpnViewModel.wifiAdvancedChecks.observe(viewLifecycleOwner) { checks ->
            latestAdvancedChecks = checks
            binding.textViewAdvancedChecksTitle.text = getString(
                R.string.wifi_advanced_checks_title_with_count,
                checks.size
            )
            renderScoreBreakdown(checks)
            renderAdvancedChecks(checks)
        }

        vpnViewModel.nearbyWifiNetworksState.observe(viewLifecycleOwner) { state ->
            binding.textViewAvailableNetworksTitle.text = getString(
                R.string.wifi_available_networks_title,
                state.networks.size
            )
            renderNearbyNetworks(state)
        }

        vpnViewModel.trustedWifiNetworks.observe(viewLifecycleOwner) { trusted ->
            binding.textViewTrustedNetworksTitle.text = getString(
                R.string.wifi_trusted_networks_title,
                trusted.size
            )
            renderTrustedNetworks(trusted, latestWifiAlert)
        }

        vpnViewModel.refreshWifiSecurityAlert()
    }

    override fun onStart() {
        super.onStart()
        vpnViewModel.startWifiMonitoring()
    }

    override fun onStop() {
        val keepBackgroundMonitoring = vpnViewModel.backgroundScanState.value?.enabled == true
        if (!keepBackgroundMonitoring) {
            vpnViewModel.stopWifiMonitoring()
        }
        super.onStop()
    }

    private fun renderSafetyAssessment(assessment: WifiSafetyAssessment) {
        latestSafetyAssessment = assessment
        val (titleRes, iconRes, iconDescRes) = when (assessment.level) {
            WifiNetworkRiskLevel.SAFE -> Triple(
                R.string.wifi_security_title_secure,
                R.drawable.ic_wifi_status_safe,
                R.string.wifi_status_icon_safe_desc
            )
            WifiNetworkRiskLevel.WARNING -> Triple(
                R.string.wifi_security_title_info,
                R.drawable.ic_wifi_status_warning,
                R.string.wifi_status_icon_warning_desc
            )
            WifiNetworkRiskLevel.DANGER -> Triple(
                R.string.wifi_security_title_warning,
                R.drawable.ic_wifi_status_danger,
                R.string.wifi_status_icon_danger_desc
            )
        }

        binding.imageViewWifiStatusIcon.setImageResource(iconRes)
        binding.imageViewWifiStatusIcon.contentDescription = getString(iconDescRes)
        binding.textViewWifiSafetyTitle.text = getString(titleRes)
        binding.textViewWifiSafetyScore.text = getString(
            R.string.wifi_safety_score_format,
            assessment.score
        )
        binding.textViewWifiSafetyBody.text = assessment.summary
        binding.textViewWifiRecommendation.text = getString(assessment.recommendationResId)
    }

    private fun renderAutoVpnPolicy(policy: AutoVpnPolicy) {
        isBindingPolicyControls = true
        binding.switchAutoVpnProtect.isChecked = policy != AutoVpnPolicy.OFF
        binding.switchAutoVpnDisconnectSafe.isEnabled = policy != AutoVpnPolicy.OFF
        binding.switchAutoVpnDisconnectSafe.isChecked =
            policy == AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE
        binding.textViewAutoVpnPolicyStatus.text = getString(
            when (policy) {
                AutoVpnPolicy.OFF -> R.string.wifi_auto_vpn_policy_off
                AutoVpnPolicy.CONNECT_ON_RISK -> R.string.wifi_auto_vpn_policy_connect_on_risk
                AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE ->
                    R.string.wifi_auto_vpn_policy_connect_and_disconnect
            }
        )
        isBindingPolicyControls = false
    }

    private fun renderBackgroundScanState(state: WifiBackgroundScanState) {
        isBindingPolicyControls = true
        binding.switchBackgroundScan.isChecked = state.enabled
        isBindingPolicyControls = false
        if (!state.enabled) {
            binding.textViewBackgroundScanStatus.text =
                getString(R.string.wifi_background_scan_status_default)
            return
        }
        val lastRun = state.lastRunMillis?.let { formatCheckedAt(it) }
            ?: getString(R.string.wifi_background_scan_never)
        binding.textViewBackgroundScanStatus.text = getString(
            R.string.wifi_background_scan_status_format,
            state.intervalMinutes,
            lastRun
        )
    }

    private fun handleRiskTransitionAlert(alert: WifiRiskTransitionAlert?) {
        if (alert == null || alert.id == lastHandledRiskAlertId) return
        lastHandledRiskAlertId = alert.id
        val message = getString(
            if (alert.worsened) {
                R.string.wifi_risk_transition_worsened
            } else {
                R.string.wifi_risk_transition_improved
            },
            getString(riskLevelLabelRes(alert.toLevel)),
            alert.score
        )
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        vpnViewModel.clearRiskTransitionAlert()
    }

    private fun showScoreDetailsDialog() {
        val assessment = latestSafetyAssessment ?: return
        val checks = latestAdvancedChecks
        val detail = StringBuilder().apply {
            append(getString(R.string.wifi_safety_score_format, assessment.score))
            append('\n')
            append(getString(R.string.wifi_score_breakdown_total_format, checks.sumOf { it.penalty }))
            if (checks.isEmpty()) {
                append("\n\n")
                append(getString(R.string.wifi_advanced_checks_none))
            } else {
                checks.sortedByDescending { it.penalty }.forEach { check ->
                    append("\n\n")
                    append("\u2022 ")
                    append(getString(check.titleResId))
                    append(" [")
                    append(
                        getString(
                            when (check.level) {
                                WifiNetworkRiskLevel.SAFE -> R.string.wifi_check_status_safe
                                WifiNetworkRiskLevel.WARNING -> R.string.wifi_check_status_warning
                                WifiNetworkRiskLevel.DANGER -> R.string.wifi_check_status_danger
                            }
                        )
                    )
                    append("]")
                    if (check.penalty > 0) {
                        append(" -")
                        append(check.penalty)
                    }
                    append('\n')
                    append(check.detail)
                }
            }
        }.toString()

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.wifi_score_details_title)
            .setMessage(detail)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun renderAdvancedChecks(checks: List<WifiAdvancedCheckItem>) {
        val container = binding.layoutAdvancedChecksContainer
        container.removeAllViews()
        if (checks.isEmpty()) {
            addSectionMessage(container, getString(R.string.wifi_advanced_checks_none))
            return
        }

        checks.forEach { check ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 6, 0, 6)
            }
            val statusRes = when (check.level) {
                WifiNetworkRiskLevel.SAFE -> R.string.wifi_check_status_safe
                WifiNetworkRiskLevel.WARNING -> R.string.wifi_check_status_warning
                WifiNetworkRiskLevel.DANGER -> R.string.wifi_check_status_danger
            }
            val title = TextView(requireContext()).apply {
                text = getString(
                    R.string.wifi_advanced_check_title_format,
                    getString(statusRes),
                    getString(check.titleResId)
                )
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            val detail = TextView(requireContext()).apply {
                text = check.detail
                textSize = 12f
            }
            row.addView(title)
            row.addView(detail)
            container.addView(row)
        }
    }

    private fun renderScoreBreakdown(checks: List<WifiAdvancedCheckItem>) {
        val penalties = checks
            .filter { it.penalty > 0 }
            .sortedWith(compareByDescending<WifiAdvancedCheckItem> { it.penalty }.thenBy { it.key })
        val chipsContainer = binding.layoutScoreBreakdownChips
        chipsContainer.removeAllViews()

        if (penalties.isEmpty()) {
            binding.scrollViewScoreBreakdown.visibility = View.GONE
            binding.textViewScoreBreakdownEmpty.visibility = View.VISIBLE
            return
        }

        binding.scrollViewScoreBreakdown.visibility = View.VISIBLE
        binding.textViewScoreBreakdownEmpty.visibility = View.GONE

        val totalPenalty = penalties.sumOf { it.penalty }
        val totalLevel = when {
            totalPenalty >= 30 -> WifiNetworkRiskLevel.DANGER
            totalPenalty >= 12 -> WifiNetworkRiskLevel.WARNING
            else -> WifiNetworkRiskLevel.SAFE
        }
        addScoreBreakdownChip(
            text = getString(R.string.wifi_score_breakdown_total_format, totalPenalty),
            level = totalLevel
        )

        penalties.forEach { check ->
            addScoreBreakdownChip(
                text = getString(
                    R.string.wifi_score_breakdown_chip_format,
                    getString(check.titleResId),
                    check.penalty
                ),
                level = check.level
            )
        }
    }

    private fun addScoreBreakdownChip(text: String, level: WifiNetworkRiskLevel) {
        val chip = TextView(requireContext()).apply {
            this.text = text
            textSize = 12f
            setPadding(20, 10, 20, 10)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        chip.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = 10
        }
        val (backgroundRes, textColorRes) = when (level) {
            WifiNetworkRiskLevel.SAFE -> Pair(
                R.drawable.bg_wifi_risk_safe,
                R.color.wifi_risk_safe_text
            )

            WifiNetworkRiskLevel.WARNING -> Pair(
                R.drawable.bg_wifi_risk_warning,
                R.color.wifi_risk_warning_text
            )

            WifiNetworkRiskLevel.DANGER -> Pair(
                R.drawable.bg_wifi_risk_danger,
                R.color.wifi_risk_danger_text
            )
        }
        chip.setBackgroundResource(backgroundRes)
        chip.setTextColor(ContextCompat.getColor(requireContext(), textColorRes))
        binding.layoutScoreBreakdownChips.addView(chip)
    }

    private fun renderNearbyNetworks(state: WifiNearbyNetworksState) {
        val container = binding.layoutAvailableNetworksContainer
        container.removeAllViews()
        when {
            state.messageResId != null -> addSectionMessage(container, getString(state.messageResId))
            state.networks.isEmpty() -> addSectionMessage(container, getString(R.string.wifi_nearby_none))
            else -> {
                state.networks.forEach { network ->
                    val row = layoutInflater.inflate(
                        R.layout.item_wifi_available_network,
                        container,
                        false
                    )
                    val icon = row.findViewById<ImageView>(R.id.imageViewAvailableNetworkIcon)
                    val title = row.findViewById<TextView>(R.id.textViewAvailableNetworkTitle)
                    val currentBadge = row.findViewById<TextView>(R.id.textViewAvailableNetworkCurrentBadge)
                    val body = row.findViewById<TextView>(R.id.textViewAvailableNetworkBody)
                    val identity = row.findViewById<TextView>(R.id.textViewAvailableNetworkIdentity)
                    val internet = row.findViewById<TextView>(R.id.textViewAvailableNetworkInternet)
                    val security = row.findViewById<TextView>(R.id.textViewAvailableNetworkSecurity)
                    val status = row.findViewById<TextView>(R.id.textViewAvailableNetworkStatus)
                    val trust = row.findViewById<TextView>(R.id.textViewAvailableNetworkTrust)
                    val trustButton = row.findViewById<Button>(R.id.buttonToggleAvailableNetworkTrust)

                    val (iconRes, iconDescRes, titleRes) = riskHeaderUi(network.riskLevel)

                    icon.setImageResource(iconRes)
                    icon.contentDescription = getString(iconDescRes)
                    title.text = getString(titleRes)
                    currentBadge.visibility = if (network.isCurrent) View.VISIBLE else View.GONE
                    if (network.isCurrent) {
                        styleCurrentBadge(currentBadge, network.riskLevel)
                    }
                    body.text = getString(reasonMessageRes(network.reason))
                    identity.text = getString(
                        R.string.wifi_network_line,
                        getNetworkIdentityText(network)
                    )
                    internet.text = getString(
                        R.string.wifi_internet_access_line,
                        getString(
                            internetAccessLabelRes(
                                hasInternetAccess = network.hasInternetAccess,
                                notApplicable = !network.isCurrent
                            )
                        )
                    )
                    security.text = getString(
                        R.string.wifi_security_type_line,
                        getString(securityTypeLabelRes(network.securityType))
                    )
                    status.text = getString(
                        R.string.wifi_security_reason_line,
                        getString(reasonLabelRes(network.reason))
                    )
                    trust.text = getString(
                        R.string.wifi_trust_status_line,
                        getString(
                            if (network.isTrusted) {
                                R.string.wifi_trust_status_trusted
                            } else {
                                R.string.wifi_trust_status_not_trusted
                            }
                        )
                    )
                    trustButton.text = getString(
                        if (network.isTrusted) {
                            R.string.wifi_trust_remove_short
                        } else {
                            R.string.wifi_trust_add_short
                        }
                    )
                    trustButton.setOnClickListener {
                        val messageResId = vpnViewModel.toggleTrustedNetwork(
                            ssid = network.ssid,
                            bssid = network.bssid,
                            securityType = network.securityType,
                            currentlyTrusted = network.isTrusted
                        )
                        Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT)
                            .show()
                    }
                    container.addView(row)
                }
            }
        }
    }

    private fun renderTrustedNetworks(trusted: List<TrustedWifiProfile>, currentAlert: WifiSecurityAlert?) {
        val container = binding.layoutTrustedNetworksContainer
        container.removeAllViews()
        if (trusted.isEmpty()) {
            addSectionMessage(container, getString(R.string.wifi_trust_list_empty))
            return
        }

        trusted.sortedBy { it.ssid.lowercase() }.forEach { profile ->
            val row = layoutInflater.inflate(
                R.layout.item_wifi_trusted_network,
                container,
                false
            )
            val icon = row.findViewById<ImageView>(R.id.imageViewTrustedNetworkIcon)
            val title = row.findViewById<TextView>(R.id.textViewTrustedNetworkTitle)
            val currentBadge = row.findViewById<TextView>(R.id.textViewTrustedNetworkCurrentBadge)
            val body = row.findViewById<TextView>(R.id.textViewTrustedNetworkBody)
            val identity = row.findViewById<TextView>(R.id.textViewTrustedNetworkIdentity)
            val internet = row.findViewById<TextView>(R.id.textViewTrustedNetworkInternet)
            val security = row.findViewById<TextView>(R.id.textViewTrustedNetworkSecurity)
            val status = row.findViewById<TextView>(R.id.textViewTrustedNetworkStatus)
            val trust = row.findViewById<TextView>(R.id.textViewTrustedNetworkTrust)
            val removeButton = row.findViewById<Button>(R.id.buttonRemoveTrustedNetwork)

            val isCurrent = currentAlert != null && isCurrentTrustedProfile(profile, currentAlert)
            val reason = if (isCurrent && currentAlert != null) {
                currentAlert.reason
            } else {
                reasonForSecurityType(profile.securityType)
            }
            val riskLevel = riskLevelFromReason(reason)
            val (iconRes, iconDescRes, titleRes) = riskHeaderUi(riskLevel)
            val displayedSecurityType = if (isCurrent && currentAlert?.securityType != null) {
                currentAlert.securityType
            } else {
                profile.securityType
            }

            icon.setImageResource(iconRes)
            icon.contentDescription = getString(iconDescRes)
            title.text = getString(titleRes)
            currentBadge.visibility = if (isCurrent) View.VISIBLE else View.GONE
            if (isCurrent) {
                styleCurrentBadge(currentBadge, riskLevel)
            }
            body.text = getString(reasonMessageRes(reason))
            identity.text = getString(
                R.string.wifi_network_line,
                getString(
                    R.string.wifi_network_identity_with_bssid,
                    profile.ssid,
                    profile.bssid ?: getString(R.string.wifi_network_unknown)
                )
            )
            internet.text = getString(
                R.string.wifi_internet_access_line,
                getString(
                    internetAccessLabelRes(
                        hasInternetAccess = if (isCurrent) currentAlert?.hasInternetAccess else null,
                        notApplicable = !isCurrent
                    )
                )
            )
            security.text = getString(
                R.string.wifi_security_type_line,
                getString(securityTypeLabelRes(displayedSecurityType))
            )
            status.text = getString(
                R.string.wifi_security_reason_line,
                getString(reasonLabelRes(reason))
            )
            trust.text = getString(
                R.string.wifi_trust_status_line,
                getString(R.string.wifi_trust_status_trusted)
            )
            removeButton.setOnClickListener {
                val messageResId = vpnViewModel.removeTrustedNetwork(profile.ssid)
                Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT).show()
            }
            container.addView(row)
        }
    }

    private fun updateAdvancedChecksSectionVisibility() {
        binding.layoutAdvancedChecksContainer.visibility =
            if (isAdvancedChecksExpanded) View.VISIBLE else View.GONE
        binding.textViewAdvancedChecksToggle.text = getString(
            if (isAdvancedChecksExpanded) {
                R.string.wifi_networks_hide
            } else {
                R.string.wifi_networks_show
            }
        )
    }

    private fun updateCurrentWifiDetailsVisibility() {
        binding.layoutCurrentWifiDetailsContainer.visibility =
            if (isCurrentWifiDetailsExpanded) View.VISIBLE else View.GONE
        binding.textViewCurrentWifiToggle.text = getString(
            if (isCurrentWifiDetailsExpanded) {
                R.string.wifi_current_hide_details
            } else {
                R.string.wifi_current_show_details
            }
        )
    }

    private fun updateNearbyNetworksSectionVisibility() {
        binding.layoutAvailableNetworksContainer.visibility =
            if (isNearbyNetworksExpanded) View.VISIBLE else View.GONE
        binding.textViewAvailableNetworksToggle.text = getString(
            if (isNearbyNetworksExpanded) {
                R.string.wifi_networks_hide
            } else {
                R.string.wifi_networks_show
            }
        )
    }

    private fun updateTrustedNetworksSectionVisibility() {
        binding.layoutTrustedNetworksContainer.visibility =
            if (isTrustedNetworksExpanded) View.VISIBLE else View.GONE
        binding.textViewTrustedNetworksToggle.text = getString(
            if (isTrustedNetworksExpanded) {
                R.string.wifi_networks_hide
            } else {
                R.string.wifi_networks_show
            }
        )
    }

    private fun hasLocationPermission(): Boolean {
        val context = requireContext()
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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
            WifiAlertReason.LOCATION_SERVICES_DISABLED -> R.string.wifi_reason_location_services_disabled
            WifiAlertReason.OPEN_OR_WEP -> R.string.wifi_reason_open_or_wep
            WifiAlertReason.UNKNOWN_SECURITY -> R.string.wifi_reason_unknown_security
            WifiAlertReason.WEAK_LEGACY_CIPHER -> R.string.wifi_reason_weak_legacy_cipher
            WifiAlertReason.WPS_ENABLED -> R.string.wifi_reason_wps_enabled
            WifiAlertReason.TRUSTED_BSSID_MISMATCH -> R.string.wifi_reason_trusted_bssid_mismatch
            WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE -> R.string.wifi_reason_trusted_security_downgrade
            WifiAlertReason.SECURE -> R.string.wifi_reason_secure
        }
    }

    private fun reasonMessageRes(reason: WifiAlertReason): Int {
        return when (reason) {
            WifiAlertReason.UNAVAILABLE -> R.string.wifi_security_unavailable
            WifiAlertReason.NO_NETWORK -> R.string.wifi_security_no_network
            WifiAlertReason.NOT_WIFI -> R.string.wifi_security_not_wifi
            WifiAlertReason.CAPTIVE_PORTAL -> R.string.wifi_security_captive_portal
            WifiAlertReason.UNVALIDATED -> R.string.wifi_security_unvalidated
            WifiAlertReason.LEGACY_NO_SECURITY_TYPE -> R.string.wifi_security_legacy
            WifiAlertReason.MISSING_PERMISSION -> R.string.wifi_security_missing_permission
            WifiAlertReason.LOCATION_SERVICES_DISABLED -> R.string.wifi_security_location_services_disabled
            WifiAlertReason.OPEN_OR_WEP -> R.string.wifi_security_open_or_wep
            WifiAlertReason.UNKNOWN_SECURITY -> R.string.wifi_security_connected_unknown
            WifiAlertReason.WEAK_LEGACY_CIPHER -> R.string.wifi_security_weak_legacy_cipher
            WifiAlertReason.WPS_ENABLED -> R.string.wifi_security_wps_enabled
            WifiAlertReason.TRUSTED_BSSID_MISMATCH -> R.string.wifi_security_trusted_bssid_mismatch
            WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE -> R.string.wifi_security_trusted_security_downgrade
            WifiAlertReason.SECURE -> R.string.wifi_security_secure
        }
    }

    private fun riskLevelFromReason(reason: WifiAlertReason): WifiNetworkRiskLevel {
        return when (reason) {
            WifiAlertReason.OPEN_OR_WEP,
            WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE -> WifiNetworkRiskLevel.DANGER

            WifiAlertReason.CAPTIVE_PORTAL,
            WifiAlertReason.UNVALIDATED,
            WifiAlertReason.UNKNOWN_SECURITY,
            WifiAlertReason.WEAK_LEGACY_CIPHER,
            WifiAlertReason.WPS_ENABLED,
            WifiAlertReason.TRUSTED_BSSID_MISMATCH,
            WifiAlertReason.MISSING_PERMISSION,
            WifiAlertReason.LOCATION_SERVICES_DISABLED,
            WifiAlertReason.LEGACY_NO_SECURITY_TYPE,
            WifiAlertReason.NOT_WIFI,
            WifiAlertReason.NO_NETWORK,
            WifiAlertReason.UNAVAILABLE -> WifiNetworkRiskLevel.WARNING

            WifiAlertReason.SECURE -> WifiNetworkRiskLevel.SAFE
        }
    }

    private fun reasonForSecurityType(securityType: WifiSecurityType): WifiAlertReason {
        return when (securityType) {
            WifiSecurityType.OPEN,
            WifiSecurityType.WEP -> WifiAlertReason.OPEN_OR_WEP

            WifiSecurityType.UNKNOWN -> WifiAlertReason.UNKNOWN_SECURITY
            WifiSecurityType.SECURE -> WifiAlertReason.SECURE
        }
    }

    private fun riskHeaderUi(level: WifiNetworkRiskLevel): Triple<Int, Int, Int> {
        return when (level) {
            WifiNetworkRiskLevel.SAFE -> Triple(
                R.drawable.ic_wifi_status_safe,
                R.string.wifi_status_icon_safe_desc,
                R.string.wifi_security_title_secure
            )

            WifiNetworkRiskLevel.WARNING -> Triple(
                R.drawable.ic_wifi_status_warning,
                R.string.wifi_status_icon_warning_desc,
                R.string.wifi_security_title_info
            )

            WifiNetworkRiskLevel.DANGER -> Triple(
                R.drawable.ic_wifi_status_danger,
                R.string.wifi_status_icon_danger_desc,
                R.string.wifi_security_title_warning
            )
        }
    }

    private fun styleCurrentBadge(badge: TextView, level: WifiNetworkRiskLevel) {
        val (backgroundRes, textColorRes) = when (level) {
            WifiNetworkRiskLevel.SAFE -> Pair(
                R.drawable.bg_wifi_risk_safe,
                R.color.wifi_risk_safe_text
            )

            WifiNetworkRiskLevel.WARNING -> Pair(
                R.drawable.bg_wifi_risk_warning,
                R.color.wifi_risk_warning_text
            )

            WifiNetworkRiskLevel.DANGER -> Pair(
                R.drawable.bg_wifi_risk_danger,
                R.color.wifi_risk_danger_text
            )
        }
        badge.setBackgroundResource(backgroundRes)
        badge.setTextColor(ContextCompat.getColor(requireContext(), textColorRes))
    }

    private fun isCurrentTrustedProfile(profile: TrustedWifiProfile, currentAlert: WifiSecurityAlert): Boolean {
        val currentSsid = currentAlert.ssid
        return currentSsid != null && profile.ssid.equals(currentSsid, ignoreCase = true)
    }

    private fun getNetworkIdentityText(alert: WifiSecurityAlert): String {
        return when {
            alert.ssid != null && alert.bssid != null -> getString(
                R.string.wifi_network_identity_with_bssid,
                alert.ssid,
                alert.bssid
            )

            alert.ssid != null -> getString(R.string.wifi_network_identity_ssid_only, alert.ssid)
            alert.bssid != null -> getString(R.string.wifi_network_identity_bssid_only, alert.bssid)
            else -> getString(R.string.wifi_network_unknown)
        }
    }

    private fun getNetworkIdentityText(network: WifiNearbyNetwork): String {
        return when {
            network.bssid != null -> getString(
                R.string.wifi_network_identity_with_bssid,
                network.ssid,
                network.bssid
            )

            else -> getString(R.string.wifi_network_identity_ssid_only, network.ssid)
        }
    }

    private fun internetAccessLabelRes(hasInternetAccess: Boolean?, notApplicable: Boolean): Int {
        return when (hasInternetAccess) {
            true -> R.string.wifi_internet_access_yes
            false -> R.string.wifi_internet_access_no
            null -> if (notApplicable) {
                R.string.wifi_internet_access_not_applicable
            } else {
                R.string.wifi_internet_access_unknown
            }
        }
    }

    private fun addSectionMessage(container: LinearLayout, text: String) {
        val message = TextView(requireContext()).apply {
            setText(text)
            textSize = 13f
            setPadding(0, 8, 0, 8)
        }
        container.addView(message)
    }

    private fun formatCheckedAt(timestampMs: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
            .format(Date(timestampMs))
    }

    private fun riskLevelLabelRes(level: WifiNetworkRiskLevel): Int {
        return when (level) {
            WifiNetworkRiskLevel.SAFE -> R.string.risk_safe
            WifiNetworkRiskLevel.WARNING -> R.string.risk_warning
            WifiNetworkRiskLevel.DANGER -> R.string.risk_risk
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
