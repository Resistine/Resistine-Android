package com.resistine.android.ui.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object VpnLauncherIconManager {
    private const val PREFERENCES = "launcher_icon"
    private const val KEY_CONNECTED = "connected"
    private const val CONNECTED_ALIAS = "com.resistine.android.LauncherConnected"
    private const val DISCONNECTED_ALIAS = "com.resistine.android.LauncherDisconnected"

    @Synchronized
    fun setConnected(context: Context, connected: Boolean) {
        val appContext = context.applicationContext
        val preferences = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (preferences.contains(KEY_CONNECTED) && preferences.getBoolean(KEY_CONNECTED, false) == connected) {
            return
        }
        val packageManager = appContext.packageManager
        setAliasEnabled(packageManager, appContext, CONNECTED_ALIAS, connected)
        setAliasEnabled(packageManager, appContext, DISCONNECTED_ALIAS, !connected)
        preferences.edit().putBoolean(KEY_CONNECTED, connected).apply()
    }

    private fun setAliasEnabled(
        packageManager: PackageManager,
        context: Context,
        alias: String,
        enabled: Boolean
    ) {
        packageManager.setComponentEnabledSetting(
            ComponentName(context, alias),
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP
        )
    }
}
