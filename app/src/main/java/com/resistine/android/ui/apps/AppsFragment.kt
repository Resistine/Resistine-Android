package com.resistine.android.ui.apps

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.resistine.android.R
import com.resistine.android.databinding.FragmentAppsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppsViewModel
    private lateinit var adapter: AppAdapter
    private var allApps: List<AppEntry> = emptyList()
    private var sortOption: SortOption = SortOption.NAME_ASC
    private var searchQuery: String = ""
    private var filtersExpanded: Boolean = true
    private lateinit var permissionScopeOptions: List<PermissionScopeOption>
    private var selectedPermissionScope: String? = null
    private var reviewQueue: List<AppEntry> = emptyList()
    private var reviewScopePermission: String? = null
    private var reviewCurrentIndex: Int = -1
    private val liveHighRiskPermissionOverrides = HashMap<String, Set<String>>()
    private var lastOpenedAppPackage: String? = null
    private var permissionRefreshJob: Job? = null
    private val badgeBaseLabels: Map<BadgeType, String> by lazy {
        mapOf(
            BadgeType.SIGNATURE_MISMATCH to getString(R.string.badge_signature_mismatch),
            BadgeType.LOOKALIKE_NAME to getString(R.string.badge_lookalike_name),
            BadgeType.OUTDATED to getString(R.string.badge_outdated),
            BadgeType.SIDELOADED to getString(R.string.badge_sideloaded),
            BadgeType.DEBUGGABLE to getString(R.string.badge_debuggable),
            BadgeType.OLD_TARGET_SDK to getString(R.string.badge_old_target_sdk),
            BadgeType.HIGH_RISK_PERMISSION to getString(R.string.badge_high_risk_permission)
        )
    }
    private val riskBaseLabels: Map<RiskVerdict, String> by lazy {
        mapOf(
            RiskVerdict.SAFE to getString(R.string.risk_safe),
            RiskVerdict.WARNING to getString(R.string.risk_warning),
            RiskVerdict.RISK to getString(R.string.risk_risk)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[AppsViewModel::class.java]

        adapter = AppAdapter(requireContext().packageManager) { entry ->
            val opened = openPermissionSettingsForEntry(entry, selectedPermissionScope)
            if (!opened) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_actions_open_failed, entry.label),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.recyclerViewApps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewApps.adapter = adapter
        binding.recyclerViewApps.isNestedScrollingEnabled = false
        binding.recyclerViewApps.setHasFixedSize(false)

        setupFilters()
        setupFilterToggle()
        setupPermissionActions()

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, 0)
        val initialShowSystem = prefs.getBoolean(KEY_SHOW_SYSTEM_APPS, false)
        binding.switchSystemApps.isChecked = initialShowSystem
        viewModel.setShowSystemApps(initialShowSystem)
        binding.switchSystemApps.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_SHOW_SYSTEM_APPS, isChecked).apply()
            viewModel.setShowSystemApps(isChecked)
        }

        binding.buttonScan.setOnClickListener {
            maybeRunScan()
        }

        viewModel.apps.observe(viewLifecycleOwner) { apps ->
            allApps = apps
            val currentPackages = apps.map { it.packageInfo.packageName }.toSet()
            liveHighRiskPermissionOverrides.keys.retainAll(currentPackages)
            if (lastOpenedAppPackage !in currentPackages) {
                lastOpenedAppPackage = null
            }
            clearReviewSession()
            applyFilters()
            updatePermissionActionsUi()
        }

        viewModel.scanSummary.observe(viewLifecycleOwner) { summary ->
            if (summary.isScanned) {
                binding.scanSummaryTitle.text = getString(R.string.scan_summary_title)
                binding.scanSummaryBody.text = getString(
                    R.string.scan_summary_body,
                    summary.safe,
                    summary.warning,
                    summary.risk,
                    summary.total
                )
                val formatted = DateFormat.getDateTimeInstance().format(Date(summary.lastScanAt ?: 0))
                binding.scanSummaryTime.text = getString(R.string.scan_summary_time, formatted)
            } else {
                binding.scanSummaryTitle.text = getString(R.string.scan_summary_not_scanned)
                binding.scanSummaryBody.text = getString(R.string.scan_summary_prompt)
                binding.scanSummaryTime.text = ""
            }
        }

        viewModel.isScanning.observe(viewLifecycleOwner) { scanning ->
            binding.scanProgress.visibility = if (scanning) View.VISIBLE else View.GONE
            binding.buttonScan.isEnabled = !scanning
            binding.buttonScan.text = if (scanning) {
                getString(R.string.scan_in_progress)
            } else {
                getString(R.string.scan_button)
            }
        }

        return binding.root
    }

    private fun setupFilterToggle() {
        updateFilterVisibility()
        binding.buttonToggleFilters.setOnClickListener {
            filtersExpanded = !filtersExpanded
            updateFilterVisibility()
        }
    }

    private fun updateFilterVisibility() {
        binding.filtersContainer.visibility = if (filtersExpanded) View.VISIBLE else View.GONE
        binding.buttonToggleFilters.text = if (filtersExpanded) {
            getString(R.string.hide_filters)
        } else {
            getString(R.string.show_filters)
        }
    }

    private fun setupFilters() {
        binding.searchInput.addTextChangedListener { text ->
            searchQuery = text?.toString()?.trim().orEmpty()
            applyFilters()
        }

        val sortOptions = listOf(
            getString(R.string.sort_name_asc),
            getString(R.string.sort_name_desc),
            getString(R.string.sort_risk_high_low),
            getString(R.string.sort_risk_low_high)
        )
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sortOptions)
        (binding.sortDropdown as MaterialAutoCompleteTextView).setAdapter(sortAdapter)
        binding.sortDropdown.setText(sortOptions.first(), false)
        binding.sortDropdown.setOnItemClickListener { _, _, position, _ ->
            sortOption = when (position) {
                1 -> SortOption.NAME_DESC
                2 -> SortOption.RISK_HIGH_LOW
                3 -> SortOption.RISK_LOW_HIGH
                else -> SortOption.NAME_ASC
            }
            applyFilters()
        }

        binding.riskChipGroup.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }

        binding.badgeChipGroup.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun setupPermissionActions() {
        permissionScopeOptions = buildPermissionScopeOptions()
        val labels = permissionScopeOptions.map { it.label }
        val permissionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels)
        (binding.permissionActionDropdown as MaterialAutoCompleteTextView).setAdapter(permissionAdapter)
        if (labels.isNotEmpty()) {
            binding.permissionActionDropdown.setText(labels.first(), false)
            selectedPermissionScope = permissionScopeOptions.first().permission
        }
        binding.permissionActionDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedPermissionScope = permissionScopeOptions.getOrNull(position)?.permission
            clearReviewSession()
            updatePermissionActionsUi()
        }
        binding.buttonPermissionManager.setOnClickListener {
            openPermissionManager(selectedPermissionScope)
        }
        binding.buttonReviewFlaggedApps.setOnClickListener {
            openCurrentFlaggedApp()
        }
        binding.buttonReviewPrevious.setOnClickListener {
            moveReviewSelection(-1)
        }
        binding.buttonReviewNext.setOnClickListener {
            moveReviewSelection(1)
        }
        updatePermissionActionsUi()
    }

    private fun buildPermissionScopeOptions(): List<PermissionScopeOption> {
        val options = ArrayList<PermissionScopeOption>()
        options.add(PermissionScopeOption(null, getString(R.string.permission_actions_scope_all)))
        ScanUtils.highRiskPermissions
            .sortedBy { ScanUtils.permissionDisplayName(it) }
            .forEach { permission ->
                options.add(PermissionScopeOption(permission, ScanUtils.permissionDisplayName(permission)))
            }
        return options
    }

    private fun applyFilters() {
        val query = searchQuery.lowercase()
        val selectedRisks = selectedRiskVerdicts()
        val selectedBadges = selectedBadgeTypes()

        val base = allApps.filter { entry ->
            val nameMatch = entry.label.lowercase().contains(query) ||
                entry.packageInfo.packageName.lowercase().contains(query)
            if (!nameMatch) return@filter false

            val scan = entry.scanResult
            if (selectedRisks.isNotEmpty()) {
                val verdict = scan?.verdict
                if (verdict == null || verdict !in selectedRisks) return@filter false
            }
            true
        }

        updateRiskCounts(base)
        updateBadgeCounts(base)

        val filtered = if (selectedBadges.isEmpty()) {
            base
        } else {
            base.filter { entry ->
                val badgeTypes = entry.scanResult?.badges?.map { it.type }?.toSet().orEmpty()
                badgeTypes.containsAll(selectedBadges)
            }
        }

        val sorted = when (sortOption) {
            SortOption.NAME_ASC -> filtered.sortedBy { it.label.lowercase() }
            SortOption.NAME_DESC -> filtered.sortedByDescending { it.label.lowercase() }
            SortOption.RISK_HIGH_LOW -> filtered.sortedWith(compareByDescending<AppEntry> { it.scanResult?.score ?: -1 }
                .thenBy { it.label.lowercase() })
            SortOption.RISK_LOW_HIGH -> filtered.sortedWith(compareBy<AppEntry> { it.scanResult?.score ?: Int.MAX_VALUE }
                .thenBy { it.label.lowercase() })
        }

        adapter.submitList(sorted)
    }

    private fun openPermissionManager(permission: String?) {
        val intents = ArrayList<Intent>()
        if (permission != null) {
            intents.add(
                Intent(ACTION_MANAGE_PERMISSION_APPS).apply {
                    putExtra(EXTRA_PERMISSION_NAME, permission)
                }
            )
        }
        intents.add(Intent(ACTION_MANAGE_PERMISSIONS))
        intents.add(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        val opened = launchFirstResolvable(intents)
        if (!opened) {
            Toast.makeText(
                requireContext(),
                getString(R.string.permission_actions_manager_open_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openCurrentFlaggedApp() {
        val hasCurrent = ensureReviewSession(selectedPermissionScope)
        if (!hasCurrent) {
            updatePermissionActionsUi()
            Toast.makeText(requireContext(), getString(R.string.permission_actions_no_flagged), Toast.LENGTH_SHORT).show()
            return
        }

        val currentIndex = reviewCurrentIndex
        val entry = reviewQueue[currentIndex]
        val opened = openPermissionSettingsForEntry(entry, selectedPermissionScope)
        if (!opened) {
            Toast.makeText(
                requireContext(),
                getString(R.string.permission_actions_open_failed, entry.label),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Toast.makeText(
            requireContext(),
            getString(
                R.string.permission_actions_opening_app,
                entry.label,
                currentIndex + 1,
                reviewQueue.size
            ),
            Toast.LENGTH_SHORT
        ).show()
        updatePermissionActionsUi()
    }

    private fun moveReviewSelection(delta: Int) {
        val hasCurrent = ensureReviewSession(selectedPermissionScope)
        if (!hasCurrent) {
            updatePermissionActionsUi()
            Toast.makeText(requireContext(), getString(R.string.permission_actions_no_flagged), Toast.LENGTH_SHORT).show()
            return
        }
        val newIndex = when {
            reviewQueue.size <= 1 -> reviewCurrentIndex
            delta > 0 -> if (reviewCurrentIndex >= reviewQueue.lastIndex) 0 else reviewCurrentIndex + 1
            delta < 0 -> if (reviewCurrentIndex <= 0) reviewQueue.lastIndex else reviewCurrentIndex - 1
            else -> reviewCurrentIndex
        }
        if (newIndex != reviewCurrentIndex) {
            reviewCurrentIndex = newIndex
        }
        updatePermissionActionsUi()
    }

    private fun ensureReviewSession(scopePermission: String?): Boolean {
        val scopeQueue = buildReviewQueue(scopePermission)
        val queueChanged = reviewScopePermission != scopePermission || !sameQueuePackages(reviewQueue, scopeQueue)
        if (queueChanged) {
            reviewQueue = scopeQueue
            reviewScopePermission = scopePermission
            reviewCurrentIndex = if (scopeQueue.isNotEmpty()) 0 else -1
        } else if (scopeQueue.isEmpty()) {
            reviewCurrentIndex = -1
        } else if (reviewCurrentIndex !in scopeQueue.indices) {
            reviewCurrentIndex = reviewCurrentIndex.coerceIn(0, scopeQueue.lastIndex)
        }
        return reviewCurrentIndex in reviewQueue.indices
    }

    private fun buildReviewQueue(permission: String?): List<AppEntry> {
        return allApps
            .filter { entry ->
                val scannedPermissions = entry.scanResult?.highRiskPermissions.orEmpty()
                if (scannedPermissions.isEmpty()) return@filter false
                if (permission != null && permission !in scannedPermissions) return@filter false

                val currentPermissions = resolveHighRiskPermissions(entry)
                currentPermissions.isNotEmpty() && (permission == null || permission in currentPermissions)
            }
            .sortedWith(compareBy<AppEntry> { it.label.lowercase() }.thenBy { it.packageInfo.packageName })
    }

    private fun resolveHighRiskPermissions(entry: AppEntry): Set<String> {
        val packageName = entry.packageInfo.packageName
        return liveHighRiskPermissionOverrides[packageName]
            ?: entry.scanResult?.highRiskPermissions.orEmpty().toSet()
    }

    private fun queryCurrentHighRiskPermissions(
        packageManager: PackageManager,
        packageName: String,
        label: String
    ): List<String> {
        val packageInfo = runCatching {
            val flags = PackageManager.GET_PERMISSIONS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(flags.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, flags)
            }
        }.getOrNull() ?: return emptyList()

        val requested = packageInfo.requestedPermissions ?: return emptyList()
        val requestFlags = packageInfo.requestedPermissionsFlags
        val allowed = ScanUtils.allowedHighRiskPermissions(packageName, label)
        val results = ArrayList<String>()
        for (i in requested.indices) {
            val permission = requested[i]
            if (permission !in ScanUtils.highRiskPermissions) continue
            if (permission in allowed) continue
            val granted = if (requestFlags != null && requestFlags.size > i) {
                (requestFlags[i] and android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            } else {
                true
            }
            if (granted) {
                results.add(permission)
            }
        }
        return results
    }

    private fun openPermissionSettingsForEntry(entry: AppEntry, preferredPermission: String?): Boolean {
        val packageName = entry.packageInfo.packageName
        val currentPermissions = resolveHighRiskPermissions(entry)
        val targetedPermission = preferredPermission?.takeIf { it in currentPermissions }
        val opened = openAppPermissionSettings(packageName, targetedPermission)
        if (opened) {
            lastOpenedAppPackage = packageName
        }
        return opened
    }

    private fun refreshLastOpenedAppPermissions() {
        val packageName = lastOpenedAppPackage ?: return
        val entry = allApps.firstOrNull { it.packageInfo.packageName == packageName } ?: run {
            lastOpenedAppPackage = null
            return
        }
        val context = context ?: return
        val packageManager = context.packageManager
        permissionRefreshJob?.cancel()
        permissionRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            val currentPermissions = withContext(Dispatchers.Default) {
                queryCurrentHighRiskPermissions(packageManager, packageName, entry.label).toSet()
            }
            liveHighRiskPermissionOverrides[packageName] = currentPermissions
            if (_binding != null) {
                updatePermissionActionsUi()
            }
        }
    }

    private fun openAppPermissionSettings(packageName: String, permission: String?): Boolean {
        val intents = ArrayList<Intent>()
        if (permission != null) {
            intents.add(
                Intent(ACTION_MANAGE_APP_PERMISSION).apply {
                    putExtra(EXTRA_PACKAGE_NAME, packageName)
                    putExtra(EXTRA_PERMISSION_NAME, permission)
                }
            )
        }
        intents.add(
            Intent(ACTION_MANAGE_APP_PERMISSIONS).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
        )
        intents.add(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
        return launchFirstResolvable(intents)
    }

    private fun launchFirstResolvable(intents: List<Intent>): Boolean {
        val context = context ?: return false
        val packageManager = context.packageManager
        for (intent in intents) {
            val canResolve = intent.resolveActivity(packageManager) != null
            if (!canResolve) continue
            val launched = runCatching { startActivity(intent) }.isSuccess
            if (launched) return true
        }
        return false
    }

    private fun sameQueuePackages(first: List<AppEntry>, second: List<AppEntry>): Boolean {
        if (first.size != second.size) return false
        for (i in first.indices) {
            if (first[i].packageInfo.packageName != second[i].packageInfo.packageName) {
                return false
            }
        }
        return true
    }

    private fun clearReviewSession() {
        reviewQueue = emptyList()
        reviewScopePermission = null
        reviewCurrentIndex = -1
    }

    private fun updatePermissionActionsUi() {
        val scopePermission = selectedPermissionScope
        val scopeLabel = scopePermission?.let { ScanUtils.permissionDisplayName(it) }
            ?: getString(R.string.permission_actions_scope_all)
        val hasCurrent = ensureReviewSession(scopePermission)
        val total = reviewQueue.size

        if (hasCurrent) {
            val currentEntry = reviewQueue[reviewCurrentIndex]
            binding.permissionReviewCurrentApp.text = getString(
                R.string.permission_actions_current_format,
                currentEntry.label,
                reviewCurrentIndex + 1,
                total
            )
            binding.permissionReviewStatus.text = getString(
                R.string.permission_actions_status_active_format,
                scopeLabel,
                currentEntry.label,
                reviewCurrentIndex + 1,
                total
            )
            binding.buttonReviewFlaggedApps.text = getString(
                R.string.permission_actions_open_current_named,
                currentEntry.label
            )
        } else {
            binding.permissionReviewCurrentApp.text = getString(R.string.permission_actions_current_none)
            binding.permissionReviewStatus.text = getString(
                R.string.permission_actions_status_ready_format,
                scopeLabel,
                total
            )
            binding.buttonReviewFlaggedApps.text = getString(R.string.permission_actions_open_current)
        }
        binding.buttonReviewFlaggedApps.isEnabled = hasCurrent
        binding.buttonReviewPrevious.isEnabled = hasCurrent && total > 1
        binding.buttonReviewNext.isEnabled = hasCurrent && total > 1
    }

    private fun updateBadgeCounts(base: List<AppEntry>) {
        val counts = HashMap<BadgeType, Int>()
        for (entry in base) {
            val types = entry.scanResult?.badges?.map { it.type }.orEmpty()
            for (type in types) {
                counts[type] = (counts[type] ?: 0) + 1
            }
        }
        updateBadgeChipText(binding.chipBadgeSignatureMismatch, BadgeType.SIGNATURE_MISMATCH, counts)
        updateBadgeChipText(binding.chipBadgeLookalike, BadgeType.LOOKALIKE_NAME, counts)
        updateBadgeChipText(binding.chipBadgeOutdated, BadgeType.OUTDATED, counts)
        updateBadgeChipText(binding.chipBadgeSideloaded, BadgeType.SIDELOADED, counts)
        updateBadgeChipText(binding.chipBadgeDebuggable, BadgeType.DEBUGGABLE, counts)
        updateBadgeChipText(binding.chipBadgeOldTargetSdk, BadgeType.OLD_TARGET_SDK, counts)
        updateBadgeChipText(binding.chipBadgeHighRiskPermission, BadgeType.HIGH_RISK_PERMISSION, counts)
    }

    private fun updateRiskCounts(base: List<AppEntry>) {
        val counts = HashMap<RiskVerdict, Int>()
        for (entry in base) {
            val verdict = entry.scanResult?.verdict ?: continue
            counts[verdict] = (counts[verdict] ?: 0) + 1
        }
        updateRiskChipText(binding.chipRiskSafe, RiskVerdict.SAFE, counts)
        updateRiskChipText(binding.chipRiskWarning, RiskVerdict.WARNING, counts)
        updateRiskChipText(binding.chipRiskRisk, RiskVerdict.RISK, counts)
    }

    private fun updateRiskChipText(
        chip: com.google.android.material.chip.Chip,
        verdict: RiskVerdict,
        counts: Map<RiskVerdict, Int>
    ) {
        val baseLabel = riskBaseLabels[verdict] ?: verdict.name
        val count = counts[verdict] ?: 0
        chip.text = "$baseLabel ($count)"
    }

    private fun updateBadgeChipText(
        chip: com.google.android.material.chip.Chip,
        type: BadgeType,
        counts: Map<BadgeType, Int>
    ) {
        val baseLabel = badgeBaseLabels[type] ?: type.name
        val count = counts[type] ?: 0
        chip.text = "$baseLabel ($count)"
    }

    private fun selectedRiskVerdicts(): Set<RiskVerdict> {
        val checked = binding.riskChipGroup.checkedChipIds
        val result = HashSet<RiskVerdict>()
        for (id in checked) {
            when (id) {
                R.id.chip_risk_safe -> result.add(RiskVerdict.SAFE)
                R.id.chip_risk_warning -> result.add(RiskVerdict.WARNING)
                R.id.chip_risk_risk -> result.add(RiskVerdict.RISK)
            }
        }
        return result
    }

    private fun selectedBadgeTypes(): Set<BadgeType> {
        val checked = binding.badgeChipGroup.checkedChipIds
        val result = HashSet<BadgeType>()
        for (id in checked) {
            when (id) {
                R.id.chip_badge_signature_mismatch -> result.add(BadgeType.SIGNATURE_MISMATCH)
                R.id.chip_badge_lookalike -> result.add(BadgeType.LOOKALIKE_NAME)
                R.id.chip_badge_outdated -> result.add(BadgeType.OUTDATED)
                R.id.chip_badge_sideloaded -> result.add(BadgeType.SIDELOADED)
                R.id.chip_badge_debuggable -> result.add(BadgeType.DEBUGGABLE)
                R.id.chip_badge_old_target_sdk -> result.add(BadgeType.OLD_TARGET_SDK)
                R.id.chip_badge_high_risk_permission -> result.add(BadgeType.HIGH_RISK_PERMISSION)
            }
        }
        return result
    }

    private fun maybeRunScan() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, 0)
        val consented = prefs.getBoolean(KEY_CONSENT, false)
        if (consented) {
            viewModel.startScan()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.scan_consent_title)
            .setMessage(R.string.scan_consent_message)
            .setPositiveButton(R.string.scan_consent_accept) { _, _ ->
                prefs.edit().putBoolean(KEY_CONSENT, true).apply()
                viewModel.startScan()
            }
            .setNegativeButton(R.string.scan_consent_decline, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        permissionRefreshJob?.cancel()
        permissionRefreshJob = null
        clearReviewSession()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            refreshLastOpenedAppPermissions()
            updatePermissionActionsUi()
        }
    }

    private companion object {
        private const val PREFS_NAME = "deceptive_scan_prefs"
        private const val KEY_CONSENT = "scan_consent"
        private const val KEY_SHOW_SYSTEM_APPS = "show_system_apps"
        private const val ACTION_MANAGE_PERMISSION_APPS = "android.intent.action.MANAGE_PERMISSION_APPS"
        private const val ACTION_MANAGE_PERMISSIONS = "android.intent.action.MANAGE_PERMISSIONS"
        private const val ACTION_MANAGE_APP_PERMISSION = "android.intent.action.MANAGE_APP_PERMISSION"
        private const val ACTION_MANAGE_APP_PERMISSIONS = "android.intent.action.MANAGE_APP_PERMISSIONS"
        private const val EXTRA_PACKAGE_NAME = "android.intent.extra.PACKAGE_NAME"
        private const val EXTRA_PERMISSION_NAME = "android.intent.extra.PERMISSION_NAME"
    }

    private data class PermissionScopeOption(
        val permission: String?,
        val label: String
    )

    private enum class SortOption {
        NAME_ASC,
        NAME_DESC,
        RISK_HIGH_LOW,
        RISK_LOW_HIGH
    }
}
