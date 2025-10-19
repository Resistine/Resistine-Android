//package com.example.resistineandroidapp.ui.apps
//
//import android.app.Application
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.content.pm.ResolveInfo
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//
//class AppsViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val _apps = MutableLiveData<List<ResolveInfo>>()
//    val apps: LiveData<List<ResolveInfo>> get() = _apps
//
//    init {
//        loadLauncherApps()
//    }
//
//    private fun loadLauncherApps() {
//        val pm = getApplication<Application>().packageManager
//
//        val intent = Intent(Intent.ACTION_MAIN, null)
//        intent.addCategory(Intent.CATEGORY_LAUNCHER)
//
//        val launcherApps = pm.queryIntentActivities(intent, 0)
//            .sortedBy { it.loadLabel(pm).toString() }
//
//        _apps.value = launcherApps
//    }
//}
//
package com.resistine.android.ui.apps

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val _apps = MutableLiveData<List<PackageInfo>>()
    val apps: LiveData<List<PackageInfo>> get() = _apps

    init {
        loadAllPackages()
    }

    private fun loadAllPackages() {
        val pm = getApplication<Application>().packageManager
        val allPackages = pm.getInstalledPackages(0)
            .filter { it.applicationInfo != null } // Add this line to filter out nulls
            .sortedBy {
                if (it.applicationInfo != null) {
                    pm.getApplicationLabel(it.applicationInfo!!).toString()
                } else {
                    // Provide a default name or handle the null case appropriately
                    "Resistine App" // Example default
                }
            }
        _apps.value = allPackages
    }
}

