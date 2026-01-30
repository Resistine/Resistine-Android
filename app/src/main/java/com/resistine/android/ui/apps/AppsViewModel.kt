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

class AppsViewModel(application: Application) : AndroidViewModel(application) {

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

    init {
        loadAllPackages()
    }

    fun setShowSystemApps(enabled: Boolean) {
        if (showSystemAppsFlag == enabled) return
        showSystemAppsFlag = enabled
        _showSystemApps.postValue(enabled)
        loadAllPackages(resetSummary = true)
    }

    private fun loadAllPackages(resetSummary: Boolean = true) {
        viewModelScope.launch(Dispatchers.Default) {
            val allPackages = loadInstalledApps()

            _apps.postValue(allPackages)
            if (resetSummary) {
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

                val scanned = currentApps.map { entry ->
                    val pkg = entry.packageInfo.packageName
                    val appInfo = entry.packageInfo.applicationInfo
                    val badges = ArrayList<Badge>()
                    var score = 0

                    val registryEntry = registry.byPackage[pkg]
                    val versionCode = entry.packageInfo.longVersionCodeCompat()

                    val isSideloaded = isSideloaded(appContext, pkg)
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

                    val requestedPermissions = entry.packageInfo.requestedPermissions?.toSet().orEmpty()
                    if (requestedPermissions.any { it in ScanUtils.highRiskPermissions }) {
                        badges.add(Badge(BadgeType.HIGH_RISK_PERMISSION, badgeLabel(appContext, BadgeType.HIGH_RISK_PERMISSION)))
                        score += 10
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

                val isSystemApp = appInfo != null && !isUserInstalled(appInfo)
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

                    entry.copy(
                        scanResult = AppScanResult(
                            score = score,
                            verdict = verdict,
                            badges = badges,
                            apkSha256 = apkHashes
                        )
                    )
                }.sortedBy { it.label.lowercase() }

                val summary = buildSummary(scanned)
                _apps.postValue(scanned)
                _scanSummary.postValue(summary)
            } finally {
                _isScanning.postValue(false)
            }
        }
    }

    private fun loadInstalledApps(): List<AppEntry> {
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
                AppEntry(info, label)
            }
            .sortedBy { it.label.lowercase() }
        return allPackages
    }

    private fun buildSummary(entries: List<AppEntry>): ScanSummary {
        var safe = 0
        var warning = 0
        var risk = 0
        for (entry in entries) {
            when (entry.scanResult?.verdict) {
                RiskVerdict.SAFE -> safe++
                RiskVerdict.WARNING -> warning++
                RiskVerdict.RISK -> risk++
                else -> Unit
            }
        }
        return ScanSummary(
            total = entries.size,
            safe = safe,
            warning = warning,
            risk = risk,
            lastScanAt = System.currentTimeMillis(),
            isScanned = true
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

    private fun isUserInstalled(appInfo: ApplicationInfo): Boolean {
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        return !isSystem || isUpdatedSystem
    }

    private companion object {
        private const val REGISTRY_ASSET_NAME = "official_registry.jsonl"
        private const val PLAY_STORE_PACKAGE = "com.android.vending"
    }
}

