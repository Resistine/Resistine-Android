package com.resistine.android.ui.vpn

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import org.json.JSONObject
import java.io.IOException

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private var backend: GoBackend? = null
    private var tunnel: Tunnel? = null
    private val tunnelName = "MyWireGuardTunnel"

    private val _vpnStatus = MutableLiveData<String>()
    val vpnStatus: LiveData<String> = _vpnStatus

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
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isWifiMonitoringStarted = false

    init {
        loadPhoneInfo()
        fetchLocationData()
        refreshWifiSecurityAlert()
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

    fun refreshWifiSecurityAlert() {
        _wifiSecurityAlert.postValue(buildWifiSecurityAlert())
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
            _vpnStatus.postValue("VPN connected")
        } catch (e: Exception) {
            val reason = e::class.java.getDeclaredField("reason").apply { isAccessible = true }.get(e)?.toString()
            if (reason?.contains("UNABLE_TO_START_VPN") == true) {
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        backend?.setState(tunnel!!, State.UP, config)
                        isVpnConnected = true
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
            .url("https://ipwho.is") // or https://ip-api.com/json/
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
//                        val city = obj.optString("city")
                        val region = obj.optString("regionName")
                        val country = obj.optString("country")
                        val lat = obj.optDouble("lat")
                        val lon = obj.optDouble("lon")

                        val text = buildString {
                            if (country.isNotEmpty()) append(country)
//                            if (city.isNotEmpty()) append(", $city")
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
        val supportsSecurityTypeDetection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val hasLocationAccess = hasLocationPermission(context)
        val wifiSecurityType = resolveWifiSecurityType(
            caps = caps,
            supportsSecurityTypeDetection = supportsSecurityTypeDetection,
            hasLocationPermission = hasLocationAccess
        )

        val classification = WifiSecurityClassifier.classify(
            WifiClassificationInput(
                hasNetwork = true,
                isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),
                hasCaptivePortal = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL),
                isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                supportsSecurityTypeDetection = supportsSecurityTypeDetection,
                hasLocationPermission = hasLocationAccess,
                securityType = wifiSecurityType
            )
        )

        return WifiSecurityAlert(
            level = classification.level,
            reason = classification.reason,
            message = context.getString(reasonMessageResId(classification.reason)),
            securityType = wifiSecurityType,
            checkedAtMillis = checkedAtMillis,
            requiresLocationPermission = classification.requiresLocationPermission
        )
    }

    private fun hasLocationPermission(context: Application): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
            WifiAlertReason.OPEN_OR_WEP -> R.string.wifi_security_open_or_wep
            WifiAlertReason.UNKNOWN_SECURITY -> R.string.wifi_security_connected_unknown
            WifiAlertReason.SECURE -> R.string.wifi_security_secure
        }
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
    OPEN_OR_WEP,
    UNKNOWN_SECURITY,
    SECURE
}

enum class WifiSecurityType {
    OPEN,
    WEP,
    UNKNOWN,
    SECURE
}

data class WifiSecurityAlert(
    val level: WifiAlertLevel,
    val reason: WifiAlertReason,
    val message: String,
    val securityType: WifiSecurityType? = null,
    val checkedAtMillis: Long = System.currentTimeMillis(),
    val requiresLocationPermission: Boolean = false
)
