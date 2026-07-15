package com.resistine.android.ui.wifi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.resistine.android.BuildConfig
import com.resistine.android.R
import com.resistine.android.databinding.FragmentWifiSecurityBinding
import com.resistine.android.ui.vpn.AutoVpnPolicy
import com.resistine.android.ui.vpn.TrustedWifiProfile
import com.resistine.android.ui.vpn.VpnViewModel
import com.resistine.android.ui.vpn.WifiAdvancedCheckItem
import com.resistine.android.ui.vpn.WifiAlertReason
import com.resistine.android.ui.vpn.WifiNearbyNetwork
import com.resistine.android.ui.vpn.WifiNearbyNetworksState
import com.resistine.android.ui.vpn.WifiNetworkRiskLevel
import com.resistine.android.ui.vpn.WifiRiskTransitionAlert
import com.resistine.android.ui.vpn.WifiSafetyAssessment
import com.resistine.android.ui.vpn.WifiSecurityAlert
import com.resistine.android.ui.vpn.WifiSecurityType
import com.resistine.android.ui.wifi.WifiScanFreshness
import com.resistine.android.ui.wifi.WifiScanRequestStatus
import java.text.DateFormat
import java.util.Date

class WifiSecurityFragment : Fragment() {

    private var _binding: FragmentWifiSecurityBinding? = null
    private val binding get() = _binding!!
    private val vpnViewModel: VpnViewModel by activityViewModels()

    private var isNearbyNetworksExpanded = false
    private var isTrustedNetworksExpanded = false
    private var isCurrentWifiDetailsExpanded = false
    private var latestNearbyNetworksState = WifiNearbyNetworksState()
    private var latestTrustedNetworks: List<TrustedWifiProfile> = emptyList()
    private var latestWifiAlert: WifiSecurityAlert? = null
    private var latestSafetyAssessment: WifiSafetyAssessment? = null
    private var latestAdvancedChecks: List<WifiAdvancedCheckItem> = emptyList()
    private var lastHandledRiskAlertId: Long = -1L
    private var isBindingPolicyControls = false
    private var latestAutoVpnPolicy: AutoVpnPolicy = AutoVpnPolicy.OFF
    private var latestAutoProtectUnknownWifi: Boolean = false
    private var hasAutoPromptedLocationServicesResolution = false
    private var showDebugDetails = false

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

    private val locationServicesResolutionLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                hasAutoPromptedLocationServicesResolution = false
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

        binding.buttonRefreshWifiQuick.setOnClickListener {
            vpnViewModel.refreshWifiSecurityAlert()
        }

        binding.buttonGrantWifiPermission.setOnClickListener {
            requestNearbyWifiAccess()
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
        binding.switchAutoVpnProtectUnknown.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingPolicyControls) return@setOnCheckedChangeListener
            vpnViewModel.setAutoProtectUnknownWifi(isChecked)
        }
        binding.switchAutoVpnDisconnectSafe.setOnCheckedChangeListener { _, isChecked ->
            if (isBindingPolicyControls) return@setOnCheckedChangeListener
            vpnViewModel.setAutoVpnDisconnectOnSafe(isChecked)
        }
        binding.buttonAutoVpnDisconnectNow.setOnClickListener {
            vpnViewModel.disconnectVpnIfConnected()
        }
        binding.layoutAvailableNetworksHeader.setOnClickListener {
            isNearbyNetworksExpanded = !isNearbyNetworksExpanded
            updateNearbyNetworksSectionVisibility()
            if (isNearbyNetworksExpanded) renderNearbyNetworks(latestNearbyNetworksState)
        }
        binding.layoutTrustedNetworksHeader.setOnClickListener {
            isTrustedNetworksExpanded = !isTrustedNetworksExpanded
            updateTrustedNetworksSectionVisibility()
            if (isTrustedNetworksExpanded) {
                renderTrustedNetworks(latestTrustedNetworks, latestWifiAlert)
            }
        }

        updateNearbyNetworksSectionVisibility()
        updateTrustedNetworksSectionVisibility()
        updateCurrentWifiDetailsVisibility()
        bindDebugDetailsControls()

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
            latestAutoVpnPolicy = policy
            renderAutoVpnPolicy()
        }

        vpnViewModel.autoProtectUnknownWifi.observe(viewLifecycleOwner) { enabled ->
            latestAutoProtectUnknownWifi = enabled
            renderAutoVpnPolicy()
        }

        vpnViewModel.isVpnConnectedLiveData.observe(viewLifecycleOwner) { connected ->
            binding.buttonAutoVpnDisconnectNow.visibility = if (connected) View.VISIBLE else View.GONE
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
            binding.textViewWifiSecurityProfile.text = getString(
                R.string.wifi_security_profile_line,
                getString(securityProfileLabelRes(alert.securityProfile))
            )
            binding.textViewWifiPmf.text = getString(
                R.string.wifi_pmf_line,
                getString(pmfLabelRes(alert.securityProfile?.pmfState ?: WifiPmfState.UNKNOWN))
            )
            binding.textViewWifiMatchConfidence.text = getString(
                R.string.wifi_match_confidence_line,
                getString(matchConfidenceLabelRes(alert.matchConfidence))
            )
            binding.textViewWifiScanFreshness.text = scanFreshnessText(alert)
            binding.textViewWifiSecurityReason.text = getString(
                if (alert.isTrustedNetwork && shouldHighlightTrustStatus(alert.trustStatus)) {
                    R.string.wifi_trust_baseline_status_line
                } else {
                    R.string.wifi_security_reason_line
                },
                getString(
                    if (alert.isTrustedNetwork && shouldHighlightTrustStatus(alert.trustStatus)) {
                        trustStatusLabelRes(alert.trustStatus)
                    } else {
                        reasonLabelRes(alert.reason)
                    }
                )
            )
            binding.textViewWifiTrustStatus.text = if (alert.isTrustedNetwork) {
                getString(
                    R.string.wifi_trust_status_with_baseline,
                    getString(R.string.wifi_trust_status_trusted),
                    getString(trustStatusLabelRes(alert.trustStatus))
                )
            } else {
                getString(
                    R.string.wifi_trust_status_line,
                    getString(R.string.wifi_trust_status_not_trusted)
                )
            }
            binding.textViewWifiTrustDetail.text = if (alert.isTrustedNetwork) {
                alert.trustDetail ?: trustedStatusSummary(alert.trustStatus)
            } else if (alert.ssid != null && !alert.canToggleTrust && alert.reason != WifiAlertReason.NOT_WIFI) {
                getString(R.string.wifi_trust_detail_identity_required)
            } else {
                getString(R.string.wifi_trust_detail_default)
            }
            binding.textViewWifiLastChecked.text = getString(
                R.string.wifi_security_last_checked_line,
                formatCheckedAt(alert.checkedAtMillis)
            )
            renderCurrentWifiSummaryChips(alert)

            binding.buttonGrantWifiPermission.visibility =
                if (alert.requiresLocationPermission || alert.reason == WifiAlertReason.LOCATION_SERVICES_DISABLED) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            binding.buttonGrantWifiPermission.text = getString(
                if (alert.reason == WifiAlertReason.LOCATION_SERVICES_DISABLED && hasLocationPermission()) {
                    R.string.wifi_security_enable_location_services
                } else {
                    R.string.wifi_security_grant_permission
                }
            )
            binding.buttonToggleTrustedWifi.visibility =
                if (alert.canToggleTrust) View.VISIBLE else View.GONE
            if (alert.canToggleTrust) {
                binding.buttonToggleTrustedWifi.text = getString(
                    if (alert.isTrustedNetwork && alert.hasPendingTrustApproval) {
                        R.string.wifi_trust_approve_current_button
                    } else if (alert.isTrustedNetwork) {
                        R.string.wifi_trust_remove_button
                    } else {
                        R.string.wifi_trust_add_button
                    }
                )
            }

            updateCurrentTechnicalDetailsVisibility()
            maybePromptForLocationServices(alert)
            renderTrustedNetworks(vpnViewModel.trustedWifiNetworks.value.orEmpty(), alert)
        }

        vpnViewModel.wifiAdvancedChecks.observe(viewLifecycleOwner) { checks ->
            latestAdvancedChecks = checks
        }

        vpnViewModel.nearbyWifiNetworksState.observe(viewLifecycleOwner) { state ->
            latestNearbyNetworksState = state
            binding.textViewAvailableNetworksTitle.text = getString(
                R.string.wifi_available_networks_title,
                state.networks.size
            )
            if (isNearbyNetworksExpanded) renderNearbyNetworks(state)
        }

        vpnViewModel.trustedWifiNetworks.observe(viewLifecycleOwner) { trusted ->
            latestTrustedNetworks = trusted
            binding.textViewTrustedNetworksTitle.text = getString(
                R.string.wifi_trusted_networks_title,
                trusted.size
            )
            if (isTrustedNetworksExpanded) renderTrustedNetworks(trusted, latestWifiAlert)
        }

        vpnViewModel.isWifiRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.wifiRefreshProgress.visibility = if (refreshing) View.VISIBLE else View.GONE
            binding.buttonRefreshWifiQuick.isEnabled = !refreshing
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

    private fun renderSafetyAssessment(assessment: WifiSafetyAssessment) {
        latestSafetyAssessment = assessment
        val (titleRes, iconRes, iconDescRes) = when {
            assessment.level == WifiNetworkRiskLevel.DANGER -> Triple(
                R.string.wifi_security_title_warning,
                R.drawable.ic_wifi_status_danger,
                R.string.wifi_status_icon_danger_desc
            )

            assessment.level == WifiNetworkRiskLevel.WARNING || assessment.isLimitedData -> Triple(
                R.string.wifi_security_title_info,
                R.drawable.ic_wifi_status_warning,
                R.string.wifi_status_icon_warning_desc
            )

            else -> Triple(
                R.string.wifi_security_title_secure,
                R.drawable.ic_wifi_status_safe,
                R.string.wifi_status_icon_safe_desc
            )
        }

        binding.imageViewWifiStatusIcon.setImageResource(iconRes)
        binding.imageViewWifiStatusIcon.contentDescription = getString(iconDescRes)
        binding.textViewWifiSafetyTitle.text = getString(titleRes)
        binding.textViewWifiSafetyScore.text = if (assessment.isScoreAvailable) {
            getString(R.string.wifi_safety_score_format, assessment.score)
        } else {
            getString(R.string.wifi_safety_score_unavailable)
        }
        binding.textViewWifiSafetyBody.text = assessment.summary
        binding.textViewWifiRecommendation.text = getString(assessment.recommendationResId)
        renderScoreBreakdown(assessment.dimensions)
        latestWifiAlert?.let { renderCurrentWifiSummaryChips(it) }
    }

    private fun renderAutoVpnPolicy() {
        val policy = latestAutoVpnPolicy
        isBindingPolicyControls = true
        binding.switchAutoVpnProtect.isChecked = policy != AutoVpnPolicy.OFF
        binding.switchAutoVpnProtectUnknown.isEnabled = policy != AutoVpnPolicy.OFF
        binding.switchAutoVpnProtectUnknown.isChecked = latestAutoProtectUnknownWifi
        binding.switchAutoVpnDisconnectSafe.isEnabled = policy != AutoVpnPolicy.OFF
        binding.switchAutoVpnDisconnectSafe.isChecked =
            policy == AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE
        val baseStatus = getString(
            when (policy) {
                AutoVpnPolicy.OFF -> R.string.wifi_auto_vpn_policy_off
                AutoVpnPolicy.CONNECT_ON_RISK -> R.string.wifi_auto_vpn_policy_connect_on_risk
                AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE ->
                    R.string.wifi_auto_vpn_policy_connect_and_disconnect
            }
        )
        val unknownStatus = getString(
            if (latestAutoProtectUnknownWifi) {
                R.string.wifi_auto_vpn_unknown_status_on
            } else {
                R.string.wifi_auto_vpn_unknown_status_off
            }
        )
        binding.textViewAutoVpnPolicyStatus.text = if (policy == AutoVpnPolicy.OFF) {
            baseStatus
        } else {
            "$baseStatus\n$unknownStatus"
        }
        isBindingPolicyControls = false
    }

    private fun handleRiskTransitionAlert(alert: WifiRiskTransitionAlert?) {
        if (alert == null || alert.id == lastHandledRiskAlertId) return
        lastHandledRiskAlertId = alert.id
        if (!alert.worsened) {
            vpnViewModel.clearRiskTransitionAlert()
            return
        }
        val message = getString(
            R.string.wifi_risk_transition_worsened,
            getString(riskLevelLabelRes(alert.toLevel)),
            alert.score
        )
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        vpnViewModel.clearRiskTransitionAlert()
    }

    private fun maybePromptForLocationServices(alert: WifiSecurityAlert) {
        if (alert.reason != WifiAlertReason.LOCATION_SERVICES_DISABLED || !hasLocationPermission()) {
            hasAutoPromptedLocationServicesResolution = false
            return
        }
        if (hasAutoPromptedLocationServicesResolution) {
            return
        }
        hasAutoPromptedLocationServicesResolution = true
        requestLocationServicesResolution(userInitiated = false)
    }

    private fun requestLocationServicesResolution(userInitiated: Boolean) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        ).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                hasAutoPromptedLocationServicesResolution = false
                vpnViewModel.refreshWifiSecurityAlert()
            }
            .addOnFailureListener { exception ->
                val resolvable = exception as? ResolvableApiException
                if (resolvable != null) {
                    val request = IntentSenderRequest.Builder(resolvable.resolution).build()
                    locationServicesResolutionLauncher.launch(request)
                } else {
                    if (userInitiated) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.wifi_security_location_services_resolution_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }
    }

    private fun bindDebugDetailsControls() {
        showDebugDetails = BuildConfig.DEBUG && loadDebugDetailsEnabled()
        binding.switchWifiDebugDetails.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        binding.textViewWifiDebugDetailsHint.visibility = if (BuildConfig.DEBUG) View.VISIBLE else View.GONE
        binding.switchWifiDebugDetails.isChecked = showDebugDetails
        binding.switchWifiDebugDetails.setOnCheckedChangeListener { _, isChecked ->
            showDebugDetails = isChecked
            saveDebugDetailsEnabled(isChecked)
            updateCurrentTechnicalDetailsVisibility()
            latestWifiAlert?.let { renderCurrentWifiSummaryChips(it) }
            vpnViewModel.nearbyWifiNetworksState.value?.let { renderNearbyNetworks(it) }
            renderTrustedNetworks(vpnViewModel.trustedWifiNetworks.value.orEmpty(), latestWifiAlert)
        }
        updateCurrentTechnicalDetailsVisibility()
    }

    private fun updateCurrentTechnicalDetailsVisibility() {
        binding.layoutCurrentWifiTechnicalContainer.visibility =
            if (BuildConfig.DEBUG && showDebugDetails) View.VISIBLE else View.GONE
    }

    private fun renderCurrentWifiSummaryChips(alert: WifiSecurityAlert) {
        val chips = mutableListOf<UiChip>()
        chips += securityProfileChip(alert.securityProfile)
        chips += trustStatusChip(
            isTrusted = alert.isTrustedNetwork,
            trustStatus = alert.trustStatus
        )
        chips += internetChip(
            hasInternetAccess = alert.hasInternetAccess,
            notApplicable = !alert.isOnWifi
        )
        val pmfState = alert.securityProfile?.pmfState
        if (pmfState != null && pmfState != WifiPmfState.UNKNOWN && pmfState != WifiPmfState.NOT_APPLICABLE) {
            chips += pmfChip(pmfState)
        }
        if (alert.isOnWifi && alert.matchConfidence != WifiMatchConfidence.VERIFIED_BSSID) {
            chips += matchConfidenceChip(alert.matchConfidence)
        }
        chips += scanFreshnessChip(alert)
        if (latestSafetyAssessment?.isLimitedData == true) {
            chips += limitedVerificationChip()
        }
        // Keep the overview readable; the complete evidence remains available in details.
        renderChipRow(binding.layoutCurrentWifiSummaryChips, chips.take(3))
        binding.scrollViewCurrentWifiSummaryChips.visibility =
            if (chips.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showScoreDetailsDialog() {
        val assessment = latestSafetyAssessment ?: return
        val checks = latestAdvancedChecks
        val detail = StringBuilder().apply {
            append(
                if (assessment.isScoreAvailable) {
                    getString(R.string.wifi_safety_score_format, assessment.score)
                } else {
                    getString(R.string.wifi_safety_score_unavailable)
                }
            )
            append('\n')
            append(getString(R.string.wifi_score_breakdown_total_format, assessment.dimensions.sumOf { it.penalty }))
            val activeDimensions = assessment.dimensions.filter { it.penalty > 0 }
            if (activeDimensions.isNotEmpty()) {
                append("\n\n")
                append(getString(R.string.wifi_score_details_areas))
                activeDimensions.sortedByDescending { it.penalty }.forEach { dimension ->
                    append("\n\n")
                    append("\u2022 ")
                    append(getString(dimension.dimension.titleResId))
                    append(" [")
                    append(
                        getString(
                            when (dimension.level) {
                                WifiNetworkRiskLevel.SAFE -> R.string.wifi_check_status_safe
                                WifiNetworkRiskLevel.WARNING -> R.string.wifi_check_status_warning
                                WifiNetworkRiskLevel.DANGER -> R.string.wifi_check_status_danger
                            }
                        )
                    )
                    append("] -")
                    append(dimension.penalty)
                    append('\n')
                    append(dimension.summary)
                }
            }
            if (checks.isEmpty()) {
                append("\n\n")
                append(getString(R.string.wifi_advanced_checks_none))
            } else {
                append("\n\n")
                append(getString(R.string.wifi_score_details_checks))
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

    private fun renderScoreBreakdown(dimensions: List<WifiScoreDimensionResult>) {
        val penalties = dimensions
            .filter { it.penalty > 0 }
            .sortedWith(compareByDescending<WifiScoreDimensionResult> { it.penalty }.thenBy { it.dimension.name })
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
            level = totalLevel,
            dialogTitle = getString(R.string.wifi_score_details_title),
            dialogMessage = buildTotalBreakdownMessage(dimensions)
        )

        penalties.forEach { dimension ->
            addScoreBreakdownChip(
                text = getString(
                    R.string.wifi_score_breakdown_chip_format,
                    getString(dimension.dimension.titleResId),
                    dimension.penalty
                ),
                level = dimension.level,
                dialogTitle = getString(dimension.dimension.titleResId),
                dialogMessage = buildDimensionBreakdownMessage(dimension)
            )
        }
    }

    private fun addScoreBreakdownChip(
        text: String,
        level: WifiNetworkRiskLevel,
        dialogTitle: String,
        dialogMessage: String
    ) {
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
        chip.setOnLongClickListener {
            showInfoDialog(dialogTitle, dialogMessage)
            true
        }
        binding.layoutScoreBreakdownChips.addView(chip)
    }

    private fun renderNearbyNetworks(state: WifiNearbyNetworksState) {
        val container = binding.layoutAvailableNetworksContainer
        val currentAlert = latestWifiAlert
        container.removeAllViews()
        when {
            state.messageResId != null -> addNearbyEmptyState(
                container = container,
                title = getString(R.string.wifi_nearby_access_needed_title),
                message = getString(state.messageResId),
                showAction = true
            )
            state.networks.isEmpty() -> addNearbyEmptyState(
                container = container,
                title = getString(R.string.wifi_nearby_empty_title),
                message = getString(R.string.wifi_nearby_none),
                showAction = false
            )
            else -> {
                state.networks.forEach { network ->
                    val row = layoutInflater.inflate(
                        R.layout.item_wifi_available_network,
                        container,
                        false
                    )
                    val icon = row.findViewById<ImageView>(R.id.imageViewAvailableNetworkIcon)
                    val title = row.findViewById<TextView>(R.id.textViewAvailableNetworkTitle)
                    val signal = row.findViewById<TextView>(R.id.textViewAvailableNetworkSignal)
                    val currentBadge = row.findViewById<TextView>(R.id.textViewAvailableNetworkCurrentBadge)
                    val body = row.findViewById<TextView>(R.id.textViewAvailableNetworkBody)
                    val identity = row.findViewById<TextView>(R.id.textViewAvailableNetworkIdentity)
                    val internet = row.findViewById<TextView>(R.id.textViewAvailableNetworkInternet)
                    val profile = row.findViewById<TextView>(R.id.textViewAvailableNetworkProfile)
                    val confidence = row.findViewById<TextView>(R.id.textViewAvailableNetworkConfidence)
                    val security = row.findViewById<TextView>(R.id.textViewAvailableNetworkSecurity)
                    val status = row.findViewById<TextView>(R.id.textViewAvailableNetworkStatus)
                    val trust = row.findViewById<TextView>(R.id.textViewAvailableNetworkTrust)
                    val chips = row.findViewById<LinearLayout>(R.id.layoutAvailableNetworkChips)
                    val technicalDetails = row.findViewById<LinearLayout>(R.id.layoutAvailableNetworkTechnicalDetails)
                    val trustButton = row.findViewById<Button>(R.id.buttonToggleAvailableNetworkTrust)

                    val (iconRes, iconDescRes, _) = riskHeaderUi(network.riskLevel)

                    icon.setImageResource(iconRes)
                    icon.contentDescription = getString(iconDescRes)
                    title.text = network.ssid
                    signal.text = network.signalDbm?.let { dbm ->
                        getString(
                            R.string.wifi_signal_format,
                            getString(signalQualityLabelRes(dbm)),
                            dbm
                        )
                    }.orEmpty()
                    signal.visibility = if (network.signalDbm == null) View.GONE else View.VISIBLE
                    currentBadge.visibility = if (network.isCurrent) View.VISIBLE else View.GONE
                    if (network.isCurrent) {
                        styleCurrentBadge(currentBadge, network.riskLevel)
                    }
                    val showTrustState = network.isTrusted && shouldHighlightTrustStatus(network.trustStatus)
                    body.text = when {
                        network.isCurrent && showTrustState -> currentAlert?.trustDetail
                            ?: trustedStatusSummary(network.trustStatus)

                        showTrustState -> trustedStatusSummary(network.trustStatus)
                        else -> getString(riskLevelLabelRes(network.riskLevel)) + " - " +
                            getString(reasonMessageRes(network.reason))
                    }
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
                    profile.text = getString(
                        R.string.wifi_security_profile_line,
                        getString(securityProfileLabelRes(network.securityProfile))
                    )
                    confidence.text = getString(
                        R.string.wifi_match_confidence_line,
                        getString(matchConfidenceLabelRes(network.matchConfidence))
                    )
                    security.text = getString(
                        R.string.wifi_security_type_line,
                        getString(securityTypeLabelRes(network.securityType))
                    )
                    status.text = if (showTrustState) {
                        getString(
                            R.string.wifi_trust_baseline_status_line,
                            getString(trustStatusLabelRes(network.trustStatus))
                        )
                    } else {
                        getString(
                            R.string.wifi_security_reason_line,
                            getString(reasonLabelRes(network.reason))
                        )
                    }
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
                    renderChipRow(
                        chips,
                        nearbyNetworkChips(network)
                    )
                    technicalDetails.visibility =
                        if (BuildConfig.DEBUG && showDebugDetails) View.VISIBLE else View.GONE
                    trustButton.text = getString(
                        if (network.isTrusted) {
                            R.string.wifi_trust_remove_short
                        } else {
                            R.string.wifi_trust_add_short
                        }
                    )
                    trustButton.visibility = if (network.canToggleTrust) View.VISIBLE else View.GONE
                    trustButton.setOnClickListener {
                        val messageResId = vpnViewModel.toggleTrustedNetwork(
                            ssid = network.ssid,
                            bssid = network.bssid,
                            securityProfile = network.securityProfile,
                            frequencyMhz = network.frequencyMhz,
                            isCurrent = network.isCurrent,
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
            val profileLine = row.findViewById<TextView>(R.id.textViewTrustedNetworkProfile)
            val baselineLine = row.findViewById<TextView>(R.id.textViewTrustedNetworkBaseline)
            val security = row.findViewById<TextView>(R.id.textViewTrustedNetworkSecurity)
            val status = row.findViewById<TextView>(R.id.textViewTrustedNetworkStatus)
            val trust = row.findViewById<TextView>(R.id.textViewTrustedNetworkTrust)
            val chips = row.findViewById<LinearLayout>(R.id.layoutTrustedNetworkChips)
            val technicalDetails = row.findViewById<LinearLayout>(R.id.layoutTrustedNetworkTechnicalDetails)
            val removeButton = row.findViewById<Button>(R.id.buttonRemoveTrustedNetwork)

            val isCurrent = currentAlert != null && isCurrentTrustedProfile(profile, currentAlert)
            val trustStatus = if (isCurrent && currentAlert != null) {
                currentAlert.trustStatus
            } else {
                storedTrustStatus(profile)
            }
            val reason = if (isCurrent && currentAlert != null) {
                currentAlert.reason
            } else {
                reasonForSecurityType(profile.securityType)
            }
            val riskLevel = if (isCurrent) {
                maxRiskLevel(
                    riskLevelFromReason(reason),
                    trustRiskLevel(trustStatus)
                )
            } else {
                trustRiskLevel(trustStatus)
            }
            val (iconRes, iconDescRes, _) = riskHeaderUi(riskLevel)
            val displayedSecurityType = if (isCurrent && currentAlert?.securityType != null) {
                currentAlert.securityType
            } else {
                profile.securityType
            }
            val displayedSecurityProfile = if (isCurrent && currentAlert?.securityProfile != null) {
                currentAlert.securityProfile
            } else {
                profile.securityProfile
            }

            icon.setImageResource(iconRes)
            icon.contentDescription = getString(iconDescRes)
            title.text = profile.ssid
            currentBadge.visibility = if (isCurrent) View.VISIBLE else View.GONE
            if (isCurrent) {
                styleCurrentBadge(currentBadge, riskLevel)
            }
            val showTrustState = shouldHighlightTrustStatus(trustStatus)
            body.text = if (isCurrent && showTrustState) {
                trustedStatusSummary(trustStatus)
            } else if (isCurrent) {
                getString(riskLevelLabelRes(riskLevel)) + " - " + getString(reasonMessageRes(reason))
            } else {
                trustedStatusSummary(trustStatus)
            }
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
            profileLine.text = getString(
                R.string.wifi_security_profile_line,
                getString(securityProfileLabelRes(displayedSecurityProfile))
            )
            baselineLine.text = trustedBaselineDetail(profile, isCurrent, currentAlert)
            security.text = getString(
                R.string.wifi_security_type_line,
                getString(securityTypeLabelRes(displayedSecurityType))
            )
            status.text = if (isCurrent && !showTrustState) {
                getString(
                    R.string.wifi_security_reason_line,
                    getString(reasonLabelRes(reason))
                )
            } else {
                getString(
                    R.string.wifi_trust_baseline_status_line,
                    getString(trustStatusLabelRes(trustStatus))
                )
            }
            trust.text = getString(
                R.string.wifi_trust_status_line,
                getString(R.string.wifi_trust_status_trusted)
            )
            renderChipRow(
                chips,
                trustedNetworkChips(
                    displayedSecurityProfile = displayedSecurityProfile,
                    trustStatus = trustStatus,
                    hasInternetAccess = if (isCurrent) currentAlert?.hasInternetAccess else null,
                    isCurrent = isCurrent
                )
            )
            technicalDetails.visibility =
                if (BuildConfig.DEBUG && showDebugDetails) View.VISIBLE else View.GONE
            removeButton.setOnClickListener {
                val messageResId = vpnViewModel.removeTrustedNetwork(profile.ssid)
                Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT).show()
            }
            container.addView(row)
        }
    }

    private fun updateCurrentWifiDetailsVisibility() {
        beginSectionTransition()
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
        beginSectionTransition()
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
        beginSectionTransition()
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

    private fun beginSectionTransition() {
        TransitionManager.beginDelayedTransition(
            binding.root as ViewGroup,
            AutoTransition().apply { duration = 180L }
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

    private fun securityProfileLabelRes(profile: WifiSecurityProfile?): Int {
        return when (profile?.mode) {
            WifiSecurityMode.OPEN -> R.string.wifi_security_profile_open
            WifiSecurityMode.OWE -> R.string.wifi_security_profile_owe
            WifiSecurityMode.WEP -> R.string.wifi_security_profile_wep
            WifiSecurityMode.WPA2_PSK -> R.string.wifi_security_profile_wpa2_psk
            WifiSecurityMode.WPA3_SAE -> R.string.wifi_security_profile_wpa3_sae
            WifiSecurityMode.WPA2_ENTERPRISE -> R.string.wifi_security_profile_wpa2_enterprise
            WifiSecurityMode.WPA3_ENTERPRISE -> R.string.wifi_security_profile_wpa3_enterprise
            WifiSecurityMode.TRANSITION -> R.string.wifi_security_profile_transition
            WifiSecurityMode.UNKNOWN,
            null -> R.string.wifi_security_profile_unknown
        }
    }

    private fun pmfLabelRes(state: WifiPmfState): Int {
        return when (state) {
            WifiPmfState.REQUIRED -> R.string.wifi_pmf_required
            WifiPmfState.CAPABLE -> R.string.wifi_pmf_capable
            WifiPmfState.ABSENT -> R.string.wifi_pmf_absent
            WifiPmfState.NOT_APPLICABLE -> R.string.wifi_pmf_not_applicable
            WifiPmfState.UNKNOWN -> R.string.wifi_pmf_unknown
        }
    }

    private fun matchConfidenceLabelRes(confidence: WifiMatchConfidence): Int {
        return when (confidence) {
            WifiMatchConfidence.VERIFIED_BSSID -> R.string.wifi_match_confidence_verified_bssid
            WifiMatchConfidence.SSID_ONLY_SINGLE -> R.string.wifi_match_confidence_ssid_only_single
            WifiMatchConfidence.AMBIGUOUS_SSID -> R.string.wifi_match_confidence_ambiguous_ssid
            WifiMatchConfidence.WIFI_INFO_ONLY -> R.string.wifi_match_confidence_wifi_info_only
            WifiMatchConfidence.UNAVAILABLE -> R.string.wifi_match_confidence_unavailable
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
            WifiAlertReason.TRUSTED_FINGERPRINT_CHANGED -> R.string.wifi_reason_trusted_fingerprint_changed
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
            WifiAlertReason.TRUSTED_FINGERPRINT_CHANGED -> R.string.wifi_security_trusted_fingerprint_changed
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
            WifiAlertReason.TRUSTED_FINGERPRINT_CHANGED,
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

    private fun trustedBaselineDetail(
        profile: TrustedWifiProfile,
        isCurrent: Boolean,
        currentAlert: WifiSecurityAlert?
    ): String {
        if (isCurrent && currentAlert?.trustDetail != null) {
            return currentAlert.trustDetail
        }
        if (profile.pendingBssid != null && profile.pendingSeenCount > 0) {
            return getString(
                R.string.wifi_trust_detail_pending_format,
                profile.pendingSeenCount
            )
        }
        return getString(R.string.wifi_trust_detail_stable)
    }

    private fun storedTrustStatus(profile: TrustedWifiProfile): WifiTrustBaselineStatus {
        return when {
            profile.pendingSeenCount > 0 -> WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT
            profile.knownBssids.isEmpty() -> WifiTrustBaselineStatus.STABLE_PROFILE_ONLY
            else -> WifiTrustBaselineStatus.STABLE_VERIFIED
        }
    }

    private fun trustRiskLevel(status: WifiTrustBaselineStatus): WifiNetworkRiskLevel {
        return when (status) {
            WifiTrustBaselineStatus.DOWNGRADED -> WifiNetworkRiskLevel.DANGER
            WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT,
            WifiTrustBaselineStatus.FINGERPRINT_CHANGED,
            WifiTrustBaselineStatus.AMBIGUOUS,
            WifiTrustBaselineStatus.UNVERIFIED,
            WifiTrustBaselineStatus.STABLE_PROFILE_ONLY -> WifiNetworkRiskLevel.WARNING

            WifiTrustBaselineStatus.STABLE_VERIFIED,
            WifiTrustBaselineStatus.NOT_TRUSTED -> WifiNetworkRiskLevel.SAFE
        }
    }

    private fun shouldHighlightTrustStatus(status: WifiTrustBaselineStatus): Boolean {
        return status in setOf(
            WifiTrustBaselineStatus.STABLE_PROFILE_ONLY,
            WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT,
            WifiTrustBaselineStatus.FINGERPRINT_CHANGED,
            WifiTrustBaselineStatus.DOWNGRADED,
            WifiTrustBaselineStatus.AMBIGUOUS,
            WifiTrustBaselineStatus.UNVERIFIED
        )
    }

    private fun trustStatusLabelRes(status: WifiTrustBaselineStatus): Int {
        return when (status) {
            WifiTrustBaselineStatus.NOT_TRUSTED -> R.string.wifi_trust_baseline_status_not_trusted
            WifiTrustBaselineStatus.STABLE_VERIFIED -> R.string.wifi_trust_baseline_status_stable_verified
            WifiTrustBaselineStatus.STABLE_PROFILE_ONLY -> R.string.wifi_trust_baseline_status_stable_profile_only
            WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT -> R.string.wifi_trust_baseline_status_pending
            WifiTrustBaselineStatus.FINGERPRINT_CHANGED -> R.string.wifi_trust_baseline_status_changed
            WifiTrustBaselineStatus.DOWNGRADED -> R.string.wifi_trust_baseline_status_downgraded
            WifiTrustBaselineStatus.AMBIGUOUS -> R.string.wifi_trust_baseline_status_ambiguous
            WifiTrustBaselineStatus.UNVERIFIED -> R.string.wifi_trust_baseline_status_unverified
        }
    }

    private fun trustedStatusSummary(status: WifiTrustBaselineStatus): String {
        return getString(
            when (status) {
                WifiTrustBaselineStatus.NOT_TRUSTED -> R.string.wifi_trust_summary_not_trusted
                WifiTrustBaselineStatus.STABLE_VERIFIED -> R.string.wifi_trust_summary_stable_verified
                WifiTrustBaselineStatus.STABLE_PROFILE_ONLY -> R.string.wifi_trust_summary_stable_profile_only
                WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT -> R.string.wifi_trust_summary_pending
                WifiTrustBaselineStatus.FINGERPRINT_CHANGED -> R.string.wifi_trust_summary_changed
                WifiTrustBaselineStatus.DOWNGRADED -> R.string.wifi_trust_summary_downgraded
                WifiTrustBaselineStatus.AMBIGUOUS -> R.string.wifi_trust_summary_ambiguous
                WifiTrustBaselineStatus.UNVERIFIED -> R.string.wifi_trust_summary_unverified
            }
        )
    }

    private fun maxRiskLevel(
        first: WifiNetworkRiskLevel,
        second: WifiNetworkRiskLevel
    ): WifiNetworkRiskLevel {
        return when {
            first == WifiNetworkRiskLevel.DANGER || second == WifiNetworkRiskLevel.DANGER ->
                WifiNetworkRiskLevel.DANGER

            first == WifiNetworkRiskLevel.WARNING || second == WifiNetworkRiskLevel.WARNING ->
                WifiNetworkRiskLevel.WARNING

            else -> WifiNetworkRiskLevel.SAFE
        }
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

    private fun internetChip(hasInternetAccess: Boolean?, notApplicable: Boolean): UiChip {
        return when (hasInternetAccess) {
            true -> UiChip(
                text = getString(R.string.wifi_chip_validated_internet),
                level = WifiNetworkRiskLevel.SAFE,
                dialogTitle = "Internet validation",
                dialogMessage = "Android validated internet access on this Wi-Fi. That usually means captive portal checks passed and the network can reach the public internet."
            )

            false -> UiChip(
                text = getString(R.string.wifi_chip_no_internet),
                level = WifiNetworkRiskLevel.WARNING,
                dialogTitle = "Internet validation",
                dialogMessage = "Android has not validated working internet access on this Wi-Fi. That can happen on captive portals, restricted networks, or broken connections."
            )

            null -> UiChip(
                text = getString(
                    if (notApplicable) {
                        R.string.wifi_internet_access_not_applicable
                    } else {
                        R.string.wifi_chip_internet_unknown
                    }
                ),
                dialogTitle = "Internet validation",
                dialogMessage = if (notApplicable) {
                    "Internet validation is not applicable because the device is not currently using Wi-Fi."
                } else {
                    "The app could not determine whether Android has validated internet access for this Wi-Fi."
                }
            )
        }
    }

    private fun nearbyNetworkChips(network: WifiNearbyNetwork): List<UiChip> {
        val chips = mutableListOf<UiChip>()
        chips += nearbyScoreChip(network)
        chips += securityProfileChip(network.securityProfile)
        val pmfState = network.securityProfile?.pmfState
        if (pmfState != null && pmfState != WifiPmfState.UNKNOWN && pmfState != WifiPmfState.NOT_APPLICABLE) {
            chips += pmfChip(pmfState)
        }
        if (network.isTrusted) {
            chips += trustStatusChip(
                isTrusted = true,
                trustStatus = network.trustStatus
            )
        }
        if (network.isLimitedData) {
            chips += limitedVerificationChip(network.scoreUncertainties)
        }
        if (network.isCurrent) {
            chips += internetChip(
                hasInternetAccess = network.hasInternetAccess,
                notApplicable = false
            )
        }
        if (network.matchConfidence != WifiMatchConfidence.VERIFIED_BSSID) {
            chips += matchConfidenceChip(network.matchConfidence)
        }
        return chips
    }

    private fun trustedNetworkChips(
        displayedSecurityProfile: WifiSecurityProfile?,
        trustStatus: WifiTrustBaselineStatus,
        hasInternetAccess: Boolean?,
        isCurrent: Boolean
    ): List<UiChip> {
        val chips = mutableListOf<UiChip>()
        chips += securityProfileChip(displayedSecurityProfile)
        chips += trustStatusChip(
            isTrusted = true,
            trustStatus = trustStatus
        )
        if (isCurrent) {
            chips += internetChip(
                hasInternetAccess = hasInternetAccess,
                notApplicable = false
            )
        }
        return chips
    }

    private fun renderChipRow(
        container: LinearLayout,
        chips: List<UiChip>
    ) {
        container.removeAllViews()
        chips.forEachIndexed { index, chip ->
            container.addView(createChipView(chip, addLeadingMargin = index > 0))
        }
    }

    private fun createChipView(
        chip: UiChip,
        addLeadingMargin: Boolean
    ): TextView {
        val view = TextView(requireContext()).apply {
            text = chip.text
            textSize = 12f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(18, 8, 18, 8)
        }
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            if (addLeadingMargin) {
                marginStart = 8
            }
        }
        when (chip.level) {
            WifiNetworkRiskLevel.SAFE -> {
                view.setBackgroundResource(R.drawable.bg_wifi_risk_safe)
                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.wifi_risk_safe_text))
            }

            WifiNetworkRiskLevel.WARNING -> {
                view.setBackgroundResource(R.drawable.bg_wifi_risk_warning)
                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.wifi_risk_warning_text))
            }

            WifiNetworkRiskLevel.DANGER -> {
                view.setBackgroundResource(R.drawable.bg_wifi_risk_danger)
                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.wifi_risk_danger_text))
            }

            null -> {
                view.setBackgroundResource(R.drawable.bg_wifi_chip_neutral)
                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_blue))
            }
        }
        if (!chip.dialogTitle.isNullOrBlank() && !chip.dialogMessage.isNullOrBlank()) {
            view.setOnLongClickListener {
                showInfoDialog(chip.dialogTitle, chip.dialogMessage)
                true
            }
        }
        return view
    }

    private fun securityProfileChip(profile: WifiSecurityProfile?): UiChip {
        return UiChip(
            text = getString(securityProfileLabelRes(profile)),
            dialogTitle = "Security profile",
            dialogMessage = buildSecurityProfileExplanation(profile)
        )
    }

    private fun pmfChip(state: WifiPmfState): UiChip {
        return UiChip(
            text = getString(R.string.wifi_chip_pmf_format, getString(pmfLabelRes(state))),
            level = if (state == WifiPmfState.ABSENT) WifiNetworkRiskLevel.WARNING else null,
            dialogTitle = "Protected Management Frames (PMF)",
            dialogMessage = buildPmfExplanation(state)
        )
    }

    private fun trustStatusChip(
        isTrusted: Boolean,
        trustStatus: WifiTrustBaselineStatus
    ): UiChip {
        return if (!isTrusted) {
            UiChip(
                text = getString(R.string.wifi_chip_not_trusted),
                dialogTitle = "Trusted baseline",
                dialogMessage = "This Wi-Fi does not have a saved trusted baseline yet, so the app is only scoring the live security posture and not checking for trusted fingerprint changes."
            )
        } else {
            UiChip(
                text = getString(trustStatusLabelRes(trustStatus)),
                level = trustRiskLevel(trustStatus),
                dialogTitle = "Trusted baseline",
                dialogMessage = buildTrustStatusExplanation(trustStatus)
            )
        }
    }

    private fun matchConfidenceChip(confidence: WifiMatchConfidence): UiChip {
        return UiChip(
            text = getString(matchConfidenceLabelRes(confidence)),
            level = WifiNetworkRiskLevel.WARNING,
            dialogTitle = "Access point identity",
            dialogMessage = buildMatchConfidenceExplanation(confidence)
        )
    }

    private fun scanFreshnessChip(alert: WifiSecurityAlert): UiChip {
        val textRes = when (alert.scanFreshness) {
            WifiScanFreshness.FRESH -> R.string.wifi_scan_chip_fresh
            WifiScanFreshness.CACHED -> R.string.wifi_scan_chip_cached
            WifiScanFreshness.STALE -> R.string.wifi_scan_chip_stale
            WifiScanFreshness.UNAVAILABLE -> R.string.wifi_scan_chip_unavailable
        }
        val level = when (alert.scanFreshness) {
            WifiScanFreshness.FRESH -> WifiNetworkRiskLevel.SAFE
            WifiScanFreshness.CACHED,
            WifiScanFreshness.STALE,
            WifiScanFreshness.UNAVAILABLE -> WifiNetworkRiskLevel.WARNING
        }
        return UiChip(
            text = getString(textRes),
            level = level,
            dialogTitle = getString(R.string.wifi_scan_data_title),
            dialogMessage = scanFreshnessText(alert)
        )
    }

    private fun limitedVerificationChip(): UiChip {
        return limitedVerificationChip(latestSafetyAssessment?.uncertainties.orEmpty())
    }

    private fun limitedVerificationChip(
        uncertainties: Set<WifiAssessmentUncertainty>
    ): UiChip {
        val uncertaintyText = uncertainties
            .joinToString("\n") { uncertainty -> "- ${uncertaintyLabel(uncertainty)}" }
        return UiChip(
            text = getString(R.string.wifi_chip_limited_verification),
            level = WifiNetworkRiskLevel.WARNING,
            dialogTitle = "Limited verification",
            dialogMessage = buildString {
                append("Missing data does not reduce the safety score. These checks could not be verified confidently.")
                if (uncertaintyText.isNotBlank()) {
                    append("\n\nActive uncertainty signals:\n")
                    append(uncertaintyText)
                }
            }
        )
    }

    private fun nearbyScoreChip(network: WifiNearbyNetwork): UiChip {
        return UiChip(
            text = getString(R.string.wifi_nearby_score_chip_format, network.score),
            level = network.scoreLevel,
            dialogTitle = getString(R.string.wifi_nearby_score_details_title, network.ssid),
            dialogMessage = buildNearbyScoreDetailsMessage(network)
        )
    }

    private fun buildNearbyScoreDetailsMessage(network: WifiNearbyNetwork): String {
        val activeDimensions = network.scoreDimensions
            .filter { it.penalty > 0 }
            .sortedByDescending { it.penalty }
        return buildString {
            append(getString(R.string.wifi_safety_score_format, network.score))
            append('\n')
            append(getString(R.string.wifi_score_breakdown_total_format, network.scoreDimensions.sumOf { it.penalty }))
            append("\n\n")
            append(
                getString(
                    if (network.isCurrent) {
                        R.string.wifi_nearby_score_details_current_note
                    } else {
                        R.string.wifi_nearby_score_details_scan_note
                    }
                )
            )
            if (network.scoreSummary.isNotBlank()) {
                append("\n\nSummary\n")
                append(network.scoreSummary)
            }
            if (network.scoreUncertainties.isNotEmpty()) {
                append("\n\nActive uncertainty signals")
                network.scoreUncertainties.forEach { uncertainty ->
                    append("\n- ")
                    append(uncertaintyLabel(uncertainty))
                }
            }
            if (activeDimensions.isNotEmpty()) {
                append("\n\n")
                append(getString(R.string.wifi_score_details_areas))
                activeDimensions.forEach { dimension ->
                    append("\n\n- ")
                    append(getString(dimension.dimension.titleResId))
                    append(": -")
                    append(dimension.penalty)
                    append("\n")
                    append(dimension.summary)
                }
            }
            if (network.scoreChecks.isNotEmpty()) {
                append("\n\n")
                append(getString(R.string.wifi_score_details_checks))
                network.scoreChecks
                    .sortedByDescending { it.penalty }
                    .forEach { check ->
                        append("\n\n- ")
                        append(getString(check.titleResId))
                        append(" [")
                        append(getString(riskLevelLabelRes(check.level)))
                        append("]")
                        if (check.penalty > 0) {
                            append(" -")
                            append(check.penalty)
                        }
                        append("\n")
                        append(check.detail)
                    }
            }
        }
    }

    private fun buildDimensionBreakdownMessage(dimension: WifiScoreDimensionResult): String {
        val checks = latestAdvancedChecks
            .filter { it.dimension == dimension.dimension }
            .sortedByDescending { it.penalty }
        return buildString {
            append("Applied penalty: -${dimension.penalty}")
            append("\nStatus: ")
            append(getString(riskLevelLabelRes(dimension.level)))
            if (dimension.rawPenalty != dimension.penalty) {
                append("\nRaw penalty before cap: -${dimension.rawPenalty}")
            }
            append("\n\nSummary\n")
            append(dimension.summary)
            if (checks.isNotEmpty()) {
                append("\n\nComponents")
                checks.forEach { check ->
                    append("\n\n- ")
                    append(getString(check.titleResId))
                    append(": ")
                    append(getString(riskLevelLabelRes(check.level)))
                    if (check.penalty > 0) {
                        append(" (-${check.penalty})")
                    }
                    append("\n")
                    append(check.detail)
                }
            }
        }
    }

    private fun buildTotalBreakdownMessage(dimensions: List<WifiScoreDimensionResult>): String {
        val active = dimensions.filter { it.penalty > 0 }.sortedByDescending { it.penalty }
        return buildString {
            append(latestSafetyAssessment?.let {
                getString(R.string.wifi_safety_score_format, it.score)
            } ?: getString(R.string.wifi_safety_score_default))
            if (active.isNotEmpty()) {
                append("\n\nActive dimensions")
                active.forEach { dimension ->
                    append("\n- ")
                    append(getString(dimension.dimension.titleResId))
                    append(": -${dimension.penalty}")
                    append("\n")
                    append(dimension.summary)
                }
            }
        }
    }

    private fun buildSecurityProfileExplanation(profile: WifiSecurityProfile?): String {
        if (profile == null) {
            return "The app could not determine the security profile for this Wi-Fi."
        }
        val modeText = when (profile.mode) {
            WifiSecurityMode.OPEN -> "Open means there is no Wi-Fi link encryption between the device and the access point."
            WifiSecurityMode.OWE -> "OWE protects open-style Wi-Fi by encrypting traffic without requiring a shared password."
            WifiSecurityMode.WEP -> "WEP is obsolete and considered weak."
            WifiSecurityMode.WPA2_PSK -> "WPA2 Personal is still common, but it is older than WPA3."
            WifiSecurityMode.WPA3_SAE -> "WPA3 Personal is the modern personal Wi-Fi standard."
            WifiSecurityMode.WPA2_ENTERPRISE -> "WPA2 Enterprise is managed Wi-Fi with stronger authentication than personal WPA2."
            WifiSecurityMode.WPA3_ENTERPRISE -> "WPA3 Enterprise is the strongest enterprise Wi-Fi profile exposed here."
            WifiSecurityMode.TRANSITION -> "Transition mode allows both WPA2 and WPA3 style access, which improves compatibility but can allow downgrade paths."
            WifiSecurityMode.UNKNOWN -> "Android or scan data did not expose a reliable security mode."
        }
        return buildString {
            append("Observed profile: ${getString(securityProfileLabelRes(profile))}\n\n")
            append(modeText)
            if (profile.hasWeakCipher) {
                append("\n\nThe network also exposed a weak legacy cipher such as TKIP.")
            }
            if (profile.hasWps) {
                append("\n\nThe network also advertised WPS, which is generally discouraged.")
            }
        }
    }

    private fun buildPmfExplanation(state: WifiPmfState): String {
        val stateDetail = when (state) {
            WifiPmfState.REQUIRED -> "Required means clients must use PMF. That is the strongest posture."
            WifiPmfState.CAPABLE -> "Optional means the AP supports PMF, but clients may still connect without requiring it."
            WifiPmfState.ABSENT -> "Not advertised means the visible Wi-Fi capabilities did not expose PMF support."
            WifiPmfState.NOT_APPLICABLE -> "PMF is not applicable for this network type."
            WifiPmfState.UNKNOWN -> "The app could not determine PMF support."
        }
        return "PMF stands for Protected Management Frames. It helps protect Wi-Fi management traffic, such as deauthentication or disassociation frames, from spoofing.\n\n$stateDetail\n\nThis matters most on modern secure networks, where required or at least capable PMF is generally better than absent PMF."
    }

    private fun buildTrustStatusExplanation(status: WifiTrustBaselineStatus): String {
        return when (status) {
            WifiTrustBaselineStatus.NOT_TRUSTED ->
                "No trusted baseline is saved, so the app is not comparing this network against an approved fingerprint."
            WifiTrustBaselineStatus.STABLE_VERIFIED ->
                "The current trusted network matched the saved baseline by both fingerprint and access point identity."
            WifiTrustBaselineStatus.STABLE_PROFILE_ONLY ->
                "The security profile matched the saved baseline, but the app could not verify the access point by BSSID."
            WifiTrustBaselineStatus.PENDING_NEW_FINGERPRINT ->
                "A new fingerprint has been seen cleanly enough to observe, but it has not been promoted into the trusted baseline yet."
            WifiTrustBaselineStatus.FINGERPRINT_CHANGED ->
                "The current network does not match the saved trusted fingerprint for this SSID."
            WifiTrustBaselineStatus.DOWNGRADED ->
                "The current trusted network appears weaker than the baseline you previously approved."
            WifiTrustBaselineStatus.AMBIGUOUS ->
                "Multiple same-name access points are nearby, so the app cannot confidently prove which AP you are on."
            WifiTrustBaselineStatus.UNVERIFIED ->
                "The app could not verify the trusted baseline with enough identity information."
        }
    }

    private fun buildMatchConfidenceExplanation(confidence: WifiMatchConfidence): String {
        return when (confidence) {
            WifiMatchConfidence.VERIFIED_BSSID ->
                "The current access point was verified directly by BSSID, which is the strongest identity signal available here."
            WifiMatchConfidence.SSID_ONLY_SINGLE ->
                "The current access point could only be matched by SSID, with a single same-name candidate. That is weaker than a verified BSSID match."
            WifiMatchConfidence.AMBIGUOUS_SSID ->
                "Multiple same-name access points are nearby, so the app cannot confidently identify the exact current AP."
            WifiMatchConfidence.WIFI_INFO_ONLY ->
                "Android exposed the current Wi-Fi profile, but the app could not cross-check it against scan results."
            WifiMatchConfidence.UNAVAILABLE ->
                "The app could not verify the current access point identity from either scan results or usable Wi-Fi identity data."
        }
    }

    private fun uncertaintyLabel(uncertainty: WifiAssessmentUncertainty): String {
        return when (uncertainty) {
            WifiAssessmentUncertainty.ENCRYPTION_UNVERIFIED ->
                "Encryption could not be verified"
            WifiAssessmentUncertainty.AP_IDENTITY_FALLBACK ->
                "Access point identity used a fallback match"
            WifiAssessmentUncertainty.AP_IDENTITY_AMBIGUOUS ->
                "Access point identity is ambiguous"
            WifiAssessmentUncertainty.AP_IDENTITY_UNAVAILABLE ->
                "Access point identity is unavailable"
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_got_it, null)
            .show()
    }

    private fun signalQualityLabelRes(dbm: Int): Int = when {
        dbm >= -55 -> R.string.wifi_signal_excellent
        dbm >= -67 -> R.string.wifi_signal_good
        dbm >= -75 -> R.string.wifi_signal_fair
        else -> R.string.wifi_signal_weak
    }

    private fun requestNearbyWifiAccess() {
        if (latestWifiAlert?.reason == WifiAlertReason.LOCATION_SERVICES_DISABLED && hasLocationPermission()) {
            requestLocationServicesResolution(userInitiated = true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun addNearbyEmptyState(
        container: LinearLayout,
        title: String,
        message: String,
        showAction: Boolean
    ) {
        val card = MaterialCardView(requireContext()).apply {
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rs_surface_tinted))
            radius = resources.getDimension(R.dimen.rs_radius_medium)
            strokeWidth = 0
        }
        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }
        content.addView(TextView(requireContext()).apply {
            text = title
            setTextAppearance(R.style.TextAppearance_Resistine_Section)
        })
        content.addView(TextView(requireContext()).apply {
            text = message
            setTextAppearance(R.style.TextAppearance_Resistine_Body)
            setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, 0)
        })
        if (showAction) {
            content.addView(MaterialButton(requireContext()).apply {
                text = getString(R.string.wifi_nearby_access_action)
                setOnClickListener { requestNearbyWifiAccess() }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (12 * resources.displayMetrics.density).toInt() }
            })
        }
        card.addView(content)
        container.addView(card)
    }

    private fun loadDebugDetailsEnabled(): Boolean {
        return requireContext()
            .getSharedPreferences(DEBUG_PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(DEBUG_DETAILS_KEY, false)
    }

    private fun saveDebugDetailsEnabled(enabled: Boolean) {
        requireContext()
            .getSharedPreferences(DEBUG_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(DEBUG_DETAILS_KEY, enabled)
            .apply()
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

    private fun scanFreshnessText(alert: WifiSecurityAlert): String {
        val ageSeconds = alert.scanAgeMillis?.div(1_000L)
        val ageText = when {
            ageSeconds == null -> getString(R.string.wifi_scan_age_unknown)
            ageSeconds < 60L -> getString(R.string.wifi_scan_age_seconds, ageSeconds)
            else -> getString(R.string.wifi_scan_age_minutes, ageSeconds / 60L)
        }
        val state = when (alert.scanFreshness) {
            WifiScanFreshness.FRESH -> getString(R.string.wifi_scan_freshness_fresh)
            WifiScanFreshness.CACHED -> getString(R.string.wifi_scan_freshness_cached)
            WifiScanFreshness.STALE -> getString(R.string.wifi_scan_freshness_stale)
            WifiScanFreshness.UNAVAILABLE -> getString(R.string.wifi_scan_freshness_unavailable)
        }
        val throttleNote = when (alert.scanRequestStatus) {
            WifiScanRequestStatus.COOLDOWN -> getString(
                R.string.wifi_scan_refresh_cooldown,
                (alert.scanCooldownRemainingMillis + 999L) / 1_000L
            )
            WifiScanRequestStatus.REJECTED -> getString(R.string.wifi_scan_refresh_rejected)
            WifiScanRequestStatus.TIMED_OUT -> getString(R.string.wifi_scan_refresh_timed_out)
            WifiScanRequestStatus.NOT_REQUESTED,
            WifiScanRequestStatus.UPDATED -> ""
        }
        return getString(R.string.wifi_scan_freshness_line, state, ageText, throttleNote)
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

    private data class UiChip(
        val text: String,
        val level: WifiNetworkRiskLevel? = null,
        val dialogTitle: String? = null,
        val dialogMessage: String? = null
    )

    private companion object {
        private const val DEBUG_PREFS_NAME = "wifi_security_ui_prefs"
        private const val DEBUG_DETAILS_KEY = "show_debug_wifi_details"
    }
}
