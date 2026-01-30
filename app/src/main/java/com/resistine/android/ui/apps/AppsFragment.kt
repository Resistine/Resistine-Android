package com.resistine.android.ui.apps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.resistine.android.R
import com.resistine.android.databinding.FragmentAppsBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import android.widget.ArrayAdapter
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

        adapter = AppAdapter(requireContext().packageManager)
        binding.recyclerViewApps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewApps.adapter = adapter
        binding.recyclerViewApps.setHasFixedSize(true)

        setupFilters()
        setupFilterToggle()

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
            applyFilters()
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
        _binding = null
    }

    private companion object {
        private const val PREFS_NAME = "deceptive_scan_prefs"
        private const val KEY_CONSENT = "scan_consent"
        private const val KEY_SHOW_SYSTEM_APPS = "show_system_apps"
    }

    private enum class SortOption {
        NAME_ASC,
        NAME_DESC,
        RISK_HIGH_LOW,
        RISK_LOW_HIGH
    }
}
