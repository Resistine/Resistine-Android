package com.resistine.android

import android.app.Application
import com.resistine.android.security.SystemEventLogger
import com.resistine.android.security.WazuhAgent
import com.resistine.android.ui.icon.VpnLauncherIconManager
import com.resistine.android.ui.theme.AppThemeManager

class ResistineApp : Application() {

    private lateinit var systemEventLogger: SystemEventLogger

    override fun onCreate() {
        super.onCreate()
        AppThemeManager.applyStoredMode(this)
        VpnLauncherIconManager.initialize(this)
        WazuhAgent.getInstance(this).logAppStart()

        systemEventLogger = SystemEventLogger(this)
        systemEventLogger.start()
    }
}
