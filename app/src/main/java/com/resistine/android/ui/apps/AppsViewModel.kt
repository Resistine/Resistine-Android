package com.resistine.android.ui.apps

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.resistine.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, 0)

    private val _apps = MutableLiveData<List<AppEntry>>()
    val apps: LiveData<List<AppEntry>> get() = _apps

    private val _scanSummary = MutableLiveData<ScanSummary>()
    val scanSummary: LiveData<ScanSummary> get() = _scanSummary

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> get() = _isScanning


    private val _showSystemApps = MutableLiveData(false)
    val showSystemApps: LiveData<Boolean> get() = _showSystemApps

    private var scanJob: Job? = null
    private var registryIndex: RegistryIndex? = null
    private val hashCache = HashMap<String, List<String>>()
    private var showSystemAppsFlag = false
    private var lastScanAt: Long? = null
    private var lastSummarySafe: Int = 0
    private var lastSummaryWarning: Int = 0
    private var lastSummaryRisk: Int = 0
    private var lastSummaryTotal: Int = 0
    private var persistedScans: Map<String, PersistedScan> = emptyMap()

    init {
        val saved = prefs.getLong(KEY_LAST_SCAN_AT, 0L)
        lastScanAt = if (saved > 0) saved else null
        lastSummarySafe = prefs.getInt(KEY_LAST_SCAN_SAFE, 0)
        lastSummaryWarning = prefs.getInt(KEY_LAST_SCAN_WARNING, 0)
        lastSummaryRisk = prefs.getInt(KEY_LAST_SCAN_RISK, 0)
        lastSummaryTotal = prefs.getInt(KEY_LAST_SCAN_TOTAL, 0)
        persistedScans = loadPersistedScans()
        loadAllPackages()
    }

    fun setShowSystemApps(enabled: Boolean) {
        if (showSystemAppsFlag == enabled) return
        showSystemAppsFlag = enabled
        _showSystemApps.postValue(enabled)
        loadAllPackages(resetSummary = false)
    }

    private fun loadAllPackages(resetSummary: Boolean = true) {
        viewModelScope.launch(Dispatchers.Default) {
            val existingResults = _apps.value
                ?.associate { it.packageInfo.packageName to it.scanResult }
                ?.filterValues { it != null }
                ?.mapValues { it.value!! }
                ?: emptyMap()
            val allPackages = loadInstalledApps(existingResults, persistedScans)

            _apps.postValue(allPackages)
            if (resetSummary) {
                _scanSummary.postValue(
                    ScanSummary(
                        total = allPackages.size,
                        safe = 0,
                        warning = 0,
                        risk = 0,
                        lastScanAt = lastScanAt,
                        isScanned = lastScanAt != null
                    )
                )
            } else {
                val hasScans = allPackages.any { it.scanResult != null }
                if (hasScans) {
                    _scanSummary.postValue(buildSummary(allPackages))
                } else if (lastScanAt != null) {
                    _scanSummary.postValue(
                        ScanSummary(
                            total = if (lastSummaryTotal > 0) lastSummaryTotal else allPackages.size,
                            safe = lastSummarySafe,
                            warning = lastSummaryWarning,
                            risk = lastSummaryRisk,
                            lastScanAt = lastScanAt,
                            isScanned = true
                        )
                    )
                } else {
                    _scanSummary.postValue(
                        ScanSummary(
                            total = allPackages.size,
                            safe = 0,
                            warning = 0,
                            risk = 0,
                            lastScanAt = null,
                            isScanned = false
                        )
                    )
                }
            }
        }
    }

    fun startScan() {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch(Dispatchers.Default) {
            _isScanning.postValue(true)
            try {
                val appContext = getApplication<Application>()
                val currentApps = _apps.value?.ifEmpty { loadInstalledApps() } ?: loadInstalledApps()
                val registry = registryIndex ?: runCatching {
                    ScanUtils.loadRegistry(appContext, REGISTRY_ASSET_NAME)
                }.getOrElse {
                    RegistryIndex(emptyMap(), emptyMap())
                }.also {
                    registryIndex = it
                }
                val officialPackages = registry.byPackage.keys

                val normalizedInstalled = currentApps.associate { entry ->
                    entry.packageInfo.packageName to ScanUtils.normalizeName(entry.label)
                }
                val localClonePackages = detectLocalClones(normalizedInstalled, officialPackages)

                val scanned = ArrayList<AppEntry>(currentApps.size)
                currentApps.forEach { entry ->
                    val pkg = entry.packageInfo.packageName
                    val appInfo = entry.packageInfo.applicationInfo
                    val badges = ArrayList<Badge>()
                    var score = 0

                    val registryEntry = registry.byPackage[pkg]
                    val versionCode = entry.packageInfo.longVersionCodeCompat()

                    val isSystemApp = appInfo != null && !isUserInstalled(appInfo)
                    val isSideloaded = !isSystemApp && isSideloaded(appContext, pkg)
                    if (isSideloaded) {
                        badges.add(Badge(BadgeType.SIDELOADED, badgeLabel(appContext, BadgeType.SIDELOADED)))
                        score += 15
                    }

                    val isDebuggable = appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    if (isDebuggable) {
                        badges.add(Badge(BadgeType.DEBUGGABLE, badgeLabel(appContext, BadgeType.DEBUGGABLE)))
                        score += 10
                    }

                    val targetSdk = appInfo?.targetSdkVersion ?: 0
                    if (targetSdk in 1 until 28) {
                        badges.add(Badge(BadgeType.OLD_TARGET_SDK, badgeLabel(appContext, BadgeType.OLD_TARGET_SDK)))
                        score += 10
                    }

                    val highRiskGranted = grantedHighRiskPermissions(entry.packageInfo, pkg, entry.label)
                    if (highRiskGranted.isNotEmpty()) {
                        val label = appContext.getString(
                            R.string.badge_high_risk_permission_count,
                            highRiskGranted.size
                        )
                        val description = buildHighRiskDescription(appContext, highRiskGranted)
                        badges.add(Badge(BadgeType.HIGH_RISK_PERMISSION, label, description))
                        score += highRiskGranted.size * HIGH_RISK_PERMISSION_SCORE_PERM
                    }

                    if (registryEntry != null) {
                        if (registryEntry.versionCode > 0 && versionCode in 1 until registryEntry.versionCode) {
                            badges.add(Badge(BadgeType.OUTDATED, badgeLabel(appContext, BadgeType.OUTDATED)))
                            score += 15
                        }
                        if (registryEntry.certSha256.isNotEmpty()) {
                            val signatures = ScanUtils.signatureSha256(entry.packageInfo).map { it.lowercase() }.toSet()
                            val matches = signatures.intersect(registryEntry.certSha256).isNotEmpty()
                            if (!matches) {
                                badges.add(Badge(BadgeType.SIGNATURE_MISMATCH, badgeLabel(appContext, BadgeType.SIGNATURE_MISMATCH)))
                                score += 60
                            }
                        }
                    }

                    val isOfficial = registryEntry != null
                    if (!isOfficial) {
                        val lookalikeOfficial = isOfficialLookalike(entry.label, registry, pkg)
                        if (lookalikeOfficial) {
                            badges.add(Badge(BadgeType.LOOKALIKE_NAME, badgeLabel(appContext, BadgeType.LOOKALIKE_NAME)))
                            score += 35
                        }
                    }

                if (!isSystemApp && pkg in localClonePackages) {
                    val already = badges.any { it.type == BadgeType.LOOKALIKE_NAME }
                    if (!already) {
                        badges.add(Badge(BadgeType.LOOKALIKE_NAME, badgeLabel(appContext, BadgeType.LOOKALIKE_NAME)))
                        score += 35
                    }
                }

                    val apkHashes = hashApk(entry)

                    val verdict = when {
                        score >= 60 -> RiskVerdict.RISK
                        score >= 30 -> RiskVerdict.WARNING
                        else -> RiskVerdict.SAFE
                    }

                    scanned.add(entry.copy(
                        scanResult = AppScanResult(
                            score = score,
                            verdict = verdict,
                            badges = badges,
                            apkSha256 = apkHashes
                        )
                    ))
                }
                val sortedScanned = scanned.sortedBy { it.label.lowercase() }

                lastScanAt = System.currentTimeMillis()
                val summary = buildSummary(sortedScanned)
                lastSummarySafe = summary.safe
                lastSummaryWarning = summary.warning
                lastSummaryRisk = summary.risk
                lastSummaryTotal = summary.total
                prefs.edit()
                    .putLong(KEY_LAST_SCAN_AT, lastScanAt ?: 0L)
                    .putInt(KEY_LAST_SCAN_SAFE, lastSummarySafe)
                    .putInt(KEY_LAST_SCAN_WARNING, lastSummaryWarning)
                    .putInt(KEY_LAST_SCAN_RISK, lastSummaryRisk)
                    .putInt(KEY_LAST_SCAN_TOTAL, lastSummaryTotal)
                    .apply()
                persistScans(sortedScanned)
                _apps.postValue(sortedScanned)
                _scanSummary.postValue(summary)
            } finally {
                _isScanning.postValue(false)
            }
        }
    }

    private fun loadInstalledApps(
        existingResults: Map<String, AppScanResult> = emptyMap(),
        persisted: Map<String, PersistedScan> = emptyMap()
    ): List<AppEntry> {
        val pm = getApplication<Application>().packageManager
        val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNING_CERTIFICATES
        val allPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            pm.getInstalledPackages(flags)
        }
            .filter { info ->
                val appInfo = info.applicationInfo
                appInfo != null && (showSystemAppsFlag || isUserInstalled(appInfo))
            }
            .map { info ->
                val label = info.applicationInfo?.loadLabel(pm)?.toString().orEmpty()
                val scanResult = existingResults[info.packageName]
                    ?: persisted[info.packageName]
                        ?.takeIf { it.lastUpdateTime == info.lastUpdateTime }
                        ?.toAppScanResult()
                AppEntry(info, label, scanResult)
            }
            .sortedBy { it.label.lowercase() }
        return allPackages
    }

    private fun buildSummary(entries: List<AppEntry>): ScanSummary {
        var safe = 0
        var warning = 0
        var risk = 0
        var scanned = false
        for (entry in entries) {
            when (entry.scanResult?.verdict) {
                RiskVerdict.SAFE -> safe++
                RiskVerdict.WARNING -> warning++
                RiskVerdict.RISK -> risk++
                else -> Unit
            }
            if (entry.scanResult != null) {
                scanned = true
            }
        }
        return ScanSummary(
            total = entries.size,
            safe = safe,
            warning = warning,
            risk = risk,
            lastScanAt = if (scanned) lastScanAt else null,
            isScanned = scanned
        )
    }

    private fun hashApk(entry: AppEntry): List<String> {
        val info = entry.packageInfo.applicationInfo ?: return emptyList()
        val lastUpdate = entry.packageInfo.lastUpdateTime
        val cacheKey = "${entry.packageInfo.packageName}|$lastUpdate"
        val cached = hashCache[cacheKey]
        if (cached != null) return cached
        val paths = ArrayList<String>()
        info.sourceDir?.let { paths.add(it) }
        info.splitSourceDirs?.let { paths.addAll(it) }
        val hashes = runCatching { ScanUtils.apkSha256(paths) }.getOrDefault(emptyList())
        hashCache[cacheKey] = hashes
        return hashes
    }

    private fun isSideloaded(context: Application, packageName: String): Boolean {
        val installer = ScanUtils.installerPackageName(context, packageName)
        return installer == null || installer != PLAY_STORE_PACKAGE
    }

    private fun isOfficialLookalike(label: String, registry: RegistryIndex, packageName: String): Boolean {
        val normalized = ScanUtils.normalizeName(label)
        if (normalized.length < 4) return false
        val key = ScanUtils.nameKey(normalized)
        val candidates = registry.nameIndex[key].orEmpty()
        for (candidate in candidates) {
            if (candidate.packageName == packageName) continue
            if (ScanUtils.isSimilarName(normalized, candidate.normalizedName)) {
                return true
            }
        }
        return false
    }

    private fun detectLocalClones(
        normalizedInstalled: Map<String, String>,
        officialPackages: Set<String>
    ): Set<String> {
        val packages = normalizedInstalled.keys.toList()
        val clones = HashSet<String>()
        for (i in packages.indices) {
            val pkgA = packages[i]
            val nameA = normalizedInstalled[pkgA].orEmpty()
            if (nameA.length < 4) continue
            for (j in i + 1 until packages.size) {
                val pkgB = packages[j]
                val nameB = normalizedInstalled[pkgB].orEmpty()
                if (nameB.length < 4) continue
                if (!ScanUtils.isSimilarName(nameA, nameB)) continue
                val aOfficial = pkgA in officialPackages
                val bOfficial = pkgB in officialPackages
                if (aOfficial && bOfficial) continue
                if (aOfficial) {
                    clones.add(pkgB)
                } else if (bOfficial) {
                    clones.add(pkgA)
                } else {
                    clones.add(pkgA)
                    clones.add(pkgB)
                }
            }
        }
        return clones
    }

    private fun badgeLabel(context: Application, type: BadgeType): String {
        val resId = when (type) {
            BadgeType.SIGNATURE_MISMATCH -> R.string.badge_signature_mismatch
            BadgeType.LOOKALIKE_NAME -> R.string.badge_lookalike_name
            BadgeType.OUTDATED -> R.string.badge_outdated
            BadgeType.SIDELOADED -> R.string.badge_sideloaded
            BadgeType.DEBUGGABLE -> R.string.badge_debuggable
            BadgeType.OLD_TARGET_SDK -> R.string.badge_old_target_sdk
            BadgeType.HIGH_RISK_PERMISSION -> R.string.badge_high_risk_permission
        }
        return context.getString(resId)
    }

    private fun grantedHighRiskPermissions(
        info: android.content.pm.PackageInfo,
        packageName: String,
        label: String
    ): List<String> {
        val requested = info.requestedPermissions ?: return emptyList()
        val flags = info.requestedPermissionsFlags
        val allowed = ScanUtils.allowedHighRiskPermissions(packageName, label)
        val results = ArrayList<String>()
        for (i in requested.indices) {
            val perm = requested[i]
            if (perm !in ScanUtils.highRiskPermissions) continue
            if (perm in allowed) continue
            val granted = if (flags != null && flags.size > i) {
                (flags[i] and android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            } else {
                true
            }
            if (granted) {
                results.add(perm)
            }
        }
        return results
    }

    private fun buildHighRiskDescription(context: Application, permissions: List<String>): String {
        val lines = permissions.map { ScanUtils.permissionDisplayName(it) }
        return context.getString(R.string.badge_desc_high_risk_permission) + "\n" + lines.joinToString("\n")
    }

    private fun persistScans(scans: List<AppEntry>) {
        val file = File(getApplication<Application>().filesDir, SCAN_RESULTS_FILE)
        val entries = JSONArray()
        for (entry in scans) {
            val scan = entry.scanResult ?: continue
            val obj = JSONObject()
            obj.put("package", entry.packageInfo.packageName)
            obj.put("lastUpdateTime", entry.packageInfo.lastUpdateTime)
            obj.put("score", scan.score)
            obj.put("verdict", scan.verdict.name)
            val badges = JSONArray()
            for (badge in scan.badges) {
                val badgeObj = JSONObject()
                badgeObj.put("type", badge.type.name)
                badgeObj.put("label", badge.label)
                badge.description?.let { badgeObj.put("description", it) }
                badges.put(badgeObj)
            }
            obj.put("badges", badges)
            entries.put(obj)
        }
        val root = JSONObject()
        root.put("scans", entries)
        runCatching { file.writeText(root.toString()) }
        persistedScans = loadPersistedScans()
    }

    private fun loadPersistedScans(): Map<String, PersistedScan> {
        val file = File(getApplication<Application>().filesDir, SCAN_RESULTS_FILE)
        if (!file.exists()) return emptyMap()
        return runCatching {
            val root = JSONObject(file.readText())
            val scans = root.optJSONArray("scans") ?: return@runCatching emptyMap()
            val result = HashMap<String, PersistedScan>()
            for (i in 0 until scans.length()) {
                val obj = scans.optJSONObject(i) ?: continue
                val pkg = obj.optString("package")
                if (pkg.isBlank()) continue
                val lastUpdate = obj.optLong("lastUpdateTime", 0L)
                val score = obj.optInt("score", 0)
                val verdictName = obj.optString("verdict", RiskVerdict.SAFE.name)
                val verdict = runCatching { RiskVerdict.valueOf(verdictName) }.getOrDefault(RiskVerdict.SAFE)
                val badgesArray = obj.optJSONArray("badges")
                val badges = ArrayList<Badge>()
                if (badgesArray != null) {
                    for (j in 0 until badgesArray.length()) {
                        val badgeObj = badgesArray.optJSONObject(j) ?: continue
                        val typeName = badgeObj.optString("type")
                        val type = runCatching { BadgeType.valueOf(typeName) }.getOrNull() ?: continue
                        val label = badgeObj.optString("label")
                        val description = badgeObj.optString("description", null)
                        badges.add(Badge(type, label, description))
                    }
                }
                result[pkg] = PersistedScan(
                    score = score,
                    verdict = verdict,
                    badges = badges,
                    lastUpdateTime = lastUpdate
                )
            }
            result
        }.getOrDefault(emptyMap())
    }

    private fun isUserInstalled(appInfo: ApplicationInfo): Boolean {
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return !isSystem || isUpdatedSystem
    }

    private companion object {
        private const val REGISTRY_ASSET_NAME = "official_registry.jsonl"
        private const val PLAY_STORE_PACKAGE = "com.android.vending"
        private const val HIGH_RISK_PERMISSION_SCORE_PERM = 5
        private const val PREFS_NAME = "deceptive_scan_prefs"
        private const val KEY_LAST_SCAN_AT = "last_scan_at"
        private const val KEY_LAST_SCAN_SAFE = "last_scan_safe"
        private const val KEY_LAST_SCAN_WARNING = "last_scan_warning"
        private const val KEY_LAST_SCAN_RISK = "last_scan_risk"
        private const val KEY_LAST_SCAN_TOTAL = "last_scan_total"
        private const val SCAN_RESULTS_FILE = "scan_results.json"
    }
}

private data class PersistedScan(
    val score: Int,
    val verdict: RiskVerdict,
    val badges: List<Badge>,
    val lastUpdateTime: Long
) {
    fun toAppScanResult(): AppScanResult {
        return AppScanResult(
            score = score,
            verdict = verdict,
            badges = badges,
            apkSha256 = emptyList()
        )
    }
}
