package com.resistine.android.ui.apps

import android.app.Application
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.resistine.android.ui.integrity.IntegrityCheckStatus
import com.resistine.android.ui.integrity.IntegrityUiState
import com.resistine.android.ui.integrity.PlayIntegrityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppInventoryRepository(application)
    private val scanner = AppScanEngine(application)
    private val integrityRepository = PlayIntegrityRepository(application)
    private val prefs = application.getSharedPreferences(PREFS_NAME, 0)

    private val _apps = MutableLiveData<List<AppEntry>>(emptyList())
    val apps: LiveData<List<AppEntry>> = _apps

    private val _scanSummary = MutableLiveData(emptySummary())
    val scanSummary: LiveData<ScanSummary> = _scanSummary

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _scanProgress = MutableLiveData<Int?>(null)

    private val _showSystemApps = MutableLiveData(false)
    val showSystemApps: LiveData<Boolean> = _showSystemApps

    private val _integrityState = MutableLiveData(integrityRepository.initialState())
    val integrityState: LiveData<IntegrityUiState> = _integrityState

    val uiState: LiveData<AppsUiState> = MediatorLiveData<AppsUiState>().apply {
        fun publish() {
            value = AppsUiState(
                apps = _apps.value.orEmpty(),
                summary = _scanSummary.value ?: emptySummary(),
                isScanning = _isScanning.value == true,
                scanProgress = _scanProgress.value,
                showSystemApps = _showSystemApps.value == true
            )
        }
        addSource(_apps) { publish() }
        addSource(_scanSummary) { publish() }
        addSource(_isScanning) { publish() }
        addSource(_scanProgress) { publish() }
        addSource(_showSystemApps) { publish() }
    }

    private var scanJob: Job? = null
    private var inventoryJob: Job? = null
    private var showSystemAppsFlag = false
    private var packageReceiverRegistered = false
    private var packageReconcilePending = false
    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (scanJob?.isActive == true) {
                packageReconcilePending = true
            } else {
                loadInventory(scanChangedPackages = _scanSummary.value?.isScanned == true)
            }
        }
    }

    init {
        registerPackageChangeReceiver()
        loadInventory()
    }

    fun setShowSystemApps(enabled: Boolean) {
        if (showSystemAppsFlag == enabled) return
        scanJob?.cancel()
        scanJob = null
        showSystemAppsFlag = enabled
        _showSystemApps.value = enabled
        loadInventory()
    }

    fun runIntegrityCheck() {
        if (_integrityState.value?.isRunning == true) return
        _integrityState.value = IntegrityUiState(
            status = IntegrityCheckStatus.RUNNING,
            message = "Requesting a Play Integrity token and verifying it on the configured server…"
        )
        viewModelScope.launch {
            _integrityState.value = integrityRepository.check()
        }
    }

    suspend fun showIntegrityRemediation(activity: Activity): Result<Int> {
        return runCatching { integrityRepository.showRemediation(activity) }
    }

    fun startScan() {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            _isScanning.postValue(true)
            _scanProgress.postValue(0)
            try {
                val startedAt = SystemClock.elapsedRealtime()
                val current = repository.loadInstalledApps(
                    showSystemAppsFlag,
                    repository.loadPersistedScans()
                )
                val capabilitySnapshot = ScanUtils.captureDeviceCapabilities(getApplication())
                val now = System.currentTimeMillis()
                val scanned = scanEntries(current, capabilitySnapshot, now, publishProgress = true)
                repository.persistScans(scanned)
                prefs.edit().putLong(KEY_LAST_SCAN_AT, now).apply()
                _apps.postValue(scanned)
                val duration = SystemClock.elapsedRealtime() - startedAt
                persistMetrics(duration, scanned.size, inventoryReloaded = true)
                _scanSummary.postValue(
                    buildSummary(scanned, now, duration, scanned.size, inventoryReloaded = true)
                )
            } finally {
                _scanProgress.postValue(null)
                _isScanning.postValue(false)
                reconcilePackagesAfterScanIfNeeded()
            }
        }
    }

    fun refreshCapabilityState() {
        val current = _apps.value.orEmpty()
        if (current.none { it.scanResult != null } || scanJob?.isActive == true) return
        scanJob = viewModelScope.launch(Dispatchers.Default) {
            _isScanning.postValue(true)
            try {
                val startedAt = SystemClock.elapsedRealtime()
                val now = System.currentTimeMillis()
                val capabilities = ScanUtils.captureDeviceCapabilities(getApplication())
                val refreshed = scanEntries(
                    current,
                    capabilities,
                    now,
                    publishProgress = false,
                    reuseInstallProvenance = true
                )
                repository.persistScans(refreshed)
                prefs.edit().putLong(KEY_LAST_SCAN_AT, now).apply()
                val duration = SystemClock.elapsedRealtime() - startedAt
                persistMetrics(duration, refreshed.size, inventoryReloaded = false)
                _apps.postValue(refreshed)
                _scanSummary.postValue(
                    buildSummary(refreshed, now, duration, refreshed.size, inventoryReloaded = false)
                )
            } finally {
                _isScanning.postValue(false)
                reconcilePackagesAfterScanIfNeeded()
            }
        }
    }

    private fun loadInventory(scanChangedPackages: Boolean = false) {
        inventoryJob?.cancel()
        inventoryJob = viewModelScope.launch(Dispatchers.IO) {
            val persisted = repository.loadPersistedScans()
            var entries = repository.loadInstalledApps(showSystemAppsFlag, persisted)
            val lastScanAt = prefs.getLong(KEY_LAST_SCAN_AT, 0L).takeIf { it > 0L }
            if (scanChangedPackages) {
                val changed = entries.filter { it.scanResult == null }
                if (changed.isNotEmpty()) {
                    val capabilities = ScanUtils.captureDeviceCapabilities(getApplication())
                    val now = System.currentTimeMillis()
                    val scannedChanged = scanEntries(changed, capabilities, now, publishProgress = false)
                        .associateBy { it.packageInfo.packageName }
                    entries = entries.map { scannedChanged[it.packageInfo.packageName] ?: it }
                    repository.persistScans(entries)
                }
            }
            _apps.postValue(entries)
            _scanSummary.postValue(buildSummary(entries, lastScanAt))
        }
    }

    private suspend fun scanEntries(
        entries: List<AppEntry>,
        capabilities: ScanUtils.DeviceCapabilitySnapshot,
        now: Long,
        publishProgress: Boolean,
        reuseInstallProvenance: Boolean = false
    ): List<AppEntry> {
        val completed = AtomicInteger(0)
        val semaphore = Semaphore(SCAN_CONCURRENCY)
        return coroutineScope {
            entries.map { entry ->
                async(Dispatchers.Default) {
                    semaphore.withPermit {
                        scanner.scan(entry, capabilities, now, reuseInstallProvenance)
                    }.also {
                        if (publishProgress) {
                            val finished = completed.incrementAndGet()
                            _scanProgress.postValue(
                                if (entries.isEmpty()) 100 else (finished * 100) / entries.size
                            )
                        }
                    }
                }
            }.awaitAll()
        }.sortedBy { it.label.lowercase() }
    }

    private fun registerPackageChangeReceiver() {
        val app = getApplication<Application>()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        packageReceiverRegistered = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                app.registerReceiver(packageChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                app.registerReceiver(packageChangeReceiver, filter)
            }
        }.isSuccess
    }

    private fun reconcilePackagesAfterScanIfNeeded() {
        if (!packageReconcilePending) return
        packageReconcilePending = false
        loadInventory(scanChangedPackages = true)
    }

    override fun onCleared() {
        if (packageReceiverRegistered) {
            runCatching { getApplication<Application>().unregisterReceiver(packageChangeReceiver) }
        }
        super.onCleared()
    }

    private fun buildSummary(
        entries: List<AppEntry>,
        lastScanAt: Long?,
        durationMillis: Long? = prefs.getLong(KEY_LAST_SCAN_DURATION, -1L).takeIf { it >= 0L },
        evaluatedCount: Int = prefs.getInt(KEY_LAST_SCAN_COUNT, 0),
        inventoryReloaded: Boolean = prefs.getBoolean(KEY_LAST_SCAN_RELOADED, false)
    ): ScanSummary {
        val scanned = entries.filter { it.scanResult != null }
        return ScanSummary(
            total = entries.size,
            noConcern = scanned.count { it.scanResult?.verdict == RiskVerdict.NO_CONCERN },
            review = scanned.count { it.scanResult?.verdict == RiskVerdict.REVIEW },
            urgentReview = scanned.count { it.scanResult?.verdict == RiskVerdict.URGENT_REVIEW },
            lastScanAt = lastScanAt,
            isScanned = scanned.isNotEmpty(),
            durationMillis = durationMillis,
            evaluatedCount = evaluatedCount,
            inventoryReloaded = inventoryReloaded
        )
    }

    private fun persistMetrics(durationMillis: Long, evaluatedCount: Int, inventoryReloaded: Boolean) {
        prefs.edit()
            .putLong(KEY_LAST_SCAN_DURATION, durationMillis)
            .putInt(KEY_LAST_SCAN_COUNT, evaluatedCount)
            .putBoolean(KEY_LAST_SCAN_RELOADED, inventoryReloaded)
            .apply()
    }

    private companion object {
        private const val PREFS_NAME = "app_posture_prefs"
        private const val KEY_LAST_SCAN_AT = "last_scan_at"
        private const val KEY_LAST_SCAN_DURATION = "last_scan_duration"
        private const val KEY_LAST_SCAN_COUNT = "last_scan_count"
        private const val KEY_LAST_SCAN_RELOADED = "last_scan_reloaded"
        private const val SCAN_CONCURRENCY = 4

        fun emptySummary() = ScanSummary(
            total = 0,
            noConcern = 0,
            review = 0,
            urgentReview = 0,
            lastScanAt = null,
            isScanned = false
        )
    }
}
