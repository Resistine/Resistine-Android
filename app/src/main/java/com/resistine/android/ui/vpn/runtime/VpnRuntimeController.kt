package com.resistine.android.ui.vpn.runtime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.resistine.android.MainActivity
import com.resistine.android.R
import com.resistine.android.ui.icon.VpnLauncherIconManager
import com.wireguard.android.backend.TelemetryGoBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

object VpnRuntimeController {
    const val ACTION_CONNECT = "com.resistine.android.action.CONNECT_VPN"
    const val ACTION_DISCONNECT = "com.resistine.android.action.DISCONNECT_VPN"

    private const val PREFERENCES = "vpn_runtime_controller"
    private const val KEY_DESIRED_CONNECTED = "desired_connected"
    private const val NOTIFICATION_CHANNEL_ID = "resistine_vpn"
    private const val NOTIFICATION_ID = 2201

    private val disconnectedStatus = VpnRuntimeStatus(
        mode = VpnRuntimeMode.WIREGUARD_TELEMETRY,
        isRunning = false,
        label = "WireGuard with flow telemetry",
        detail = "Disconnected"
    )
    private val _status = MutableLiveData(disconnectedStatus)
    val status: LiveData<VpnRuntimeStatus> = _status
    private val _message = MutableLiveData(disconnectedStatus.detail)
    val message: LiveData<String> = _message

    private var service: TelemetryGoBackend.VpnService? = null
    private var runtime: VpnRuntime? = null
    private var runtimeObserver: Observer<VpnRuntimeStatus>? = null
    private var scope = newScope()
    private var activeCommand = false

    fun connect(context: Context) {
        persistDesiredState(context, true)
        val intent = Intent(context, TelemetryGoBackend.VpnService::class.java)
            .setAction(ACTION_CONNECT)
        runCatching {
            ContextCompat.startForegroundService(context, intent)
        }.onFailure { error ->
            persistDesiredState(context, false)
            publish(
                disconnectedStatus.copy(
                    detail = error.message ?: "Android did not allow the VPN service to start"
                )
            )
            VpnLauncherIconManager.requestConnected(context, false)
        }
    }

    fun disconnect(context: Context) {
        persistDesiredState(context, false)
        val intent = Intent(context, TelemetryGoBackend.VpnService::class.java)
            .setAction(ACTION_DISCONNECT)
        runCatching { context.startService(intent) }.onFailure {
            publish(disconnectedStatus)
            VpnLauncherIconManager.requestConnected(context, false)
        }
    }

    @JvmStatic
    @Synchronized
    fun onServiceCreated(createdService: TelemetryGoBackend.VpnService) {
        if (service === createdService && runtime != null) return
        service = createdService
        if (scope.coroutineContext[Job]?.isActive != true) {
            scope = newScope()
        }
        createNotificationChannel(createdService)

        val createdRuntime = WireGuardVpnRuntime(
            context = createdService.applicationContext,
            onStatusMessage = _message::postValue,
            onTunnelStarted = {},
            onConnectionConfirmed = {},
            onTunnelDown = {}
        )
        val observer = Observer<VpnRuntimeStatus>(::handleRuntimeStatus)
        runtime = createdRuntime
        runtimeObserver = observer
        createdRuntime.status().observeForever(observer)
        createdRuntime.status().value?.let(::publish)
    }

    @JvmStatic
    @Synchronized
    fun onStartCommand(
        startedService: TelemetryGoBackend.VpnService,
        intent: Intent?
    ): Int {
        onServiceCreated(startedService)
        return when (intent?.action) {
            ACTION_DISCONNECT -> {
                persistDesiredState(startedService, false)
                startForeground(startedService, status.value ?: disconnectedStatus)
                stopRuntime()
                Service.START_NOT_STICKY
            }

            ACTION_CONNECT -> {
                persistDesiredState(startedService, true)
                startForeground(startedService, status.value ?: disconnectedStatus)
                startRuntime()
                Service.START_STICKY
            }

            else -> {
                if (desiredConnected(startedService) || intent == null) {
                    persistDesiredState(startedService, true)
                    startForeground(startedService, status.value ?: disconnectedStatus)
                    startRuntime()
                    Service.START_STICKY
                } else {
                    startedService.stopSelf()
                    Service.START_NOT_STICKY
                }
            }
        }
    }

    @JvmStatic
    @Synchronized
    fun onServiceDestroyed(destroyedService: TelemetryGoBackend.VpnService) {
        if (service !== destroyedService) return
        runtimeObserver?.let { observer -> runtime?.status()?.removeObserver(observer) }
        runtimeObserver = null
        runtime?.close()
        runtime = null
        service = null
        activeCommand = false
        scope.cancel()
        scope = newScope()
        publish(disconnectedStatus)
        VpnLauncherIconManager.requestConnected(destroyedService, false)
    }

    @JvmStatic
    fun onRevoke(service: TelemetryGoBackend.VpnService) {
        persistDesiredState(service, false)
        stopRuntime()
    }

    @Synchronized
    private fun startRuntime() {
        val current = status.value
        if (current?.hasLocalTunnel == true) return
        val currentRuntime = runtime ?: return
        activeCommand = true
        scope.launch { currentRuntime.start() }
    }

    @Synchronized
    private fun stopRuntime() {
        val currentRuntime = runtime
        if (currentRuntime == null) {
            service?.stopSelf()
            return
        }
        activeCommand = true
        scope.launch { currentRuntime.stop() }
    }

    private fun handleRuntimeStatus(runtimeStatus: VpnRuntimeStatus) {
        publish(runtimeStatus)
        val transition = synchronized(this) {
            val currentService = service
            when {
                runtimeStatus.isRunning -> {
                    activeCommand = false
                    RuntimeTransition(currentService, connected = true, stopService = false)
                }

                runtimeStatus.isConnecting -> {
                    RuntimeTransition(currentService, connected = null, stopService = false)
                }

                activeCommand -> {
                    activeCommand = false
                    RuntimeTransition(currentService, connected = false, stopService = true)
                }

                else -> RuntimeTransition(currentService, connected = null, stopService = false)
            }
        }
        val currentService = transition.service
        if (currentService != null) {
            updateNotification(currentService, runtimeStatus)
        }
        currentService?.let {
            transition.connected?.let { connected ->
                persistDesiredState(it, connected)
                VpnLauncherIconManager.requestConnected(it, connected)
            }
            if (transition.stopService) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    it.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    it.stopForeground(true)
                }
                it.stopSelf()
            }
        }
    }

    private fun publish(runtimeStatus: VpnRuntimeStatus) {
        _status.postValue(runtimeStatus)
        _message.postValue(runtimeStatus.detail)
    }

    private fun startForeground(
        service: TelemetryGoBackend.VpnService,
        runtimeStatus: VpnRuntimeStatus
    ) {
        service.startForeground(NOTIFICATION_ID, createNotification(service, runtimeStatus))
    }

    private fun updateNotification(
        service: TelemetryGoBackend.VpnService,
        runtimeStatus: VpnRuntimeStatus
    ) {
        runCatching {
            NotificationManagerCompat.from(service)
                .notify(NOTIFICATION_ID, createNotification(service, runtimeStatus))
        }
    }

    private fun createNotification(
        context: Context,
        runtimeStatus: VpnRuntimeStatus
    ) = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_menu_vpn)
        .setContentTitle(context.getString(R.string.vpn_notification_title))
        .setContentText(runtimeStatus.detail)
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addAction(
            R.drawable.ic_stop_24,
            context.getString(R.string.vpn_disconnect_action),
            PendingIntent.getService(
                context,
                1,
                Intent(context, TelemetryGoBackend.VpnService::class.java)
                    .setAction(ACTION_DISCONNECT),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setOngoing(runtimeStatus.hasLocalTunnel)
        .setOnlyAlertOnce(true)
        .build()

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.vpn_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.vpn_notification_channel_description)
                setShowBadge(false)
            }
        )
    }

    private fun desiredConnected(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(KEY_DESIRED_CONNECTED, false)

    private fun persistDesiredState(context: Context, connected: Boolean) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DESIRED_CONNECTED, connected)
            .apply()
    }

    private fun newScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private data class RuntimeTransition(
        val service: TelemetryGoBackend.VpnService?,
        val connected: Boolean?,
        val stopService: Boolean
    )
}
