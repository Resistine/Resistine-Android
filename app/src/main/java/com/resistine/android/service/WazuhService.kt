package com.resistine.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.resistine.android.R
import com.resistine.android.database.AppDatabase
import com.resistine.android.network.WazuhAgentCredentials
import com.resistine.android.network.WazuhAuthdManager
import com.resistine.android.network.WazuhConfigManager
import com.resistine.android.network.WazuhConnectionMonitor
import com.resistine.android.network.WazuhConnectionState
import com.resistine.android.network.WazuhCredentialStore
import com.resistine.android.network.WazuhEnrollmentSecretStore
import com.resistine.android.network.WazuhLogger
import com.resistine.android.network.WazuhRemoteReadinessValidator
import com.resistine.android.network.WazuhRetryPolicy
import java.net.NetworkInterface
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WazuhService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val retryPolicy = WazuhRetryPolicy()
    private val credentialStore by lazy { WazuhCredentialStore(this) }

    @Volatile
    private var isRunning = false

    @Volatile
    private var logger: WazuhLogger? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopUploader()
            return START_NOT_STICKY
        }
        if (isRunning) return START_STICKY

        isRunning = true
        startForeground(NOTIFICATION_ID, createNotification(getString(R.string.wazuh_connecting)))

        val providedCredentials = credentialsFromIntent(intent)
        if (providedCredentials != null) {
            runCatching { credentialStore.save(providedCredentials) }
        }

        serviceScope.launch {
            val credentials = providedCredentials ?: credentialStore.load() ?: enrollAgent()
            if (credentials == null || !isActive || !isRunning) {
                if (isRunning) stopUploader(updateMonitor = false)
                return@launch
            }
            runUploadLoop(credentials)
        }

        return START_STICKY
    }

    private suspend fun enrollAgent(): WazuhAgentCredentials? {
        val configManager = WazuhConfigManager.getInstance(this)
        var failures = 0

        while (isRunning) {
            if (!isVpnActive()) {
                updateStatus(WazuhConnectionState.WAITING_FOR_VPN, "Waiting for an active VPN interface")
                updateNotification(getString(R.string.wazuh_waiting_for_vpn))
                delay(VPN_CHECK_DELAY_MS)
                continue
            }

            val endpoint = runCatching { configManager.endpoint() }.getOrElse { error ->
                updateStatus(WazuhConnectionState.ERROR, error.message ?: "Invalid manager endpoint")
                return null
            }
            val readiness = WazuhRemoteReadinessValidator.validate(endpoint, configManager.managerCaPem)
            if (!readiness.ready) {
                updateStatus(WazuhConnectionState.ERROR, readiness.issues.joinToString("; "))
                return null
            }

            updateStatus(
                WazuhConnectionState.ENROLLING,
                "Enrolling with ${endpoint.host}:${endpoint.authPort}"
            )
            val agentName = buildAgentName()
            val enrolled = runCatching {
                val (agentId, agentKey) = WazuhAuthdManager().registerAndGetKey(
                    context = this,
                    serverIp = endpoint.host,
                    authPort = endpoint.authPort,
                    agentName = agentName,
                    enrollmentPassword = WazuhEnrollmentSecretStore(this).loadPassword(),
                    agentIp = "any",
                    managerCaPem = configManager.managerCaPem
                )
                WazuhAgentCredentials(agentId, agentKey, agentName)
            }

            enrolled.getOrNull()?.let { credentials ->
                runCatching { credentialStore.save(credentials) }.onFailure { error ->
                    updateStatus(
                        WazuhConnectionState.ERROR,
                        "Agent enrolled but credentials could not be secured: ${error.message}"
                    )
                    return null
                }
                delay(MANAGER_SYNC_DELAY_MS)
                return credentials
            }

            failures++
            val detail = enrolled.exceptionOrNull()?.message ?: "Enrollment failed"
            updateStatus(WazuhConnectionState.ERROR, detail)
            delay(retryDelayWithJitter(failures))
        }
        return null
    }

    private suspend fun runUploadLoop(credentials: WazuhAgentCredentials) {
        val configManager = WazuhConfigManager.getInstance(this)
        val logDao = AppDatabase.getDatabase(this).logDao()
        var consecutiveFailures = 0
        var lastKeepaliveTime = 0L

        while (isRunning) {
            try {
                val currentLogger = logger ?: WazuhLogger(applicationContext).also { logger = it }

                if (!isVpnActive()) {
                    currentLogger.disconnect()
                    updateStatus(WazuhConnectionState.WAITING_FOR_VPN, "VPN interface was lost")
                    updateNotification(getString(R.string.wazuh_waiting_for_vpn))
                    delay(VPN_CHECK_DELAY_MS)
                    continue
                }

                if (!currentLogger.isConnected()) {
                    val endpoint = configManager.endpoint()
                    updateStatus(
                        WazuhConnectionState.CONNECTING,
                        "Connecting to ${endpoint.host}:${endpoint.logPort}"
                    )
                    val connected = currentLogger.connect(
                        endpoint.host,
                        endpoint.logPort,
                        credentials.agentId,
                        credentials.agentKey,
                        onStatusUpdate = ::updateNotification
                    )
                    if (!connected) {
                        throw IllegalStateException("Could not connect to ${endpoint.host}:${endpoint.logPort}")
                    }
                    consecutiveFailures = 0
                    lastKeepaliveTime = System.currentTimeMillis()
                    updateStatus(
                        WazuhConnectionState.CONNECTED,
                        "Connected to ${endpoint.host}:${endpoint.logPort}"
                    )
                    updateNotification(getString(R.string.wazuh_agent_active))
                }

                val uploadedIds = mutableListOf<Long>()
                for (logEntry in logDao.getPendingLogs()) {
                    if (!currentLogger.sendSingleLog(
                            credentials.agentId,
                            credentials.agentKey,
                            logEntry.message
                        )
                    ) {
                        throw IllegalStateException("Manager connection failed while uploading logs")
                    }
                    uploadedIds += logEntry.id
                }
                if (uploadedIds.isNotEmpty()) logDao.deleteLogsByIds(uploadedIds)

                if (System.currentTimeMillis() - lastKeepaliveTime >= KEEPALIVE_INTERVAL_MS) {
                    val sent = currentLogger.sendKeepalive(
                        credentials.agentId,
                        credentials.agentKey,
                        credentials.agentName
                    )
                    if (!sent) throw IllegalStateException("Wazuh keepalive failed")
                    lastKeepaliveTime = System.currentTimeMillis()
                }

                consecutiveFailures = 0
                delay(QUEUE_POLL_INTERVAL_MS)
            } catch (error: Exception) {
                logger?.disconnect()
                consecutiveFailures++
                updateStatus(
                    WazuhConnectionState.ERROR,
                    error.message ?: "Unknown Wazuh connection error"
                )
                updateNotification(
                    getString(
                        R.string.wazuh_connection_interrupted,
                        error.message ?: "Unknown error"
                    )
                )
                delay(retryDelayWithJitter(consecutiveFailures))
            }
        }
    }

    private fun credentialsFromIntent(intent: Intent?): WazuhAgentCredentials? {
        val id = intent?.getStringExtra(EXTRA_AGENT_ID)
        val key = intent?.getStringExtra(EXTRA_AGENT_KEY)
        val name = intent?.getStringExtra(EXTRA_AGENT_NAME)
        if (id.isNullOrBlank() || key.isNullOrBlank() || name.isNullOrBlank()) return null
        return WazuhAgentCredentials(id, key, name)
    }

    private fun buildAgentName(): String {
        val model = Build.MODEL.replace(Regex("[^A-Za-z0-9.-]"), "_").take(32)
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            .orEmpty()
            .takeLast(8)
            .ifBlank { "unknown" }
        return "resistine-$model-$deviceId"
    }

    private fun retryDelayWithJitter(failures: Int): Long {
        val base = retryPolicy.delayMillis(failures)
        val jitter = Random.nextLong(0L, (base / 4L).coerceAtLeast(1L))
        return base + jitter
    }

    private fun updateStatus(state: WazuhConnectionState, detail: String) {
        WazuhConnectionMonitor.update(state, detail)
    }

    private fun stopUploader(updateMonitor: Boolean = true) {
        isRunning = false
        logger?.disconnect()
        logger = null
        if (updateMonitor) updateStatus(WazuhConnectionState.STOPPED, "Uploader stopped")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(content: String): Notification {
        val stopIntent = Intent(this, WazuhService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.wazuh_notification_title))
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.wazuh_stop),
                stopPendingIntent
            )
            .build()
    }

    private fun updateNotification(content: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, createNotification(content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.wazuh_service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun isVpnActive(): Boolean {
        return runCatching {
            NetworkInterface.getNetworkInterfaces()?.asSequence()?.any { networkInterface ->
                val name = networkInterface.name.lowercase(Locale.US)
                networkInterface.isUp &&
                    (name.contains("tun") || name.contains("wg") || name.contains("wireguard"))
            } ?: false
        }.getOrDefault(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        logger?.disconnect()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "STOP"
        const val EXTRA_AGENT_ID = "AGENT_ID"
        const val EXTRA_AGENT_KEY = "AGENT_KEY"
        const val EXTRA_AGENT_NAME = "AGENT_NAME"

        private const val CHANNEL_ID = "WazuhAgentChannel"
        private const val NOTIFICATION_ID = 101
        private const val VPN_CHECK_DELAY_MS = 5_000L
        private const val MANAGER_SYNC_DELAY_MS = 2_000L
        private const val QUEUE_POLL_INTERVAL_MS = 2_000L
        private const val KEEPALIVE_INTERVAL_MS = 30_000L
    }
}
