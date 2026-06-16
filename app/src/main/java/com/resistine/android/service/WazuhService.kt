package com.resistine.android.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.resistine.android.R
import com.resistine.android.database.AppDatabase
import com.resistine.android.network.WazuhConfigManager
import com.resistine.android.network.WazuhLogger
import kotlinx.coroutines.*
import java.net.NetworkInterface

/**
 * A Foreground Service that manages the Wazuh Agent lifecycle.
 * 
 * Responsibilities:
 * - Maintains a persistent connection to the Wazuh Manager.
 * - Sends periodic "Keepalive" signals to report agent health.
 * - Flushes pending system logs from the local database to the manager.
 * - Ensures connection only happens when a secure VPN tunnel is detected.
 */
class WazuhService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var logger: WazuhLogger? = null
    private val CHANNEL_ID = "WazuhAgentChannel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private var isRunning = false

    /**
     * Handles service commands. Supports normal start and custom "STOP" action.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        if (action == "STOP") {
            isRunning = false
            logger?.disconnect()
            serviceScope.coroutineContext.cancelChildren()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (isRunning) {
            return START_STICKY
        }

        // Credentials can be passed via Intent or read from prefs
        val agentId = intent?.getStringExtra("AGENT_ID")
        val agentKey = intent?.getStringExtra("AGENT_KEY")
        val agentName = intent?.getStringExtra("AGENT_NAME")

        val (finalId, finalKey, finalName) = if (agentId.isNullOrEmpty() || agentKey.isNullOrEmpty() || agentName.isNullOrEmpty()) {
            val prefs = getSharedPreferences("wazuh_prefs", Context.MODE_PRIVATE)
            Triple(
                prefs.getString("agent_id", null),
                prefs.getString("agent_key", null),
                prefs.getString("agent_name", null)
            )
        } else {
            Triple(agentId, agentKey, agentName)
        }

        if (finalId.isNullOrEmpty() || finalKey.isNullOrEmpty() || finalName.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        return startWithCredentials(finalId, finalKey, finalName)
    }

    /**
     * Initiates the background loop for agent connectivity and log syncing.
     */
    private fun startWithCredentials(agentId: String, agentKey: String, agentName: String): Int {
        isRunning = true
        val configManager = WazuhConfigManager.getInstance(this)
        val database = AppDatabase.getDatabase(this)
        val logDao = database.logDao()

        startForeground(NOTIFICATION_ID, createNotification(getString(R.string.wazuh_connecting)))

        logger = WazuhLogger(this)

        serviceScope.launch {
            var lastKeepaliveTime = 0L
            val keepaliveInterval = 30000L // 30 seconds

            while (isRunning && isActive) {
                try {
                    val currentLogger = logger ?: WazuhLogger(applicationContext).also { logger = it }
                    
                    if (!currentLogger.isConnected()) {
                        // SECURITY GUARD: Only connect if VPN is active
                        if (!isVpnActive()) {
                            updateNotification(getString(R.string.wazuh_waiting_for_vpn))
                            delay(5000)
                            continue
                        }

                        val connected = currentLogger.connect(
                            configManager.serverIp,
                            configManager.logPort,
                            agentId,
                            agentKey,
                            onStatusUpdate = { status -> updateNotification(status) }
                        )
                        if (connected) {
                            updateNotification(getString(R.string.wazuh_agent_active))
                            lastKeepaliveTime = System.currentTimeMillis()
                        } else {
                            delay(5000)
                            continue
                        }
                    }

                    // Double check VPN before any data transmission
                    if (!isVpnActive()) {
                        currentLogger.disconnect()
                        updateNotification(getString(R.string.wazuh_waiting_for_vpn))
                        delay(5000)
                        continue
                    }

                    // 1. Sync pending system logs from Database to Server
                    val pendingLogs = logDao.getPendingLogs()
                    if (pendingLogs.isNotEmpty()) {
                        var anyFailed = false
                        val uploadedIds = mutableListOf<Long>()

                        for (logEntry in pendingLogs) {
                            val success = currentLogger.sendSingleLog(agentId, agentKey, logEntry.message)
                            if (success) {
                                uploadedIds.add(logEntry.id)
                            } else {
                                anyFailed = true
                                break
                            }
                        }
                        
                        if (uploadedIds.isNotEmpty()) {
                            logDao.deleteLogsByIds(uploadedIds)
                        }
                        
                        if (anyFailed) {
                            currentLogger.disconnect()
                            delay(5000)
                            continue
                        }
                    }

                    // 2. Periodic Keepalive to keep the agent 'Active' in Wazuh Dashboard
                    if (System.currentTimeMillis() - lastKeepaliveTime > keepaliveInterval) {
                        currentLogger.sendKeepalive(agentId, rawAgentKey = agentKey, agentName = agentName)
                        lastKeepaliveTime = System.currentTimeMillis()
                    }

                    delay(2000) // Polling interval

                } catch (e: Exception) {
                    updateNotification(getString(R.string.wazuh_connection_interrupted, e.message ?: "Unknown error"))
                    logger?.disconnect()
                    delay(5000)
                }
            }
        }

        return START_STICKY
    }

    private fun createNotification(content: String): Notification {
        val stopIntent = Intent(this, WazuhService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.wazuh_notification_title))
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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

    /**
     * Inspects active network interfaces to detect a VPN tunnel.
     * Looks for interface names containing 'tun', 'wg', or 'wireguard'.
     */
    private fun isVpnActive(): Boolean {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            networkInterfaces?.asSequence()?.any {
                it.isUp && (it.name.contains("tun") || it.name.contains("wg") || it.name.contains("wireguard"))
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        logger?.disconnect()
        super.onDestroy()
    }
}
