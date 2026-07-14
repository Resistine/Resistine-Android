package com.resistine.android.network

import android.content.Context
import android.util.Log
import com.resistine.android.R
import com.resistine.android.security.WazuhCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong

class WazuhLogger(private val context: Context) {
    private val connectionLock = Any()
    private val globalCounter = AtomicLong(System.currentTimeMillis() / 1000L)

    @Volatile
    private var socket: Socket? = null

    @Volatile
    private var writer: OutputStream? = null

    private fun packForWazuhTcp(payload: ByteArray): ByteArray {
        val payloadLength = payload.size
        val finalFrame = ByteArray(4 + payloadLength)
        finalFrame[0] = (payloadLength and 0xFF).toByte()
        finalFrame[1] = ((payloadLength shr 8) and 0xFF).toByte()
        finalFrame[2] = ((payloadLength shr 16) and 0xFF).toByte()
        finalFrame[3] = ((payloadLength shr 24) and 0xFF).toByte()
        System.arraycopy(payload, 0, finalFrame, 4, payloadLength)
        return finalFrame
    }

    suspend fun connect(
        serverIp: String,
        agentPort: Int,
        agentId: String,
        rawAgentKey: String,
        onStatusUpdate: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val endpoint = WazuhManagerEndpoint(serverIp.trim(), agentPort, agentPort)
            require(agentId.isNotBlank()) { "Wazuh agent ID is required" }
            require(rawAgentKey.isNotBlank()) { "Wazuh agent key is required" }

            disconnect()
            onStatusUpdate(context.getString(R.string.wazuh_connecting_to_manager, serverIp))
            val connectedSocket = Socket().apply {
                keepAlive = true
                tcpNoDelay = true
                connect(InetSocketAddress(endpoint.host, endpoint.logPort), CONNECT_TIMEOUT_MS)
            }
            synchronized(connectionLock) {
                socket = connectedSocket
                writer = connectedSocket.getOutputStream()
            }

            if (!sendStartup(agentId, rawAgentKey)) {
                return@withContext false
            }

            delay(STARTUP_SETTLE_DELAY_MS)
            true
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.wazuh_connection_interrupted, e.message ?: "")
            Log.e("WazuhLogger", errorMsg)
            onStatusUpdate(errorMsg)
            disconnect()
            false
        }
    }

    fun isConnected(): Boolean {
        val current = socket ?: return false
        return current.isConnected && !current.isClosed && !current.isOutputShutdown
    }

    suspend fun sendStartup(agentId: String, rawAgentKey: String): Boolean = withContext(Dispatchers.IO) {
        sendEncryptedMessage(
            agentId = agentId,
            rawAgentKey = rawAgentKey,
            message = "#!-agent startup {\"version\":\"Resistine-Android/1.0\"}"
        )
    }

    suspend fun sendKeepalive(
        agentId: String,
        rawAgentKey: String,
        agentName: String
    ): Boolean = withContext(Dispatchers.IO) {
        val safeAgentName = agentName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val keepaliveText =
            "#!-Wazuh v4.7.0 Linux $safeAgentName 14.0 Android aarch64\n\n" +
                "d41d8cd98f00b204e9800998ecf8427e\ndefault\n"
        sendEncryptedMessage(agentId, rawAgentKey, keepaliveText).also { sent ->
            if (sent) Log.d("WazuhLogger", "Keepalive sent")
        }
    }

    suspend fun connectOneShot(
        serverIp: String,
        agentPort: Int,
        agentId: String,
        rawAgentKey: String
    ): Boolean = connect(serverIp, agentPort, agentId, rawAgentKey) {}

    suspend fun sendSingleLog(agentId: String, rawAgentKey: String, logMessage: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (!isConnected()) {
                Log.e("WazuhLogger", context.getString(R.string.wazuh_cannot_send_log_not_connected))
                return@withContext false
            }
            val formattedMessage = "1:WazuhAgent: ${logMessage.trim()}"
            if (formattedMessage.toByteArray(Charsets.UTF_8).size > MAX_MESSAGE_BYTES) {
                Log.e("WazuhLogger", "Wazuh log exceeds $MAX_MESSAGE_BYTES bytes")
                return@withContext false
            }
            sendEncryptedMessage(agentId, rawAgentKey, formattedMessage)
        }
    }

    fun disconnect() {
        synchronized(connectionLock) {
            disconnectLocked()
        }
    }

    private fun sendEncryptedMessage(agentId: String, rawAgentKey: String, message: String): Boolean {
        return synchronized(connectionLock) {
            val output = writer ?: return@synchronized false
            if (!isConnected()) return@synchronized false

            try {
                val counter = globalCounter.incrementAndGet()
                val packet = WazuhCrypto.buildPacket(agentId, rawAgentKey, message, counter)
                output.write(packForWazuhTcp(packet))
                output.flush()
                Log.d("WazuhLogger", context.getString(R.string.wazuh_manual_log_sent, counter))
                true
            } catch (error: Exception) {
                Log.e(
                    "WazuhLogger",
                    context.getString(R.string.wazuh_error_sending_log, error.message ?: "")
                )
                disconnectLocked()
                false
            }
        }
    }

    private fun disconnectLocked() {
        runCatching { writer?.close() }
        runCatching { socket?.close() }
        writer = null
        socket = null
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val STARTUP_SETTLE_DELAY_MS = 250L
        const val MAX_MESSAGE_BYTES = 256 * 1024
    }
}
