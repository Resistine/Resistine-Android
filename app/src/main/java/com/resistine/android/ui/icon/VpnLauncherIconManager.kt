package com.resistine.android.ui.icon

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

object VpnLauncherIconManager : Application.ActivityLifecycleCallbacks {
    private const val PREFERENCES = "launcher_icon_v2"
    private const val KEY_DESIRED_CONNECTED = "desired_connected"
    private const val KEY_APPLIED_CONNECTED = "applied_connected"
    private const val CONNECTED_ALIAS = "com.resistine.android.LauncherConnected"
    private const val DISCONNECTED_ALIAS = "com.resistine.android.LauncherDisconnected"

    private var application: Application? = null
    private var startedActivities = 0

    @Synchronized
    fun initialize(application: Application) {
        if (this.application === application) return
        this.application?.unregisterActivityLifecycleCallbacks(this)
        this.application = application
        startedActivities = 0
        application.registerActivityLifecycleCallbacks(this)
    }

    @Synchronized
    fun requestConnected(context: Context, connected: Boolean) {
        val appContext = context.applicationContext
        preferences(appContext)
            .edit()
            .putBoolean(KEY_DESIRED_CONNECTED, connected)
            .apply()
        if (startedActivities == 0) {
            applyDesiredState(appContext)
        }
    }

    @Synchronized
    internal fun applyDesiredState(context: Context): Boolean {
        if (startedActivities != 0) return false
        val preferences = preferences(context)
        val connected = preferences.getBoolean(KEY_DESIRED_CONNECTED, false)
        if (preferences.contains(KEY_APPLIED_CONNECTED) &&
            preferences.getBoolean(KEY_APPLIED_CONNECTED, false) == connected
        ) {
            return false
        }

        val packageManager = context.packageManager
        val connectedComponent = ComponentName(context, CONNECTED_ALIAS)
        val disconnectedComponent = ComponentName(context, DISCONNECTED_ALIAS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.setComponentEnabledSettings(
                listOf(
                    PackageManager.ComponentEnabledSetting(
                        connectedComponent,
                        enabledState(connected),
                        PackageManager.DONT_KILL_APP
                    ),
                    PackageManager.ComponentEnabledSetting(
                        disconnectedComponent,
                        enabledState(!connected),
                        PackageManager.DONT_KILL_APP
                    )
                )
            )
        } else {
            val enabledComponent = if (connected) connectedComponent else disconnectedComponent
            val disabledComponent = if (connected) disconnectedComponent else connectedComponent
            packageManager.setComponentEnabledSetting(
                enabledComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                disabledComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        preferences.edit().putBoolean(KEY_APPLIED_CONNECTED, connected).apply()
        return true
    }

    @Synchronized
    internal fun resetForTest(context: Context) {
        preferences(context).edit().clear().commit()
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, CONNECTED_ALIAS),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, DISCONNECTED_ALIAS),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onActivityStarted(activity: Activity) {
        synchronized(this) {
            startedActivities += 1
        }
    }

    override fun onActivityStopped(activity: Activity) {
        synchronized(this) {
            startedActivities = (startedActivities - 1).coerceAtLeast(0)
            if (startedActivities == 0) {
                applyDesiredState(activity.applicationContext)
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private fun enabledState(enabled: Boolean) =
        if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
}
