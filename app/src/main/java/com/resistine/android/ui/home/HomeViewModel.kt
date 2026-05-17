package com.resistine.android.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.resistine.android.R

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _cards = MutableLiveData<List<HomeCardItem>>().apply {
        val appContext = application.applicationContext
        value = listOf(
            HomeCardItem(
                title = appContext.getString(R.string.menu_wifi_security),
                summary = appContext.getString(R.string.home_card_status),
                status = appContext.getString(R.string.home_card_scan),
                iconResId = R.drawable.ic_menu_wifi,
                destinationFragmentId = R.id.nav_wifi_security
            ),
            HomeCardItem(
                title = appContext.getString(R.string.menu_vpn),
                summary = appContext.getString(R.string.home_card_status),
                status = appContext.getString(R.string.home_card_control),
                iconResId = R.drawable.ic_menu_vpn,
                destinationFragmentId = R.id.nav_vpn
            ),
            HomeCardItem(
                title = appContext.getString(R.string.menu_apps),
                summary = appContext.getString(R.string.home_card_status),
                status = appContext.getString(R.string.home_card_enabled),
                iconResId = R.drawable.ic_menu_apps,
                destinationFragmentId = R.id.nav_apps

            ),
            HomeCardItem(
                title = appContext.getString(R.string.menu_chat),
                summary = appContext.getString(R.string.home_card_status),
                status = appContext.getString(R.string.home_card_online),
                iconResId = R.drawable.baseline_chat_24,
                destinationFragmentId = R.id.nav_chat
            )
        )
    }

    val cards: LiveData<List<HomeCardItem>> = _cards
}
