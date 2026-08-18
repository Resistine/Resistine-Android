package com.resistine.android.ui.home

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.resistine.android.R
import com.resistine.android.ui.vpn.runtime.VpnRuntimeStatus

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<HomeUiState>()
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        refresh(null)
    }

    fun refresh(vpnRuntimeStatus: VpnRuntimeStatus?) {
        val context = getApplication<Application>().applicationContext
        val vpnConnected = vpnRuntimeStatus?.isRunning == true
        val vpnConnecting = vpnRuntimeStatus?.isConnecting == true
        val wifiConnected = hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        _uiState.value = HomeUiState(
            isProtected = vpnConnected,
            isVpnConnecting = vpnConnecting,
            securityScore = when {
                vpnConnected && wifiConnected -> 96
                vpnConnected || wifiConnected -> 86
                else -> 42
            },
            threatCount = 0,
            lastScanLabel = context.getString(R.string.home_today),
            cards = listOf(
                HomeCardItem(
                    title = context.getString(R.string.menu_wifi_security),
                    summary = context.getString(R.string.home_network_protection),
                    status = if (wifiConnected) context.getString(R.string.home_card_connected) else context.getString(R.string.wifi_security_no_network),
                    iconResId = R.drawable.ic_menu_wifi,
                    destinationFragmentId = R.id.nav_wifi_security,
                    statusColorResId = R.color.rs_status_warning
                ),
                HomeCardItem(
                    title = context.getString(R.string.menu_vpn),
                    summary = context.getString(R.string.home_traffic_protection),
                    status = when {
                        vpnConnected -> context.getString(R.string.home_card_connected)
                        vpnConnecting -> context.getString(R.string.home_vpn_verifying)
                        else -> context.getString(R.string.vpn_status_disconnected)
                    },
                    iconResId = R.drawable.ic_menu_vpn,
                    destinationFragmentId = R.id.nav_vpn,
                    statusColorResId = if (vpnConnected) R.color.rs_status_safe else R.color.rs_status_warning
                ),
                HomeCardItem(
                    title = context.getString(R.string.home_app_scanner),
                    summary = context.getString(R.string.home_installed_apps),
                    status = context.getString(R.string.home_ready),
                    iconResId = R.drawable.ic_menu_apps,
                    destinationFragmentId = R.id.nav_apps,
                    statusColorResId = R.color.rs_status_safe
                ),
                HomeCardItem(
                    title = context.getString(R.string.home_chatbot),
                    summary = context.getString(R.string.home_chatbot_summary),
                    status = if (vpnConnected) context.getString(R.string.home_ready) else context.getString(R.string.home_not_ready),
                    iconResId = R.drawable.baseline_chat_24,
                    destinationFragmentId = R.id.nav_chat,
                    statusColorResId = if (vpnConnected) R.color.rs_status_safe else R.color.rs_status_warning
                )
            )
        )
    }

    private fun hasTransport(transport: Int): Boolean {
        val manager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        return manager.getNetworkCapabilities(network)?.hasTransport(transport) == true
    }
}
