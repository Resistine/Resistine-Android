package com.resistine.android.ui.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.resistine.android.R
import com.resistine.android.ui.security.SecurityChecksManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen dashboard.
 *
 * It generates and updates the list of dashboard cards based on the
 * current system state (VPN connectivity, Wi-Fi status, App scans, etc.).
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _cards = MutableLiveData<List<HomeCardItem>>()
    /**
     * LiveData holding the current list of [HomeCardItem] to be displayed in the dashboard.
     */
    val cards: LiveData<List<HomeCardItem>> = _cards

    init {
        updateCards()
    }

    /**
     * Refreshes the dashboard cards by checking the current connectivity state and app scan results.
     */
    fun updateCards() {
        val appContext = getApplication<Application>().applicationContext
        val securityManager = SecurityChecksManager(appContext)
        
        viewModelScope.launch(Dispatchers.IO) {
            val isVpnConnected = isVpnActive()
            val isWifi = isWifiActive()
            val hasInternet = isInternetAvailable()

            // 1. Agent Status
            val agentStatusText = if (isVpnConnected) "Connected" else "Requires VPN"

            // 2. Phone Security Score
            val checks = securityManager.performAllChecks()
            val score = securityManager.calculateScore(checks)
            val securityLabel = when {
                score >= 90 -> "Excellent ($score/100)"
                score >= 70 -> "Good ($score/100)"
                score >= 40 -> "Warning ($score/100)"
                else -> "Risk ($score/100)"
            }
            val securityColor = when {
                score >= 70 -> R.color.success_green
                score >= 40 -> R.color.wifi_risk_warning_text
                else -> R.color.error_red
            }

            // 3. Wi-Fi Security Adaptive Status
            val wifiStatus = if (isWifi) {
                if (score > 60) "Secure" else "Risk detected"
            } else {
                appContext.getString(R.string.wifi_security_no_network)
            }
            val wifiColor = if (isWifi && score > 60) R.color.success_green else if (isWifi) R.color.error_red else R.color.wifi_risk_warning_text

            // 4. Apps Adaptive Status
            val appsSummary = getAppScanSummary()
            val appsStatus = if (appsSummary.first == 0) "No scan run" else if (appsSummary.second > 0) "${appsSummary.second} Risks" else "All Safe"
            val appsColor = if (appsSummary.first == 0) R.color.dark_blue else if (appsSummary.second > 0) R.color.error_red else R.color.success_green

            // 5. Chat Adaptive Status
            val chatStatus = if (isVpnConnected && hasInternet) "Online" else "Offline"
            val chatColor = if (isVpnConnected && hasInternet) R.color.success_green else R.color.wifi_risk_warning_text

            val updatedCards = listOf(
                HomeCardItem(
                    title = "Security & Status",
                    summary = "Agent: $agentStatusText",
                    status = securityLabel,
                    iconResId = R.drawable.shield_with_star,
                    destinationFragmentId = R.id.nav_security,
                    statusColorResId = securityColor
                ),
                HomeCardItem(
                    title = appContext.getString(R.string.menu_wifi_security),
                    summary = appContext.getString(R.string.home_card_status),
                    status = wifiStatus,
                    iconResId = R.drawable.ic_menu_wifi,
                    destinationFragmentId = R.id.nav_wifi_security,
                    statusColorResId = wifiColor
                ),
                HomeCardItem(
                    title = appContext.getString(R.string.menu_vpn),
                    summary = appContext.getString(R.string.home_card_status),
                    status = if (isVpnConnected) appContext.getString(R.string.home_card_connected) else appContext.getString(R.string.vpn_status_disconnected),
                    iconResId = R.drawable.ic_menu_vpn,
                    destinationFragmentId = R.id.nav_vpn,
                    statusColorResId = if (isVpnConnected) R.color.success_green else R.color.error_red
                ),
                HomeCardItem(
                    title = appContext.getString(R.string.menu_apps),
                    summary = appContext.getString(R.string.home_card_status),
                    status = appsStatus,
                    iconResId = R.drawable.ic_menu_apps,
                    destinationFragmentId = R.id.nav_apps,
                    statusColorResId = appsColor
                ),
                HomeCardItem(
                    title = appContext.getString(R.string.menu_chat),
                    summary = appContext.getString(R.string.home_card_status),
                    status = chatStatus,
                    iconResId = R.drawable.baseline_chat_24,
                    destinationFragmentId = R.id.nav_chat,
                    statusColorResId = chatColor
                )
            )
            
            _cards.postValue(updatedCards)
        }
    }

    private fun isVpnActive(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    private fun isWifiActive(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && 
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Reads the last app scan results from SharedPreferences.
     * @return A pair of (Total Scanned, Risk Count)
     */
    private fun getAppScanSummary(): Pair<Int, Int> {
        val prefs = getApplication<Application>().getSharedPreferences("deceptive_scan_prefs", 0)
        val lastScanAt = prefs.getLong("last_scan_at", 0L)
        if (lastScanAt == 0L) return Pair(0, 0)
        
        val riskCount = prefs.getInt("last_scan_risk", 0)
        val totalCount = prefs.getInt("last_scan_total", 0)
        return Pair(totalCount, riskCount)
    }
}
