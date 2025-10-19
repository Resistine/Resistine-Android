package com.resistine.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.resistine.android.R

class HomeViewModel : ViewModel() {

    private val _cards = MutableLiveData<List<HomeCardItem>>().apply {
        value = listOf(
            HomeCardItem(
                title = "Network Status",
                summary = "Status",
                status = "Secure",
                iconResId = R.drawable.ic_menu_vpn,
                destinationFragmentId = R.id.nav_vpn
            ),
            HomeCardItem(
                title = "Applications",
                summary = "Status",
                status = "Enabled",
                iconResId = R.drawable.ic_menu_apps,
                destinationFragmentId = R.id.nav_apps

            )
        )
    }

    val cards: LiveData<List<HomeCardItem>> = _cards
}
