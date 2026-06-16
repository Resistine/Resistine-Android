package com.resistine.android.ui.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.resistine.android.R

/**
 * ViewModel for the Home screen dashboard.
 * 
 * It generates and updates the list of dashboard cards based on the
 * current system state (VPN connectivity, Wi-Fi status, etc.).
 * 
 * @param application The application context.
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
     * Refreshes the dashboard cards by checking the current connectivity state.
     * Updates [cards] with the new status and colors.
     */
    fun updateCards() {
        val appContext = getApplication<Application>().applicationContext
        
        val isVpnConnected = isVpnActive()
        val vpnStatus = if (isVpnConnected) {
            appContext.getString(R.string.home_card_connected)
        } else {
            appContext.getString(R.string.vpn_status_disconnected)
        }
        val vpnColor = if (isVpnConnected) R.color.success_green else R.color.error_red

        val isWifi = isWifiActive()
        val wifiStatus = if (isWifi) {
            appContext.getString(R.string.wifi_security_type_secure)
        } else {
            appContext.getString(R.string.wifi_security_no_network)
        }
        val wifiColor = if (isWifi) R.color.success_green else R.color.wifi_risk_warning_text

        _cards.value = listOf(
            HomeCardItem(
                title = "Security & Status",
                summary = "Device, IP & Agent",
                status = if (isVpnConnected) "Protected" else "Risk detected",
                iconResId = R.drawable.shield_with_star,
                destinationFragmentId = R.id.nav_security,
                statusColorResId = if (isVpnConnected) R.color.success_green else R.color.error_red
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
                status = vpnStatus,
                iconResId = R.drawable.ic_menu_vpn,
                destinationFragmentId = R.id.nav_vpn,
                statusColorResId = vpnColor
            ),
            HomeCardItem(
                title = appContext.getString(R.string.menu_apps),
                summary = appContext.getString(R.string.home_card_status),
                status = appContext.getString(R.string.home_card_enabled),
                iconResId = R.drawable.ic_menu_apps,
                destinationFragmentId = R.id.nav_apps,
                statusColorResId = R.color.success_green
            ),
            HomeCardItem(
                title = appContext.getString(R.string.menu_chat),
                summary = appContext.getString(R.string.home_card_status),
                status = appContext.getString(R.string.home_card_online),
                iconResId = R.drawable.baseline_chat_24,
                destinationFragmentId = R.id.nav_chat,
                statusColorResId = R.color.success_green
            )
        )
    }

    /**
     * Checks if a VPN transport is currently active.
     */
    private fun isVpnActive(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    /**
     * Checks if a Wi-Fi transport is currently active.
     */
    private fun isWifiActive(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
