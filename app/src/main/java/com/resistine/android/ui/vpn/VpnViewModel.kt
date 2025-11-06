package com.resistine.android.ui.vpn

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.resistine.android.security.CryptoManager
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.android.backend.Tunnel.State
import com.wireguard.config.Config

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

    init {
        loadPhoneInfo()
        fetchLocationData()
    }

    fun toggleVpn(context: Context) {
        if (isVpnConnected) {
            disconnectVpn()
        } else {
            connectVpn(context)
        }
    }
    private fun connectVpn(context: Context) {
        if (backend == null)
            backend = GoBackend(context.applicationContext)
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val reason = e::class.java.getDeclaredField("reason").apply { isAccessible = true }.get(e)?.toString()
                        _vpnStatus.postValue("Error connecting VPN: ${reason ?: e.message ?: "Unknown error"}")
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

    private fun getBatteryLevel(context: Context): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1
    }
}
