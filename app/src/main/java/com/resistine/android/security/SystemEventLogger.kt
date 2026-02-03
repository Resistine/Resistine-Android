package com.resistine.android.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.BatteryManager

class SystemEventLogger(private val context: Context) {

    private val wazuhAgent = WazuhAgent.getInstance(context)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_USER_PRESENT -> wazuhAgent.logDeviceUnlock()
                Intent.ACTION_BATTERY_CHANGED -> logBatteryState(intent)
                ConnectivityManager.CONNECTIVITY_ACTION -> logNetworkState()
            }
        }
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        context.registerReceiver(receiver, filter)
        // Log initial network state
        logNetworkState()
    }

    fun stop() {
        context.unregisterReceiver(receiver)
    }

    private fun logBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        if (level != -1 && scale != -1) {
            wazuhAgent.logBatteryState(level, scale, isCharging)
        }
    }

    @Suppress("DEPRECATION")
    private fun logNetworkState() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true
        val networkType = activeNetwork?.typeName ?: "None"
        wazuhAgent.logNetworkState(isConnected, networkType)
    }
}
