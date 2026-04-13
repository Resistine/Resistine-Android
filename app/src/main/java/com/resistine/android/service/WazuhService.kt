package com.resistine.android.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.resistine.android.R
import com.resistine.android.network.WazuhLogger
import kotlinx.coroutines.*

class WazuhService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var logger: WazuhLogger? = null
    private val CHANNEL_ID = "WazuhAgentChannel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        if (action == "STOP") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val agentId = intent?.getStringExtra("AGENT_ID") ?: ""
        val agentKey = intent?.getStringExtra("AGENT_KEY") ?: ""
        val agentName = intent?.getStringExtra("AGENT_NAME") ?: ""
        val serverIp = "10.0.0.28" // Recommended to pass via intent or SharedPreferences

        startForeground(NOTIFICATION_ID, createNotification(getString(R.string.wazuh_connecting)))

        logger = WazuhLogger(this, serverIp)

        serviceScope.launch {
            logger?.connectAndStartKeepalive(
                agentId,
                agentKey,
                agentName,
                onStatusUpdate = { status ->
                    updateNotification(status)
                },
                onConnected = {
                    updateNotification(getString(R.string.wazuh_agent_active))
                }
            )
        }

        return START_STICKY
    }

    private fun createNotification(content: String): Notification {
        val stopIntent = Intent(this, WazuhService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.wazuh_notification_title))
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your own icon
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.wazuh_stop), stopPendingIntent)
            .build()
    }

    private fun updateNotification(content: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.wazuh_service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        logger?.disconnect()
        super.onDestroy()
    }
}
