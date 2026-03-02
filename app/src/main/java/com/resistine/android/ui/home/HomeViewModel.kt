package com.resistine.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.resistine.android.R

class HomeViewModel : ViewModel() {

    private val _cards = MutableLiveData<List<HomeCardItem>>().apply {
        value = listOf(
            HomeCardItem(
                title = "Wi-Fi Security",
                summary = "Status",
                status = "Scan",
                iconResId = R.drawable.ic_menu_wifi,
                destinationFragmentId = R.id.nav_wifi_security
            ),
            HomeCardItem(
                title = "VPN",
                summary = "Status",
                status = "Control",
                iconResId = R.drawable.ic_menu_vpn,
                destinationFragmentId = R.id.nav_vpn
            ),
            HomeCardItem(
                title = "Applications",
                summary = "Status",
                status = "Enabled",
                iconResId = R.drawable.ic_menu_apps,
                destinationFragmentId = R.id.nav_apps

            ),
            HomeCardItem(
                title = "Chat",
                summary = "Status",
                status = "Online",
                iconResId = R.drawable.baseline_chat_24,
                destinationFragmentId = R.id.nav_chat
            )
        )
    }

    val cards: LiveData<List<HomeCardItem>> = _cards
}
