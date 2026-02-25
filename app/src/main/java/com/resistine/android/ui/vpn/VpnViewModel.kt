package com.resistine.android.ui.vpn

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.resistine.android.R
import com.resistine.android.security.CryptoManager
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.Tunnel.State
import com.wireguard.config.Config
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private var backend: GoBackend? = null
    private var tunnel: Tunnel? = null
    private val tunnelName = "MyWireGuardTunnel"

    private val _vpnStatus = MutableLiveData<String>()
    val vpnStatus: LiveData<String> = _vpnStatus
    private val _isVpnConnected = MutableLiveData(false)
    val isVpnConnectedLiveData: LiveData<Boolean> = _isVpnConnected

    private var isVpnConnected = false

    private val _ipAddress = MutableLiveData<String>()
    val ipAddress: LiveData<String> = _ipAddress

    private val _androidVersion = MutableLiveData<String>()
    val androidVersion: LiveData<String> = _androidVersion

    private val _batteryLevel = MutableLiveData<String>()
    val batteryLevel: LiveData<String> = _batteryLevel

    private val _deviceModel = MutableLiveData<String>()
    val deviceModel: LiveData<String> = _deviceModel

    private val _locationString = MutableLiveData<String>()
    val locationString: LiveData<String> = _locationString

    private val _wifiSecurityAlert = MutableLiveData<WifiSecurityAlert>()
    val wifiSecurityAlert: LiveData<WifiSecurityAlert> = _wifiSecurityAlert
    private val _nearbyWifiNetworksState = MutableLiveData(WifiNearbyNetworksState())
    val nearbyWifiNetworksState: LiveData<WifiNearbyNetworksState> = _nearbyWifiNetworksState
    private val _trustedWifiNetworks = MutableLiveData<List<TrustedWifiProfile>>(emptyList())
    val trustedWifiNetworks: LiveData<List<TrustedWifiProfile>> = _trustedWifiNetworks
    private val _wifiSafetyAssessment = MutableLiveData(WifiSafetyAssessment())
    val wifiSafetyAssessment: LiveData<WifiSafetyAssessment> = _wifiSafetyAssessment
    private val _wifiAdvancedChecks = MutableLiveData<List<WifiAdvancedCheckItem>>(emptyList())
    val wifiAdvancedChecks: LiveData<List<WifiAdvancedCheckItem>> = _wifiAdvancedChecks
    private val _wifiRiskTransitionAlert = MutableLiveData<WifiRiskTransitionAlert?>()
    val wifiRiskTransitionAlert: LiveData<WifiRiskTransitionAlert?> = _wifiRiskTransitionAlert
    private val _autoVpnPolicy = MutableLiveData(AutoVpnPolicy.OFF)
    val autoVpnPolicy: LiveData<AutoVpnPolicy> = _autoVpnPolicy
    private val _autoVpnActionMessageRes = MutableLiveData<Int?>()
    val autoVpnActionMessageRes: LiveData<Int?> = _autoVpnActionMessageRes
    private val _backgroundScanState = MutableLiveData(WifiBackgroundScanState())
    val backgroundScanState: LiveData<WifiBackgroundScanState> = _backgroundScanState
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isWifiMonitoringStarted = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var backgroundScanRunnable: Runnable? = null
    private var lastBackgroundScanRunMillis: Long? = null
    private var lastRiskLevel: WifiNetworkRiskLevel? = null
    private var lastAutoVpnRiskBand: Boolean? = null
    private val trustPrefs: SharedPreferences =
        application.getSharedPreferences(WIFI_TRUST_PREFS, Context.MODE_PRIVATE)
    private var currentWifiSsid: String? = null
    private var currentWifiBssid: String? = null
    private var currentWifiSecurityType: WifiSecurityType? = null

    init {
        loadPhoneInfo()
        fetchLocationData()
        _autoVpnPolicy.value = loadAutoVpnPolicy()
        createRiskNotificationChannelIfNeeded()
        updateBackgroundScanState()
        if (loadBackgroundScanEnabled()) {
            startWifiMonitoring()
        } else {
            refreshWifiSecurityAlert()
        }
    }

    fun logout(context: Context) {
        if (isVpnConnected) {
            disconnectVpn()
        }
        CryptoManager.deleteStoredData(context)
    }

    fun toggleVpn(context: Context) {
        if (isVpnConnected) {
            disconnectVpn()
        } else {
            connectVpn(context)
        }
    }

    fun connectVpnIfNeeded(context: Context) {
        if (!isVpnConnected) {
            connectVpn(context)
        }
    }

    fun disconnectVpnIfConnected() {
        if (isVpnConnected) {
            disconnectVpn()
        }
    }

    fun setAutoVpnEnabled(enabled: Boolean) {
        val current = _autoVpnPolicy.value ?: AutoVpnPolicy.OFF
        val updated = when {
            enabled && current == AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE ->
                AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE

            enabled -> AutoVpnPolicy.CONNECT_ON_RISK
            else -> AutoVpnPolicy.OFF
        }
        saveAutoVpnPolicy(updated)
        _autoVpnPolicy.postValue(updated)
        if (updated == AutoVpnPolicy.OFF) {
            lastAutoVpnRiskBand = null
        } else {
            _wifiSafetyAssessment.value?.let { applyAutoVpnPolicy(it) }
        }
    }

    fun setAutoVpnDisconnectOnSafe(enabled: Boolean) {
        val current = _autoVpnPolicy.value ?: AutoVpnPolicy.OFF
        val updated = when {
            !enabled && current != AutoVpnPolicy.OFF -> AutoVpnPolicy.CONNECT_ON_RISK
            enabled && current != AutoVpnPolicy.OFF -> AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE
            else -> AutoVpnPolicy.OFF
        }
        saveAutoVpnPolicy(updated)
        _autoVpnPolicy.postValue(updated)
        if (updated != AutoVpnPolicy.OFF) {
            _wifiSafetyAssessment.value?.let { applyAutoVpnPolicy(it) }
        }
    }

    fun setBackgroundScanEnabled(enabled: Boolean) {
        saveBackgroundScanEnabled(enabled)
        if (enabled) {
            startBackgroundScanSchedulerIfNeeded()
        } else {
            stopBackgroundScanScheduler()
        }
        updateBackgroundScanState()
    }

    fun clearRiskTransitionAlert() {
        _wifiRiskTransitionAlert.postValue(null)
    }

    fun clearAutoVpnActionMessage() {
        _autoVpnActionMessageRes.postValue(null)
    }

    fun refreshWifiSecurityAlert(lightweight: Boolean = false) {
        val alert = buildWifiSecurityAlert()
        val nearbyState = buildNearbyWifiNetworksState(
            currentAlert = alert,
            requestFreshScan = !lightweight
        )
        val checks = buildAdvancedChecks(alert, nearbyState)
        val assessment = buildSafetyAssessment(alert, checks)
        val previousRiskLevel = lastRiskLevel
        maybeUpdateTrustedBaseline(alert, nearbyState)
        _wifiSecurityAlert.postValue(alert)
        _nearbyWifiNetworksState.postValue(nearbyState)
        _trustedWifiNetworks.postValue(loadTrustedProfiles().sortedBy { it.ssid.lowercase() })
        _wifiAdvancedChecks.postValue(checks)
        _wifiSafetyAssessment.postValue(assessment)
        emitRiskTransitionIfNeeded(previousRiskLevel, assessment)
        applyAutoVpnPolicy(assessment)
        lastRiskLevel = assessment.level
        if (lightweight) {
            lastBackgroundScanRunMillis = currentTimeMillis()
            updateBackgroundScanState()
        }
    }

    fun toggleCurrentNetworkTrusted(): Int {
        val ssid = currentWifiSsid ?: return R.string.wifi_trust_unavailable
        return if (getTrustedProfile(ssid) == null) {
            saveTrustedProfile(
                ssid = ssid,
                bssid = currentWifiBssid,
                securityType = currentWifiSecurityType ?: WifiSecurityType.UNKNOWN,
                knownBssids = mergeKnownBssids(emptySet(), currentWifiBssid)
            )
            refreshWifiSecurityAlert()
            R.string.wifi_trust_added
        } else {
            removeTrustedProfile(ssid)
            refreshWifiSecurityAlert()
            R.string.wifi_trust_removed
        }
    }

    fun removeTrustedNetwork(ssid: String): Int {
        if (ssid.isBlank()) return R.string.wifi_trust_unavailable
        removeTrustedProfile(ssid)
        refreshWifiSecurityAlert()
        return R.string.wifi_trust_removed
    }

    fun toggleTrustedNetwork(
        ssid: String,
        bssid: String?,
        securityType: WifiSecurityType,
        currentlyTrusted: Boolean
    ): Int {
        if (ssid.isBlank()) return R.string.wifi_trust_unavailable
        if (currentlyTrusted) {
            removeTrustedProfile(ssid)
            refreshWifiSecurityAlert()
            return R.string.wifi_trust_removed
        }
        saveTrustedProfile(
            ssid = ssid,
            bssid = bssid,
            securityType = securityType,
            knownBssids = mergeKnownBssids(emptySet(), bssid)
        )
        refreshWifiSecurityAlert()
        return R.string.wifi_trust_added
    }

    fun startWifiMonitoring() {
        if (isWifiMonitoringStarted) {
            return
        }
        val context = getApplication<Application>()
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java) ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = refreshWifiSecurityAlert()

            override fun onLost(network: Network) = refreshWifiSecurityAlert()

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                refreshWifiSecurityAlert()
            }

            override fun onUnavailable() = refreshWifiSecurityAlert()
        }
        try {
            connectivityManager.registerDefaultNetworkCallback(callback)
            networkCallback = callback
            isWifiMonitoringStarted = true
            startBackgroundScanSchedulerIfNeeded()
        } catch (_: Exception) {
            networkCallback = null
            isWifiMonitoringStarted = false
        }
        refreshWifiSecurityAlert()
    }

    fun stopWifiMonitoring() {
        if (!isWifiMonitoringStarted) {
            return
        }
        val context = getApplication<Application>()
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val callback = networkCallback
        if (connectivityManager != null && callback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: Exception) {
                // Ignore stale callback unregister exceptions.
            }
        }
        networkCallback = null
        isWifiMonitoringStarted = false
        stopBackgroundScanScheduler()
        updateBackgroundScanState()
    }

    private fun connectVpn(context: Context) {
        if (backend == null) {
            backend = GoBackend(context.applicationContext)
        }
        val config = loadWireGuardConfig(context) ?: run {
            _vpnStatus.value = "Error: Configuration not found"
            return
        }
        if (tunnel == null) {
            tunnel = object : Tunnel {
                override fun getName() = tunnelName
                override fun onStateChange(state: State) {
                    _vpnStatus.postValue("VPN state: $state")
                }
            }
        }
        try {
            backend?.setState(tunnel!!, State.UP, config)
            isVpnConnected = true
            _isVpnConnected.postValue(true)
            _vpnStatus.postValue("VPN connected")
        } catch (e: Exception) {
            val reason = e::class.java.getDeclaredField("reason").apply { isAccessible = true }.get(e)?.toString()
            if (reason?.contains("UNABLE_TO_START_VPN") == true) {
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        backend?.setState(tunnel!!, State.UP, config)
                        isVpnConnected = true
                        _isVpnConnected.postValue(true)
                        _vpnStatus.postValue("VPN connected")
                    } catch (retryException: Exception) {
                        retryException.printStackTrace()
                        val retryReason = retryException::class.java.getDeclaredField("reason").apply { isAccessible = true }
                            .get(retryException)?.toString()
                        _vpnStatus.postValue("Error connecting VPN: ${retryReason ?: retryException.message ?: "Unknown error"}")
                    }
                }, 500)
            } else {
                _vpnStatus.postValue("Error connecting VPN: ${reason ?: e.message ?: "Unknown error"}")
            }
        }
    }

    private fun disconnectVpn() {
        try {
            tunnel?.let {
                backend?.setState(it, State.DOWN, null)
                isVpnConnected = false
                _isVpnConnected.postValue(false)
                _vpnStatus.postValue("VPN disconnected")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _vpnStatus.postValue("Error disconnecting VPN: ${e.message}")
        }
    }

    private fun loadWireGuardConfig(context: Context): Config? {
        return try {
            val encryptedConfig = CryptoManager.loadEncryptedConfig(context)

            if (encryptedConfig.isNullOrBlank()) {
                println("Failed to load configuration - no saved configuration found.")
                return null
            }

            val decryptedText = CryptoManager.decryptData(encryptedConfig)
            val inputStream = decryptedText.byteInputStream(Charsets.UTF_8)
            Config.parse(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadPhoneInfo() {
        _androidVersion.value = "Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
        _batteryLevel.value = "Battery Level: ${getBatteryLevel(getApplication())}%"
        _deviceModel.value = "Device: ${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun fetchLocationData() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://ipwho.is")
            .build()

        _ipAddress.postValue("Address: Fetching...")
        _locationString.postValue("Location: Fetching...")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _locationString.postValue("Location fetch error: ${e.message}")
                _ipAddress.postValue("Address: Fetch error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        _locationString.postValue("Location fetch failed")
                        _ipAddress.postValue("Address: Fetch error")
                        return
                    }

                    val json = response.body?.string()
                    try {
                        val obj = JSONObject(json!!)
                        val region = obj.optString("regionName")
                        val country = obj.optString("country")
                        val lat = obj.optDouble("lat")
                        val lon = obj.optDouble("lon")

                        val text = buildString {
                            if (country.isNotEmpty()) append(country)
                            if (region.isNotEmpty()) append(", $region")
                            if (!lat.isNaN() && !lon.isNaN()) append(" (Lat: $lat, Lon: $lon)")
                        }
                        _ipAddress.postValue("Public IP Address: ${obj.optString("ip")}")
                        _locationString.postValue("Location: $text")
                    } catch (e: Exception) {
                        _locationString.postValue("Location parse error: ${e.message}")
                    }
                }
            }
        })
    }

    private fun buildWifiSecurityAlert(): WifiSecurityAlert {
        val context = getApplication<Application>()
        val checkedAtMillis = currentTimeMillis()
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
            ?: return WifiSecurityAlert(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.UNAVAILABLE,
                message = context.getString(R.string.wifi_security_unavailable),
                checkedAtMillis = checkedAtMillis
            )

        val active = connectivity.activeNetwork
            ?: return WifiSecurityAlert(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.NO_NETWORK,
                message = context.getString(R.string.wifi_security_no_network),
                checkedAtMillis = checkedAtMillis
            )

        val caps = connectivity.getNetworkCapabilities(active)
            ?: return WifiSecurityAlert(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.UNAVAILABLE,
                message = context.getString(R.string.wifi_security_unavailable),
                checkedAtMillis = checkedAtMillis
            )

        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasInternetAccess = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val supportsSecurityTypeDetection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val hasLocationAccess = hasLocationPermission(context)
        val isLocationServicesEnabled = isLocationServicesEnabled(context)
        val canInspectWifiDetails = hasLocationAccess && isLocationServicesEnabled
        val wifiInfo = caps.transportInfo as? WifiInfo
        var ssid = normalizeSsid(wifiInfo?.ssid)
        var bssid = normalizeBssid(wifiInfo?.bssid)
        val wifiSecurityType = resolveWifiSecurityType(
            caps = caps,
            supportsSecurityTypeDetection = supportsSecurityTypeDetection,
            hasLocationPermission = canInspectWifiDetails
        )
        if (isWifi && (ssid == null || bssid == null)) {
            val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            val connectionInfo = runCatching { wifiManager?.connectionInfo }.getOrNull()
            if (ssid == null) {
                ssid = normalizeSsid(connectionInfo?.ssid)
            }
            if (bssid == null) {
                bssid = normalizeBssid(connectionInfo?.bssid)
            }
        }
        if (isWifi && ssid == null) {
            ssid = context.getString(R.string.wifi_network_current_unknown_ssid)
        }

        currentWifiSsid = ssid
        currentWifiBssid = bssid
        currentWifiSecurityType = wifiSecurityType

        var classification = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = isWifi,
                hasCaptivePortal = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL),
                isValidated = hasInternetAccess,
                supportsSecurityTypeDetection = supportsSecurityTypeDetection,
                hasLocationPermission = canInspectWifiDetails,
                securityType = wifiSecurityType
            )
        )
        if (isWifi && hasLocationAccess && !isLocationServicesEnabled) {
            classification = classification.copy(
                level = WifiAlertLevel.INFO,
                reason = WifiAlertReason.LOCATION_SERVICES_DISABLED,
                requiresLocationPermission = false
            )
        }

        if (isWifi && canInspectWifiDetails) {
            val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            val currentScanResult = runCatching { wifiManager?.scanResults.orEmpty() }
                .getOrElse { emptyList() }
                .firstOrNull { result ->
                    val scanBssid = normalizeBssid(result.BSSID)
                    val scanSsid = normalizeSsid(result.SSID)
                    when {
                        bssid != null && scanBssid != null -> bssid.equals(scanBssid, ignoreCase = true)
                        ssid != null && scanSsid != null -> ssid.equals(scanSsid, ignoreCase = true)
                        else -> false
                    }
                }
            val currentSignals = securitySignalsFromCapabilities(currentScanResult?.capabilities)
            classification = when {
                currentSignals.hasWeakCipher && classification.reason != WifiAlertReason.OPEN_OR_WEP -> classification.copy(
                    level = WifiAlertLevel.WARNING,
                    reason = WifiAlertReason.WEAK_LEGACY_CIPHER
                )

                currentSignals.hasWps && classification.reason != WifiAlertReason.OPEN_OR_WEP -> classification.copy(
                    level = WifiAlertLevel.WARNING,
                    reason = WifiAlertReason.WPS_ENABLED
                )

                else -> classification
            }
        }

        val trustedProfile = if (isWifi && ssid != null) getTrustedProfile(ssid) else null
        if (trustedProfile != null) {
            classification = when {
                isSecurityDowngrade(
                    trustedType = trustedProfile.securityType,
                    currentType = wifiSecurityType
                ) -> classification.copy(
                    level = WifiAlertLevel.WARNING,
                    reason = WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE
                )

                isTrustedBssidMismatch(trustedProfile, bssid) &&
                    !isBenignTrustedBssidChange(trustedProfile, wifiSecurityType) -> classification.copy(
                    level = WifiAlertLevel.WARNING,
                    reason = WifiAlertReason.TRUSTED_BSSID_MISMATCH
                )

                else -> classification
            }
        }

        return WifiSecurityAlert(
            level = classification.level,
            reason = classification.reason,
            message = context.getString(reasonMessageResId(classification.reason)),
            securityType = wifiSecurityType,
            checkedAtMillis = checkedAtMillis,
            requiresLocationPermission = classification.requiresLocationPermission,
            ssid = ssid,
            bssid = bssid,
            isTrustedNetwork = trustedProfile != null,
            canToggleTrust = isWifi && ssid != null,
            hasInternetAccess = if (isWifi) hasInternetAccess else null
        )
    }

    private fun buildNearbyWifiNetworksState(
        currentAlert: WifiSecurityAlert,
        requestFreshScan: Boolean
    ): WifiNearbyNetworksState {
        val context = getApplication<Application>()
        val trustedBySsid = loadTrustedProfiles().associateBy { it.ssid }
        val currentFallback = buildCurrentWifiFallbackNetwork(currentAlert, trustedBySsid)
        val hasLocationAccess = hasLocationPermission(context)
        val locationServicesEnabled = isLocationServicesEnabled(context)

        if (!hasLocationAccess) {
            return if (currentFallback != null) {
                WifiNearbyNetworksState(
                    networks = listOf(currentFallback),
                    messageResId = R.string.wifi_nearby_showing_current_only
                )
            } else {
                WifiNearbyNetworksState(messageResId = R.string.wifi_nearby_requires_location)
            }
        }
        if (!locationServicesEnabled) {
            return if (currentFallback != null) {
                WifiNearbyNetworksState(
                    networks = listOf(currentFallback),
                    messageResId = R.string.wifi_nearby_enable_location_services
                )
            } else {
                WifiNearbyNetworksState(messageResId = R.string.wifi_nearby_enable_location_services)
            }
        }

        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            ?: return WifiNearbyNetworksState(messageResId = R.string.wifi_nearby_unavailable)

        if (requestFreshScan) {
            // Best-effort scan refresh. If the platform rejects it, keep using cached results.
            runCatching { wifiManager.startScan() }
        }
        val scanResults = runCatching { wifiManager.scanResults }.getOrElse { emptyList() }
        val currentBssid = currentAlert.bssid?.lowercase()
        val deduped = LinkedHashMap<String, android.net.wifi.ScanResult>()
        scanResults.forEach { result ->
            val normalizedSsid = normalizeSsid(result.SSID)
            val normalizedBssid = normalizeBssid(result.BSSID)
            if (normalizedSsid == null && normalizedBssid == null) {
                return@forEach
            }
            val key = normalizedBssid ?: "${normalizedSsid ?: "(hidden)"}|${result.capabilities}"
            val existing = deduped[key]
            if (existing == null || result.level > existing.level) {
                deduped[key] = result
            }
        }

        val networks = deduped.values.map { result ->
            val ssid = normalizeSsid(result.SSID) ?: context.getString(R.string.wifi_network_hidden_ssid)
            val bssid = normalizeBssid(result.BSSID)
            val signals = securitySignalsFromCapabilities(result.capabilities)
            val securityType = signals.securityType
            val trustedProfile = trustedBySsid[ssid]
            val isCurrent = when {
                currentBssid != null && bssid != null -> currentBssid.equals(bssid, ignoreCase = true)
                currentAlert.ssid != null -> currentAlert.ssid.equals(ssid, ignoreCase = true)
                else -> false
            }

            val risk = classifyNearbyNetworkRisk(
                signals = signals,
                trustedProfile = trustedProfile,
                bssid = bssid,
                isCurrent = isCurrent,
                currentAlertReason = currentAlert.reason
            )

            WifiNearbyNetwork(
                ssid = ssid,
                bssid = bssid,
                securityType = securityType,
                riskLevel = risk.level,
                reason = risk.reason,
                isTrusted = trustedProfile != null,
                isCurrent = isCurrent,
                hasInternetAccess = if (isCurrent) currentAlert.hasInternetAccess else null,
                frequencyMhz = result.frequency,
                capabilities = result.capabilities
            )
        }.toMutableList()

        if (currentFallback != null && networks.none { isSameNetwork(it, currentFallback) }) {
            networks += currentFallback
        }

        if (networks.isEmpty()) {
            return WifiNearbyNetworksState(messageResId = R.string.wifi_nearby_none)
        }

        val sorted = networks.sortedWith(
            compareByDescending<WifiNearbyNetwork> { riskSeverity(it.riskLevel) }
                .thenBy { it.ssid.lowercase() }
        )

        return WifiNearbyNetworksState(networks = sorted)
    }

    private fun classifyNearbyNetworkRisk(
        signals: WifiSecuritySignals,
        trustedProfile: TrustedWifiProfile?,
        bssid: String?,
        isCurrent: Boolean,
        currentAlertReason: WifiAlertReason
    ): WifiNearbyRiskResult {
        val securityType = signals.securityType
        var level: WifiNetworkRiskLevel
        var reason: WifiAlertReason
        when (securityType) {
            WifiSecurityType.OPEN,
            WifiSecurityType.WEP -> {
                level = WifiNetworkRiskLevel.DANGER
                reason = WifiAlertReason.OPEN_OR_WEP
            }

            WifiSecurityType.UNKNOWN -> {
                level = WifiNetworkRiskLevel.WARNING
                reason = WifiAlertReason.UNKNOWN_SECURITY
            }

            WifiSecurityType.SECURE -> {
                level = WifiNetworkRiskLevel.SAFE
                reason = WifiAlertReason.SECURE
            }
        }
        if (signals.hasWeakCipher && riskSeverity(WifiNetworkRiskLevel.WARNING) > riskSeverity(level)) {
            level = WifiNetworkRiskLevel.WARNING
            reason = WifiAlertReason.WEAK_LEGACY_CIPHER
        }
        if (signals.hasWps && riskSeverity(WifiNetworkRiskLevel.WARNING) > riskSeverity(level)) {
            level = WifiNetworkRiskLevel.WARNING
            reason = WifiAlertReason.WPS_ENABLED
        }

        if (trustedProfile != null) {
            if (isSecurityDowngrade(trustedProfile.securityType, securityType)) {
                level = WifiNetworkRiskLevel.DANGER
                reason = WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE
            } else if (isTrustedBssidMismatch(trustedProfile, bssid) &&
                !isBenignTrustedBssidChange(trustedProfile, securityType) &&
                riskSeverity(WifiNetworkRiskLevel.WARNING) > riskSeverity(level)
            ) {
                level = WifiNetworkRiskLevel.WARNING
                reason = WifiAlertReason.TRUSTED_BSSID_MISMATCH
            }
        }

        if (isCurrent) {
            when (currentAlertReason) {
                WifiAlertReason.CAPTIVE_PORTAL -> {
                    if (riskSeverity(WifiNetworkRiskLevel.WARNING) > riskSeverity(level)) {
                        level = WifiNetworkRiskLevel.WARNING
                        reason = WifiAlertReason.CAPTIVE_PORTAL
                    }
                }

                WifiAlertReason.UNVALIDATED -> {
                    if (riskSeverity(WifiNetworkRiskLevel.WARNING) > riskSeverity(level)) {
                        level = WifiNetworkRiskLevel.WARNING
                        reason = WifiAlertReason.UNVALIDATED
                    }
                }

                else -> {
                    // Keep risk from security/trust checks.
                }
            }
        }

        return WifiNearbyRiskResult(level = level, reason = reason)
    }

    private fun buildAdvancedChecks(
        alert: WifiSecurityAlert,
        nearbyState: WifiNearbyNetworksState
    ): List<WifiAdvancedCheckItem> {
        val context = getApplication<Application>()
        val currentNetwork = nearbyState.networks.firstOrNull { it.isCurrent }
        val signals = currentNetwork?.capabilities?.let { securitySignalsFromCapabilities(it) }
        val isWifiConnected = alert.canToggleTrust
        val hasLocationAccess = hasLocationPermission(context)
        val locationServicesEnabled = isLocationServicesEnabled(context)
        val trustedProfile = alert.ssid?.let { getTrustedProfile(it) }
        val vpnActive = isVpnActive(context.getSystemService(ConnectivityManager::class.java))

        val checks = mutableListOf<WifiAdvancedCheckItem>()
        checks += buildPmfCheck(
            isWifiConnected = isWifiConnected,
            hasLocationAccess = hasLocationAccess,
            locationServicesEnabled = locationServicesEnabled,
            signals = signals
        )
        checks += buildWpaModeCheck(
            isWifiConnected = isWifiConnected,
            signals = signals
        )
        checks += buildTransitionModeCheck(
            isWifiConnected = isWifiConnected,
            signals = signals
        )
        checks += buildOpenVsOweCheck(
            isWifiConnected = isWifiConnected,
            signals = signals
        )
        checks += buildEvilTwinCheck(
            alert = alert,
            nearbyState = nearbyState,
            trustedProfile = trustedProfile
        )
        checks += buildChannelBandCheck(
            isWifiConnected = isWifiConnected,
            trustedProfile = trustedProfile,
            currentFrequencyMhz = currentNetwork?.frequencyMhz
        )
        checks += buildDhcpAnomalyCheck(
            isWifiConnected = isWifiConnected,
            trustedProfile = trustedProfile
        )

        val provisionalPenalty = checks.sumOf { it.penalty }
        checks += buildVpnPostureCheck(
            isWifiConnected = isWifiConnected,
            provisionalPenalty = provisionalPenalty,
            vpnActive = vpnActive
        )
        return checks
    }

    private fun buildSafetyAssessment(
        alert: WifiSecurityAlert,
        checks: List<WifiAdvancedCheckItem>
    ): WifiSafetyAssessment {
        val totalPenalty = checks.sumOf { it.penalty }
        val score = (100 - totalPenalty).coerceIn(0, 100)
        val level = when {
            score >= 80 -> WifiNetworkRiskLevel.SAFE
            score >= 55 -> WifiNetworkRiskLevel.WARNING
            else -> WifiNetworkRiskLevel.DANGER
        }
        val topIssue = checks
            .filter { it.level != WifiNetworkRiskLevel.SAFE }
            .maxWithOrNull(compareBy<WifiAdvancedCheckItem> { it.penalty }.thenBy { severityScore(it.level) })
        val summary = topIssue?.detail ?: alert.message
        val recommendationResId = when (level) {
            WifiNetworkRiskLevel.SAFE -> R.string.wifi_security_recommendation_secure
            WifiNetworkRiskLevel.WARNING,
            WifiNetworkRiskLevel.DANGER -> R.string.wifi_security_recommendation_warning
        }
        return WifiSafetyAssessment(
            score = score,
            level = level,
            summary = summary,
            recommendationResId = recommendationResId
        )
    }

    private fun emitRiskTransitionIfNeeded(
        previousLevel: WifiNetworkRiskLevel?,
        currentAssessment: WifiSafetyAssessment
    ) {
        if (previousLevel == null || previousLevel == currentAssessment.level) {
            return
        }
        val worsened = riskSeverity(currentAssessment.level) > riskSeverity(previousLevel)
        val transitionAlert = WifiRiskTransitionAlert(
            id = currentTimeMillis(),
            fromLevel = previousLevel,
            toLevel = currentAssessment.level,
            score = currentAssessment.score,
            summary = currentAssessment.summary,
            worsened = worsened
        )
        _wifiRiskTransitionAlert.postValue(transitionAlert)
        if (worsened) {
            postRiskNotification(transitionAlert)
        }
    }

    private fun applyAutoVpnPolicy(assessment: WifiSafetyAssessment) {
        val policy = _autoVpnPolicy.value ?: AutoVpnPolicy.OFF
        val riskBand = assessment.level != WifiNetworkRiskLevel.SAFE
        val previousRiskBand = lastAutoVpnRiskBand

        if (policy == AutoVpnPolicy.OFF) {
            lastAutoVpnRiskBand = riskBand
            return
        }

        if (previousRiskBand == null) {
            lastAutoVpnRiskBand = riskBand
            if (riskBand) {
                maybeConnectVpnForAutoPolicy()
            }
            return
        }

        if (riskBand && !previousRiskBand) {
            maybeConnectVpnForAutoPolicy()
        } else if (!riskBand &&
            previousRiskBand &&
            policy == AutoVpnPolicy.CONNECT_AND_DISCONNECT_ON_SAFE &&
            isVpnConnected
        ) {
            disconnectVpn()
            _autoVpnActionMessageRes.postValue(R.string.wifi_auto_vpn_auto_disconnected)
        }
        lastAutoVpnRiskBand = riskBand
    }

    private fun maybeConnectVpnForAutoPolicy() {
        val context = getApplication<Application>()
        if (VpnService.prepare(context) != null) {
            _autoVpnActionMessageRes.postValue(R.string.wifi_auto_vpn_permission_required)
            return
        }
        if (!isVpnConnected) {
            connectVpn(context)
            if (isVpnConnected) {
                _autoVpnActionMessageRes.postValue(R.string.wifi_auto_vpn_auto_connected)
            }
        }
    }

    private fun startBackgroundScanSchedulerIfNeeded() {
        if (!(loadBackgroundScanEnabled() && isWifiMonitoringStarted)) {
            stopBackgroundScanScheduler()
            return
        }
        if (backgroundScanRunnable == null) {
            backgroundScanRunnable = Runnable {
                if (!loadBackgroundScanEnabled() || !isWifiMonitoringStarted) {
                    stopBackgroundScanScheduler()
                    return@Runnable
                }
                refreshWifiSecurityAlert(lightweight = true)
                scheduleNextBackgroundScan()
            }
        }
        scheduleNextBackgroundScan()
        updateBackgroundScanState()
    }

    private fun stopBackgroundScanScheduler() {
        backgroundScanRunnable?.let { runnable ->
            mainHandler.removeCallbacks(runnable)
        }
        backgroundScanRunnable = null
    }

    private fun scheduleNextBackgroundScan() {
        val runnable = backgroundScanRunnable ?: return
        mainHandler.removeCallbacks(runnable)
        mainHandler.postDelayed(runnable, backgroundScanIntervalMs())
        updateBackgroundScanState()
    }

    private fun backgroundScanIntervalMs(): Long {
        val battery = getBatteryLevel(getApplication())
        return when {
            battery in 0..20 -> 15L * 60_000L
            battery in 21..50 -> 8L * 60_000L
            else -> 4L * 60_000L
        }
    }

    private fun updateBackgroundScanState() {
        val enabled = loadBackgroundScanEnabled()
        val intervalMs = backgroundScanIntervalMs()
        _backgroundScanState.postValue(
            WifiBackgroundScanState(
                enabled = enabled,
                intervalMinutes = (intervalMs / 60_000L).toInt(),
                batteryAware = true,
                lastRunMillis = lastBackgroundScanRunMillis
            )
        )
    }

    private fun postRiskNotification(alert: WifiRiskTransitionAlert) {
        val context = getApplication<Application>()
        val title = context.getString(R.string.wifi_risk_alert_title)
        val riskLabel = context.getString(riskLevelLabelRes(alert.toLevel))
        val content = context.getString(
            R.string.wifi_risk_alert_message,
            riskLabel,
            alert.score
        )
        val notification = NotificationCompat.Builder(context, WIFI_RISK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_menu_wifi)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${content}\n${alert.summary}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(WIFI_RISK_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notifications permission may be denied on Android 13+.
        }
    }

    private fun createRiskNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val context = getApplication<Application>()
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(WIFI_RISK_CHANNEL_ID)
        if (existing != null) return
        val channel = NotificationChannel(
            WIFI_RISK_CHANNEL_ID,
            context.getString(R.string.wifi_risk_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.wifi_risk_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun riskLevelLabelRes(level: WifiNetworkRiskLevel): Int {
        return when (level) {
            WifiNetworkRiskLevel.SAFE -> R.string.risk_safe
            WifiNetworkRiskLevel.WARNING -> R.string.risk_warning
            WifiNetworkRiskLevel.DANGER -> R.string.risk_risk
        }
    }

    private fun maybeUpdateTrustedBaseline(
        alert: WifiSecurityAlert,
        nearbyState: WifiNearbyNetworksState
    ) {
        if (!alert.isTrustedNetwork || alert.ssid == null) return
        val trustedProfile = getTrustedProfile(alert.ssid) ?: return
        val currentNetwork = nearbyState.networks.firstOrNull { it.isCurrent }
        val updatedFrequency = currentNetwork?.frequencyMhz ?: trustedProfile.lastFrequencyMhz
        val updatedGateway = getCurrentGatewayAddress() ?: trustedProfile.lastGateway
        val mergedKnownBssids = mergeKnownBssids(trustedProfile.knownBssids, alert.bssid)
        val updatedBssid = alert.bssid ?: trustedProfile.bssid ?: mergedKnownBssids.firstOrNull()
        val updatedSecurity = alert.securityType ?: trustedProfile.securityType
        saveTrustedProfile(
            ssid = trustedProfile.ssid,
            bssid = updatedBssid,
            securityType = updatedSecurity,
            knownBssids = mergedKnownBssids,
            lastFrequencyMhz = updatedFrequency,
            lastSeenMillis = currentTimeMillis(),
            lastGateway = updatedGateway
        )
    }

    private fun buildPmfCheck(
        isWifiConnected: Boolean,
        hasLocationAccess: Boolean,
        locationServicesEnabled: Boolean,
        signals: WifiSecuritySignals?
    ): WifiAdvancedCheckItem {
        return when {
            !isWifiConnected -> safeCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "Not on Wi-Fi."
            )

            !hasLocationAccess -> warningCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "Location permission is required to inspect PMF support.",
                penalty = 5
            )

            !locationServicesEnabled -> warningCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "Location services are off, so PMF details are unavailable.",
                penalty = 5
            )

            signals == null -> warningCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "PMF details were unavailable from Wi-Fi capabilities.",
                penalty = 5
            )

            signals.hasPmfRequired -> safeCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "PMF is required by this network."
            )

            signals.hasPmfCapable -> warningCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "PMF is optional. Required PMF is safer on untrusted Wi-Fi.",
                penalty = 7
            )

            signals.securityType == WifiSecurityType.SECURE -> warningCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "PMF was not advertised by this secure network.",
                penalty = 9
            )

            else -> safeCheck(
                key = "pmf",
                titleRes = R.string.wifi_check_pmf_title,
                detail = "PMF check is not applicable for this network type."
            )
        }
    }

    private fun buildWpaModeCheck(
        isWifiConnected: Boolean,
        signals: WifiSecuritySignals?
    ): WifiAdvancedCheckItem {
        return when {
            !isWifiConnected -> safeCheck(
                key = "wpa_mode",
                titleRes = R.string.wifi_check_wpa_mode_title,
                detail = "Not on Wi-Fi."
            )

            signals == null -> warningCheck(
                key = "wpa_mode",
                titleRes = R.string.wifi_check_wpa_mode_title,
                detail = "Security mode could not be identified.",
                penalty = 6
            )

            signals.isWep || signals.securityType == WifiSecurityType.OPEN -> warningCheck(
                key = "wpa_mode",
                titleRes = R.string.wifi_check_wpa_mode_title,
                detail = "Modern WPA security mode is not in use.",
                penalty = 8
            )

            signals.isWpa3Personal || signals.isWpa3Enterprise || signals.isOwe -> safeCheck(
                key = "wpa_mode",
                titleRes = R.string.wifi_check_wpa_mode_title,
                detail = "Modern WPA3/OWE mode detected."
            )

            signals.isTransitionMode -> warningCheck(
                key = "wpa_mode",
                titleRes = R.string.wifi_check_wpa_mode_title,
                detail = "WPA2/WPA3 transition mode is enabled.",
                penalty = 7
            )

            signals.isWpa2Personal || signals.isWpa2Enterprise -> warningCheck(
                key = "wpa_mode",
                titleRes = R.string.wifi_check_wpa_mode_title,
                detail = "WPA2 mode detected. This is acceptable but weaker than WPA3.",
                penalty = 5
            )

            else -> warningCheck(
                key = "wpa_mode",
                titleRes = R.string.wifi_check_wpa_mode_title,
                detail = "Security mode could not be identified.",
                penalty = 6
            )
        }
    }

    private fun buildTransitionModeCheck(
        isWifiConnected: Boolean,
        signals: WifiSecuritySignals?
    ): WifiAdvancedCheckItem {
        return if (!isWifiConnected) {
            safeCheck(
                key = "transition_mode",
                titleRes = R.string.wifi_check_transition_title,
                detail = "Not on Wi-Fi."
            )
        } else if (signals?.isTransitionMode == true) {
            warningCheck(
                key = "transition_mode",
                titleRes = R.string.wifi_check_transition_title,
                detail = "Transition mode can allow client downgrade paths.",
                penalty = 8
            )
        } else {
            safeCheck(
                key = "transition_mode",
                titleRes = R.string.wifi_check_transition_title,
                detail = "No transition-mode downgrade pattern detected."
            )
        }
    }

    private fun buildOpenVsOweCheck(
        isWifiConnected: Boolean,
        signals: WifiSecuritySignals?
    ): WifiAdvancedCheckItem {
        return when {
            !isWifiConnected -> safeCheck(
                key = "open_owe",
                titleRes = R.string.wifi_check_open_owe_title,
                detail = "Not on Wi-Fi."
            )

            signals == null -> warningCheck(
                key = "open_owe",
                titleRes = R.string.wifi_check_open_owe_title,
                detail = "Could not determine whether this network is plain-open or OWE-protected.",
                penalty = 5
            )

            signals.isWep || signals.securityType == WifiSecurityType.WEP -> dangerCheck(
                key = "open_owe",
                titleRes = R.string.wifi_check_open_owe_title,
                detail = "WEP is considered weak and should be avoided.",
                penalty = 20
            )

            signals.isOpenPlain || signals.securityType == WifiSecurityType.OPEN -> dangerCheck(
                key = "open_owe",
                titleRes = R.string.wifi_check_open_owe_title,
                detail = "This is a plain open network without OWE protection.",
                penalty = 24
            )

            signals.isOwe -> safeCheck(
                key = "open_owe",
                titleRes = R.string.wifi_check_open_owe_title,
                detail = "OWE protection is advertised for open-style access."
            )

            else -> safeCheck(
                key = "open_owe",
                titleRes = R.string.wifi_check_open_owe_title,
                detail = "Network is not plain-open."
            )
        }
    }

    private fun buildEvilTwinCheck(
        alert: WifiSecurityAlert,
        nearbyState: WifiNearbyNetworksState,
        trustedProfile: TrustedWifiProfile?
    ): WifiAdvancedCheckItem {
        val currentSsid = alert.ssid
        if (!alert.canToggleTrust || currentSsid == null) {
            return safeCheck(
                key = "evil_twin",
                titleRes = R.string.wifi_check_evil_twin_title,
                detail = "Not on Wi-Fi."
            )
        }

        val currentNetwork = nearbyState.networks.firstOrNull { it.isCurrent }
        val competing = nearbyState.networks.filter {
            !it.isCurrent && it.ssid.equals(currentSsid, ignoreCase = true)
        }
        if (competing.isEmpty() && trustedProfile == null) {
            return safeCheck(
                key = "evil_twin",
                titleRes = R.string.wifi_check_evil_twin_title,
                detail = "No obvious SSID clone pattern detected nearby."
            )
        }

        val currentSecurityType = currentNetwork?.securityType ?: alert.securityType ?: WifiSecurityType.UNKNOWN
        val currentRank = securityRank(currentSecurityType)
        val weakerCloneFound = competing.any { securityRank(it.securityType) < currentRank }
        val mixedSecurityTypeFound = competing.any {
            it.securityType != WifiSecurityType.UNKNOWN &&
                currentSecurityType != WifiSecurityType.UNKNOWN &&
                it.securityType != currentSecurityType
        }
        val differentBandPeerFound = competing.any { isDifferentBand(currentNetwork?.frequencyMhz, it.frequencyMhz) }
        val trustedMismatch = trustedProfile != null &&
            isTrustedBssidMismatch(trustedProfile, alert.bssid) &&
            !isBenignTrustedBssidChange(trustedProfile, currentSecurityType)

        return when {
            trustedMismatch && (weakerCloneFound || mixedSecurityTypeFound) -> dangerCheck(
                key = "evil_twin",
                titleRes = R.string.wifi_check_evil_twin_title,
                detail = "Trusted network fingerprint changed and weaker same-name APs are nearby.",
                penalty = 16
            )

            trustedMismatch || weakerCloneFound || mixedSecurityTypeFound -> warningCheck(
                key = "evil_twin",
                titleRes = R.string.wifi_check_evil_twin_title,
                detail = "Same-name AP behavior looks suspicious. Verify SSID and AP identity.",
                penalty = 11
            )

            competing.isNotEmpty() && differentBandPeerFound -> safeCheck(
                key = "evil_twin",
                titleRes = R.string.wifi_check_evil_twin_title,
                detail = "Same SSID is visible on multiple bands/APs with similar security (common on dual-band or mesh networks)."
            )

            competing.isNotEmpty() -> safeCheck(
                key = "evil_twin",
                titleRes = R.string.wifi_check_evil_twin_title,
                detail = "Multiple APs share this SSID. This is often normal for mesh or extender setups."
            )

            else -> safeCheck(
                key = "evil_twin",
                titleRes = R.string.wifi_check_evil_twin_title,
                detail = "No obvious SSID clone pattern detected nearby."
            )
        }
    }

    private fun buildChannelBandCheck(
        isWifiConnected: Boolean,
        trustedProfile: TrustedWifiProfile?,
        currentFrequencyMhz: Int?
    ): WifiAdvancedCheckItem {
        if (!isWifiConnected) {
            return safeCheck(
                key = "channel_band",
                titleRes = R.string.wifi_check_channel_title,
                detail = "Not on Wi-Fi."
            )
        }
        if (trustedProfile == null) {
            return safeCheck(
                key = "channel_band",
                titleRes = R.string.wifi_check_channel_title,
                detail = "Channel/band baseline runs after you trust this network."
            )
        }
        if (currentFrequencyMhz == null) {
            return warningCheck(
                key = "channel_band",
                titleRes = R.string.wifi_check_channel_title,
                detail = "Current channel could not be determined.",
                penalty = 4
            )
        }

        val previousFreq = trustedProfile.lastFrequencyMhz
        val previousSeen = trustedProfile.lastSeenMillis
        if (previousFreq != null && previousFreq != currentFrequencyMhz) {
            val rapidChange = previousSeen != null &&
                currentTimeMillis() - previousSeen < 15 * 60 * 1000
            val previousBand = wifiBandLabel(previousFreq)
            val currentBand = wifiBandLabel(currentFrequencyMhz)
            return warningCheck(
                key = "channel_band",
                titleRes = R.string.wifi_check_channel_title,
                detail = "Trusted network moved from $previousBand to $currentBand recently.",
                penalty = if (rapidChange) 8 else 5
            )
        }

        return safeCheck(
            key = "channel_band",
            titleRes = R.string.wifi_check_channel_title,
            detail = "Channel/band behavior is stable."
        )
    }

    private fun buildDhcpAnomalyCheck(
        isWifiConnected: Boolean,
        trustedProfile: TrustedWifiProfile?
    ): WifiAdvancedCheckItem {
        val context = getApplication<Application>()
        if (!isWifiConnected) {
            return safeCheck(
                key = "dhcp_gateway",
                titleRes = R.string.wifi_check_dhcp_title,
                detail = "Not on Wi-Fi."
            )
        }

        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
        val dhcpInfo = runCatching { wifiManager?.dhcpInfo }.getOrNull()
        if (dhcpInfo == null) {
            return warningCheck(
                key = "dhcp_gateway",
                titleRes = R.string.wifi_check_dhcp_title,
                detail = "DHCP/gateway details are unavailable.",
                penalty = 4
            )
        }

        val ipAddress = ipv4FromInt(dhcpInfo.ipAddress)
        val gateway = ipv4FromInt(dhcpInfo.gateway)
        val netmask = ipv4FromInt(dhcpInfo.netmask)
        if (ipAddress == "0.0.0.0" || gateway == "0.0.0.0") {
            return dangerCheck(
                key = "dhcp_gateway",
                titleRes = R.string.wifi_check_dhcp_title,
                detail = "Gateway/IP values look invalid for an active Wi-Fi session.",
                penalty = 18
            )
        }

        if (!isSameSubnet(ipAddress, gateway, netmask)) {
            return warningCheck(
                key = "dhcp_gateway",
                titleRes = R.string.wifi_check_dhcp_title,
                detail = "IP and gateway appear to be on different subnets.",
                penalty = 10
            )
        }

        if (trustedProfile?.lastGateway != null &&
            !trustedProfile.lastGateway.equals(gateway, ignoreCase = true)
        ) {
            return warningCheck(
                key = "dhcp_gateway",
                titleRes = R.string.wifi_check_dhcp_title,
                detail = "Gateway changed since this trusted network baseline was recorded.",
                penalty = 8
            )
        }

        return safeCheck(
            key = "dhcp_gateway",
            titleRes = R.string.wifi_check_dhcp_title,
            detail = "DHCP/gateway values look consistent."
        )
    }

    private fun buildVpnPostureCheck(
        isWifiConnected: Boolean,
        provisionalPenalty: Int,
        vpnActive: Boolean
    ): WifiAdvancedCheckItem {
        if (!isWifiConnected) {
            return safeCheck(
                key = "vpn_posture",
                titleRes = R.string.wifi_check_vpn_posture_title,
                detail = "Not on Wi-Fi."
            )
        }

        val provisionalScore = (100 - provisionalPenalty).coerceIn(0, 100)
        return if (provisionalScore < 80 && !vpnActive) {
            warningCheck(
                key = "vpn_posture",
                titleRes = R.string.wifi_check_vpn_posture_title,
                detail = "Wi-Fi risk is elevated and VPN is currently off.",
                penalty = 10
            )
        } else if (vpnActive) {
            safeCheck(
                key = "vpn_posture",
                titleRes = R.string.wifi_check_vpn_posture_title,
                detail = "VPN is active."
            )
        } else {
            safeCheck(
                key = "vpn_posture",
                titleRes = R.string.wifi_check_vpn_posture_title,
                detail = "Risk is low and VPN posture is acceptable."
            )
        }
    }

    private fun safeCheck(key: String, titleRes: Int, detail: String): WifiAdvancedCheckItem {
        return WifiAdvancedCheckItem(
            key = key,
            titleResId = titleRes,
            detail = detail,
            level = WifiNetworkRiskLevel.SAFE,
            penalty = 0
        )
    }

    private fun warningCheck(key: String, titleRes: Int, detail: String, penalty: Int): WifiAdvancedCheckItem {
        return WifiAdvancedCheckItem(
            key = key,
            titleResId = titleRes,
            detail = detail,
            level = WifiNetworkRiskLevel.WARNING,
            penalty = penalty
        )
    }

    private fun dangerCheck(key: String, titleRes: Int, detail: String, penalty: Int): WifiAdvancedCheckItem {
        return WifiAdvancedCheckItem(
            key = key,
            titleResId = titleRes,
            detail = detail,
            level = WifiNetworkRiskLevel.DANGER,
            penalty = penalty
        )
    }

    private fun severityScore(level: WifiNetworkRiskLevel): Int {
        return when (level) {
            WifiNetworkRiskLevel.SAFE -> 0
            WifiNetworkRiskLevel.WARNING -> 1
            WifiNetworkRiskLevel.DANGER -> 2
        }
    }

    private fun securityRank(type: WifiSecurityType): Int {
        return when (type) {
            WifiSecurityType.OPEN -> 0
            WifiSecurityType.WEP -> 1
            WifiSecurityType.UNKNOWN -> 2
            WifiSecurityType.SECURE -> 3
        }
    }

    private fun isTrustedBssidMismatch(trustedProfile: TrustedWifiProfile, bssid: String?): Boolean {
        val normalized = normalizeBssid(bssid) ?: return false
        if (trustedProfile.knownBssids.any { it.equals(normalized, ignoreCase = true) }) {
            return false
        }
        val primary = normalizeBssid(trustedProfile.bssid) ?: return false
        return !primary.equals(normalized, ignoreCase = true)
    }

    private fun isBenignTrustedBssidChange(
        trustedProfile: TrustedWifiProfile,
        currentType: WifiSecurityType?
    ): Boolean {
        return trustedProfile.securityType == WifiSecurityType.SECURE &&
            currentType == WifiSecurityType.SECURE
    }

    private fun isVpnActive(connectivityManager: ConnectivityManager?): Boolean {
        if (connectivityManager == null) return false
        return runCatching {
            connectivityManager.allNetworks.any { network ->
                val caps = connectivityManager.getNetworkCapabilities(network)
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            }
        }.getOrElse { false }
    }

    private fun getCurrentGatewayAddress(): String? {
        val context = getApplication<Application>()
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
        val gatewayInt = runCatching { wifiManager?.dhcpInfo?.gateway }.getOrNull() ?: return null
        val gateway = ipv4FromInt(gatewayInt)
        return if (gateway == "0.0.0.0") null else gateway
    }

    private fun ipv4FromInt(value: Int): String {
        return listOf(
            value and 0xff,
            value shr 8 and 0xff,
            value shr 16 and 0xff,
            value shr 24 and 0xff
        ).joinToString(".")
    }

    private fun isSameSubnet(ip: String, gateway: String, mask: String): Boolean {
        val ipInt = ipv4ToInt(ip) ?: return false
        val gwInt = ipv4ToInt(gateway) ?: return false
        val maskInt = ipv4ToInt(mask) ?: return false
        return (ipInt and maskInt) == (gwInt and maskInt)
    }

    private fun ipv4ToInt(ip: String): Int? {
        val parts = ip.split(".")
        if (parts.size != 4) return null
        return runCatching {
            parts.map { it.toInt() }.foldIndexed(0) { index, acc, part ->
                require(part in 0..255)
                acc or (part shl (index * 8))
            }
        }.getOrNull()
    }

    private fun wifiBandLabel(frequencyMhz: Int): String {
        return when {
            frequencyMhz in 2400..2500 -> "2.4 GHz"
            frequencyMhz in 4900..5900 -> "5 GHz"
            frequencyMhz in 5925..7125 -> "6 GHz"
            else -> "${frequencyMhz} MHz"
        }
    }

    private fun wifiBandBucket(frequencyMhz: Int?): Int? {
        val freq = frequencyMhz ?: return null
        return when {
            freq in 2400..2500 -> 24
            freq in 4900..5900 -> 50
            freq in 5925..7125 -> 60
            else -> null
        }
    }

    private fun isDifferentBand(firstFrequencyMhz: Int?, secondFrequencyMhz: Int?): Boolean {
        val firstBand = wifiBandBucket(firstFrequencyMhz) ?: return false
        val secondBand = wifiBandBucket(secondFrequencyMhz) ?: return false
        return firstBand != secondBand
    }

    private fun buildCurrentWifiFallbackNetwork(
        currentAlert: WifiSecurityAlert,
        trustedBySsid: Map<String, TrustedWifiProfile>
    ): WifiNearbyNetwork? {
        val ssid = currentAlert.ssid ?: return null
        if (currentAlert.reason == WifiAlertReason.NOT_WIFI) return null

        val securityType = currentAlert.securityType ?: WifiSecurityType.UNKNOWN
        val trustedProfile = trustedBySsid[ssid]
        val baseReason = when (currentAlert.reason) {
            WifiAlertReason.CAPTIVE_PORTAL,
            WifiAlertReason.UNVALIDATED,
            WifiAlertReason.OPEN_OR_WEP,
            WifiAlertReason.UNKNOWN_SECURITY,
            WifiAlertReason.WEAK_LEGACY_CIPHER,
            WifiAlertReason.WPS_ENABLED,
            WifiAlertReason.LOCATION_SERVICES_DISABLED,
            WifiAlertReason.TRUSTED_BSSID_MISMATCH,
            WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE,
            WifiAlertReason.SECURE -> currentAlert.reason

            else -> reasonForSecurityType(securityType)
        }

        return WifiNearbyNetwork(
            ssid = ssid,
            bssid = currentAlert.bssid,
            securityType = securityType,
            riskLevel = nearbyRiskLevelForReason(baseReason),
            reason = baseReason,
            isTrusted = trustedProfile != null,
            isCurrent = true,
            hasInternetAccess = currentAlert.hasInternetAccess,
            frequencyMhz = null,
            capabilities = null
        )
    }

    private fun reasonForSecurityType(securityType: WifiSecurityType): WifiAlertReason {
        return when (securityType) {
            WifiSecurityType.OPEN,
            WifiSecurityType.WEP -> WifiAlertReason.OPEN_OR_WEP

            WifiSecurityType.UNKNOWN -> WifiAlertReason.UNKNOWN_SECURITY
            WifiSecurityType.SECURE -> WifiAlertReason.SECURE
        }
    }

    private fun nearbyRiskLevelForReason(reason: WifiAlertReason): WifiNetworkRiskLevel {
        return when (reason) {
            WifiAlertReason.OPEN_OR_WEP,
            WifiAlertReason.TRUSTED_SECURITY_DOWNGRADE -> WifiNetworkRiskLevel.DANGER

            WifiAlertReason.CAPTIVE_PORTAL,
            WifiAlertReason.UNVALIDATED,
            WifiAlertReason.UNKNOWN_SECURITY,
            WifiAlertReason.WEAK_LEGACY_CIPHER,
            WifiAlertReason.WPS_ENABLED,
            WifiAlertReason.LOCATION_SERVICES_DISABLED,
            WifiAlertReason.TRUSTED_BSSID_MISMATCH,
            WifiAlertReason.MISSING_PERMISSION,
            WifiAlertReason.LEGACY_NO_SECURITY_TYPE,
            WifiAlertReason.NOT_WIFI,
            WifiAlertReason.NO_NETWORK,
            WifiAlertReason.UNAVAILABLE -> WifiNetworkRiskLevel.WARNING

            WifiAlertReason.SECURE -> WifiNetworkRiskLevel.SAFE
        }
    }

    private fun isSameNetwork(first: WifiNearbyNetwork, second: WifiNearbyNetwork): Boolean {
        return when {
            first.bssid != null && second.bssid != null ->
                first.bssid.equals(second.bssid, ignoreCase = true)

            else -> first.ssid.equals(second.ssid, ignoreCase = true)
        }
    }

    private fun securitySignalsFromCapabilities(capabilities: String?): WifiSecuritySignals {
        val normalized = capabilities.orEmpty().uppercase()
        val hasWep = "WEP" in normalized
        val hasPsk = "PSK" in normalized
        val hasSae = "SAE" in normalized
        val hasEap = "EAP" in normalized
        val hasSuiteB = "SUITE_B_192" in normalized
        val hasOwe = "OWE" in normalized
        val hasWps = "WPS" in normalized
        val hasWeakCipher = "TKIP" in normalized
        val hasPmfRequired = "MFPR" in normalized
        val hasPmfCapable = "MFPC" in normalized
        val hasTransition = (hasSae && hasPsk) || "OWE_TRANSITION" in normalized
        val isOpenPlain = (normalized.isBlank() || normalized == "[ESS]") && !hasOwe

        val securityType = when {
            hasWep -> WifiSecurityType.WEP
            "WPA" in normalized || "RSN" in normalized || hasSae || hasEap || hasOwe -> WifiSecurityType.SECURE
            isOpenPlain -> WifiSecurityType.OPEN
            else -> WifiSecurityType.UNKNOWN
        }
        return WifiSecuritySignals(
            securityType = securityType,
            hasWps = hasWps,
            hasWeakCipher = hasWeakCipher,
            hasPmfRequired = hasPmfRequired,
            hasPmfCapable = hasPmfCapable,
            isTransitionMode = hasTransition,
            isOwe = hasOwe,
            isOpenPlain = isOpenPlain,
            isWpa2Personal = hasPsk,
            isWpa3Personal = hasSae,
            isWpa2Enterprise = hasEap && !hasSuiteB,
            isWpa3Enterprise = hasSuiteB,
            isWep = hasWep
        )
    }

    private fun riskSeverity(level: WifiNetworkRiskLevel): Int {
        return when (level) {
            WifiNetworkRiskLevel.SAFE -> 0
            WifiNetworkRiskLevel.WARNING -> 1
            WifiNetworkRiskLevel.DANGER -> 2
        }
    }

    private data class WifiNearbyRiskResult(
        val level: WifiNetworkRiskLevel,
        val reason: WifiAlertReason
    )

    private fun hasLocationPermission(context: Application): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationServicesEnabled(context: Context): Boolean {
        val manager = context.getSystemService(LocationManager::class.java) ?: return true
        return runCatching { manager.isLocationEnabled }.getOrDefault(true)
    }

    private fun resolveWifiSecurityType(
        caps: NetworkCapabilities,
        supportsSecurityTypeDetection: Boolean,
        hasLocationPermission: Boolean
    ): WifiSecurityType? {
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return null
        }
        if (!supportsSecurityTypeDetection || !hasLocationPermission) {
            return null
        }
        val wifiInfo = caps.transportInfo as? WifiInfo ?: return WifiSecurityType.UNKNOWN
        return wifiInfo.currentSecurityType.toWifiSecurityType()
    }

    private fun Int.toWifiSecurityType(): WifiSecurityType {
        return when (this) {
            WifiInfo.SECURITY_TYPE_OPEN -> WifiSecurityType.OPEN
            WifiInfo.SECURITY_TYPE_WEP -> WifiSecurityType.WEP
            WifiInfo.SECURITY_TYPE_UNKNOWN -> WifiSecurityType.UNKNOWN
            else -> WifiSecurityType.SECURE
        }
    }

    private fun reasonMessageResId(reason: WifiAlertReason): Int {
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

    private fun normalizeSsid(rawSsid: String?): String? {
        if (rawSsid.isNullOrBlank()) return null
        val cleaned = rawSsid.trim().removePrefix("\"").removeSuffix("\"")
        return if (cleaned.equals("<unknown ssid>", ignoreCase = true) || cleaned.isBlank()) null else cleaned
    }

    private fun normalizeBssid(rawBssid: String?): String? {
        if (rawBssid.isNullOrBlank()) return null
        val cleaned = rawBssid.trim()
        return if (cleaned == "02:00:00:00:00:00") null else cleaned
    }

    private fun isSecurityDowngrade(trustedType: WifiSecurityType, currentType: WifiSecurityType?): Boolean {
        return when (trustedType) {
            WifiSecurityType.SECURE -> currentType == WifiSecurityType.OPEN || currentType == WifiSecurityType.WEP
            WifiSecurityType.WEP -> currentType == WifiSecurityType.OPEN
            WifiSecurityType.UNKNOWN,
            WifiSecurityType.OPEN -> false
        }
    }

    private fun getTrustedProfile(ssid: String): TrustedWifiProfile? {
        return loadTrustedProfiles().firstOrNull { it.ssid == ssid }
    }

    private fun loadTrustedProfiles(): List<TrustedWifiProfile> {
        val root = loadTrustedProfilesJson()
        val profiles = mutableListOf<TrustedWifiProfile>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val ssid = keys.next().orEmpty()
            if (ssid.isBlank()) continue
            val profile = root.optJSONObject(ssid) ?: continue
            val storedBssid = if (profile.isNull("bssid")) null else profile.optString("bssid", null)
            val knownBssids = mutableSetOf<String>().apply {
                val jsonArray = profile.optJSONArray("knownBssids")
                if (jsonArray != null) {
                    for (index in 0 until jsonArray.length()) {
                        normalizeBssid(jsonArray.optString(index))?.let { add(it) }
                    }
                }
                normalizeBssid(storedBssid)?.let { add(it) }
            }
            val storedSecurityName = profile.optString("securityType", WifiSecurityType.UNKNOWN.name)
            val storedSecurityType = runCatching { WifiSecurityType.valueOf(storedSecurityName) }
                .getOrDefault(WifiSecurityType.UNKNOWN)
            profiles += TrustedWifiProfile(
                ssid = ssid,
                bssid = normalizeBssid(storedBssid),
                knownBssids = knownBssids,
                securityType = storedSecurityType,
                lastFrequencyMhz = if (profile.has("lastFrequencyMhz") && !profile.isNull("lastFrequencyMhz")) {
                    profile.optInt("lastFrequencyMhz")
                } else {
                    null
                },
                lastSeenMillis = if (profile.has("lastSeenMillis") && !profile.isNull("lastSeenMillis")) {
                    profile.optLong("lastSeenMillis")
                } else {
                    null
                },
                lastGateway = if (profile.isNull("lastGateway")) null else profile.optString("lastGateway", null)
            )
        }
        return profiles
    }

    private fun saveTrustedProfile(
        ssid: String,
        bssid: String?,
        securityType: WifiSecurityType,
        knownBssids: Set<String> = emptySet(),
        lastFrequencyMhz: Int? = null,
        lastSeenMillis: Long? = null,
        lastGateway: String? = null
    ) {
        val root = loadTrustedProfilesJson()
        val normalizedKnownBssids = mergeKnownBssids(knownBssids, bssid)
        val profile = JSONObject().apply {
            put("bssid", bssid ?: JSONObject.NULL)
            put("knownBssids", JSONArray(normalizedKnownBssids.sorted()))
            put("securityType", securityType.name)
            put("lastFrequencyMhz", lastFrequencyMhz ?: JSONObject.NULL)
            put("lastSeenMillis", lastSeenMillis ?: JSONObject.NULL)
            put("lastGateway", lastGateway ?: JSONObject.NULL)
        }
        root.put(ssid, profile)
        saveTrustedProfilesJson(root)
    }

    private fun mergeKnownBssids(existing: Set<String>, bssid: String?): Set<String> {
        return buildSet {
            existing.forEach { value -> normalizeBssid(value)?.let { add(it) } }
            normalizeBssid(bssid)?.let { add(it) }
        }
    }

    private fun removeTrustedProfile(ssid: String) {
        val root = loadTrustedProfilesJson()
        root.remove(ssid)
        saveTrustedProfilesJson(root)
    }

    private fun loadAutoVpnPolicy(): AutoVpnPolicy {
        val raw = trustPrefs.getString(AUTO_VPN_POLICY_KEY, AutoVpnPolicy.OFF.name).orEmpty()
        return runCatching { AutoVpnPolicy.valueOf(raw) }.getOrDefault(AutoVpnPolicy.OFF)
    }

    private fun saveAutoVpnPolicy(policy: AutoVpnPolicy) {
        trustPrefs.edit().putString(AUTO_VPN_POLICY_KEY, policy.name).apply()
    }

    private fun loadBackgroundScanEnabled(): Boolean {
        return trustPrefs.getBoolean(BACKGROUND_SCAN_ENABLED_KEY, false)
    }

    private fun saveBackgroundScanEnabled(enabled: Boolean) {
        trustPrefs.edit().putBoolean(BACKGROUND_SCAN_ENABLED_KEY, enabled).apply()
    }

    private fun loadTrustedProfilesJson(): JSONObject {
        val raw = trustPrefs.getString(TRUSTED_PROFILES_KEY, "{}").orEmpty()
        return runCatching { JSONObject(raw) }.getOrElse { JSONObject() }
    }

    private fun saveTrustedProfilesJson(root: JSONObject) {
        trustPrefs.edit().putString(TRUSTED_PROFILES_KEY, root.toString()).apply()
    }

    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    private fun getBatteryLevel(context: Context): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1
    }

    override fun onCleared() {
        stopWifiMonitoring()
        super.onCleared()
    }

    private companion object {
        private const val WIFI_TRUST_PREFS = "wifi_trust_profiles"
        private const val TRUSTED_PROFILES_KEY = "trusted_profiles_json"
        private const val AUTO_VPN_POLICY_KEY = "auto_vpn_policy"
        private const val BACKGROUND_SCAN_ENABLED_KEY = "background_scan_enabled"
        private const val WIFI_RISK_CHANNEL_ID = "wifi_risk_alerts"
        private const val WIFI_RISK_NOTIFICATION_ID = 12002
    }
}

enum class WifiAlertLevel {
    SECURE,
    WARNING,
    INFO
}

enum class WifiAlertReason {
    UNAVAILABLE,
    NO_NETWORK,
    NOT_WIFI,
    CAPTIVE_PORTAL,
    UNVALIDATED,
    LEGACY_NO_SECURITY_TYPE,
    MISSING_PERMISSION,
    LOCATION_SERVICES_DISABLED,
    OPEN_OR_WEP,
    UNKNOWN_SECURITY,
    WEAK_LEGACY_CIPHER,
    WPS_ENABLED,
    TRUSTED_BSSID_MISMATCH,
    TRUSTED_SECURITY_DOWNGRADE,
    SECURE
}

enum class WifiSecurityType {
    OPEN,
    WEP,
    UNKNOWN,
    SECURE
}

enum class WifiNetworkRiskLevel {
    SAFE,
    WARNING,
    DANGER
}

data class WifiNearbyNetwork(
    val ssid: String,
    val bssid: String?,
    val securityType: WifiSecurityType,
    val riskLevel: WifiNetworkRiskLevel,
    val reason: WifiAlertReason,
    val isTrusted: Boolean,
    val isCurrent: Boolean,
    val hasInternetAccess: Boolean?,
    val frequencyMhz: Int? = null,
    val capabilities: String? = null
)

data class WifiNearbyNetworksState(
    val networks: List<WifiNearbyNetwork> = emptyList(),
    val messageResId: Int? = null
)

data class WifiSecurityAlert(
    val level: WifiAlertLevel,
    val reason: WifiAlertReason,
    val message: String,
    val securityType: WifiSecurityType? = null,
    val checkedAtMillis: Long = System.currentTimeMillis(),
    val requiresLocationPermission: Boolean = false,
    val ssid: String? = null,
    val bssid: String? = null,
    val isTrustedNetwork: Boolean = false,
    val canToggleTrust: Boolean = false,
    val hasInternetAccess: Boolean? = null
)

data class TrustedWifiProfile(
    val ssid: String,
    val bssid: String?,
    val knownBssids: Set<String> = emptySet(),
    val securityType: WifiSecurityType,
    val lastFrequencyMhz: Int? = null,
    val lastSeenMillis: Long? = null,
    val lastGateway: String? = null
)

data class WifiSecuritySignals(
    val securityType: WifiSecurityType,
    val hasWps: Boolean,
    val hasWeakCipher: Boolean,
    val hasPmfRequired: Boolean,
    val hasPmfCapable: Boolean,
    val isTransitionMode: Boolean,
    val isOwe: Boolean,
    val isOpenPlain: Boolean,
    val isWpa2Personal: Boolean,
    val isWpa3Personal: Boolean,
    val isWpa2Enterprise: Boolean,
    val isWpa3Enterprise: Boolean,
    val isWep: Boolean
)

data class WifiAdvancedCheckItem(
    val key: String,
    val titleResId: Int,
    val detail: String,
    val level: WifiNetworkRiskLevel,
    val penalty: Int
)

data class WifiSafetyAssessment(
    val score: Int = 100,
    val level: WifiNetworkRiskLevel = WifiNetworkRiskLevel.SAFE,
    val summary: String = "",
    val recommendationResId: Int = R.string.wifi_security_recommendation_secure
)

enum class AutoVpnPolicy {
    OFF,
    CONNECT_ON_RISK,
    CONNECT_AND_DISCONNECT_ON_SAFE
}

data class WifiRiskTransitionAlert(
    val id: Long,
    val fromLevel: WifiNetworkRiskLevel,
    val toLevel: WifiNetworkRiskLevel,
    val score: Int,
    val summary: String,
    val worsened: Boolean
)

data class WifiBackgroundScanState(
    val enabled: Boolean = false,
    val intervalMinutes: Int = 0,
    val batteryAware: Boolean = true,
    val lastRunMillis: Long? = null
)
