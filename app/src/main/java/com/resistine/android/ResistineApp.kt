package com.resistine.android

import android.app.Application
import com.resistine.android.security.SystemEventLogger
import com.resistine.android.security.WazuhAgent
import com.resistine.android.ui.icon.VpnLauncherIconManager
import com.resistine.android.ui.theme.AppThemeManager

/**
 * Custom [Application] class for the Resistine application.
 *
 * Initializes application-wide services, theme management, dynamic app launcher icon manager,
 * Wazuh security agent logging, and system event observation on startup.
 */
class ResistineApp : Application() {

    /** Logger responsible for monitoring and recording system events. */
    private lateinit var systemEventLogger: SystemEventLogger

    /**
     * Called when the application is starting, before any activity, service, or receiver objects have been created.
     * Applies stored theme preferences, initializes launcher icons, logs app start, and starts system event logging.
     */
    override fun onCreate() {
        super.onCreate()
        AppThemeManager.applyStoredMode(this)
        VpnLauncherIconManager.initialize(this)
        WazuhAgent.getInstance(this).logAppStart()

        systemEventLogger = SystemEventLogger(this)
        systemEventLogger.start()
    }
}
