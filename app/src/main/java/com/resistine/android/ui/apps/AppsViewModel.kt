package com.resistine.android.ui.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
    private var showSystemAppsFlag = false

    init {
        loadInventory()
    }

    fun setShowSystemApps(enabled: Boolean) {
        if (showSystemAppsFlag == enabled) return
        showSystemAppsFlag = enabled
        _showSystemApps.value = enabled
        loadInventory()
    }

    fun startScan() {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            _isScanning.postValue(true)
            _scanProgress.postValue(0)
            try {
                val current = repository.loadInstalledApps(
                    showSystemAppsFlag,
                    repository.loadPersistedScans()
                )
                val capabilitySnapshot = ScanUtils.captureDeviceCapabilities(getApplication())
                val now = System.currentTimeMillis()
                val completed = AtomicInteger(0)
                val semaphore = Semaphore(SCAN_CONCURRENCY)
                val scanned = coroutineScope {
                    current.map { entry ->
                        async(Dispatchers.Default) {
                            semaphore.withPermit {
                                scanner.scan(entry, capabilitySnapshot, now)
                            }.also {
                                val finished = completed.incrementAndGet()
                                _scanProgress.postValue(
                                    if (current.isEmpty()) 100 else (finished * 100) / current.size
                                )
                            }
                        }
                    }.awaitAll()
                }.sortedBy { it.label.lowercase() }
                repository.persistScans(scanned)
                prefs.edit().putLong(KEY_LAST_SCAN_AT, now).apply()
                _apps.postValue(scanned)
                _scanSummary.postValue(buildSummary(scanned, now))
            } finally {
                _scanProgress.postValue(null)
                _isScanning.postValue(false)
            }
        }
    }

    fun refreshCapabilityState() {
        val current = _apps.value.orEmpty()
        if (current.none { it.scanResult != null } || scanJob?.isActive == true) return
        startScan()
    }

    private fun loadInventory() {
        viewModelScope.launch(Dispatchers.IO) {
            val persisted = repository.loadPersistedScans()
            val entries = repository.loadInstalledApps(showSystemAppsFlag, persisted)
            val lastScanAt = prefs.getLong(KEY_LAST_SCAN_AT, 0L).takeIf { it > 0L }
            _apps.postValue(entries)
            _scanSummary.postValue(buildSummary(entries, lastScanAt))
        }
    }

    private fun buildSummary(entries: List<AppEntry>, lastScanAt: Long?): ScanSummary {
        val scanned = entries.filter { it.scanResult != null }
        return ScanSummary(
            total = entries.size,
            noConcern = scanned.count { it.scanResult?.verdict == RiskVerdict.NO_CONCERN },
            review = scanned.count { it.scanResult?.verdict == RiskVerdict.REVIEW },
            urgentReview = scanned.count { it.scanResult?.verdict == RiskVerdict.URGENT_REVIEW },
            lastScanAt = lastScanAt,
            isScanned = scanned.isNotEmpty()
        )
    }

    private companion object {
        private const val PREFS_NAME = "app_posture_prefs"
        private const val KEY_LAST_SCAN_AT = "last_scan_at"
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
