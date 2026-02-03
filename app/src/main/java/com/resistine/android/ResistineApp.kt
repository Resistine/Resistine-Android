package com.resistine.android

import android.app.Application
import com.resistine.android.security.SystemEventLogger
import com.resistine.android.security.WazuhAgent

class ResistineApp : Application() {

    private lateinit var systemEventLogger: SystemEventLogger

    override fun onCreate() {
        super.onCreate()
        WazuhAgent.getInstance(this).logAppStart()

        systemEventLogger = SystemEventLogger(this)
        systemEventLogger.start()
    }
}
