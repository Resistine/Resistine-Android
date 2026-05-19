package com.resistine.android.network

import android.content.Context
import android.util.Log
import com.resistine.android.R
import com.resistine.android.security.WazuhCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class WazuhLogger(private val context: Context) {

    private var socket: Socket? = null
    private var writer: OutputStream? = null

    // ANTI-REPLAY: We start at current time in seconds to ensure the number is always higher than before
    private var globalCounter = System.currentTimeMillis() / 1000

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
            onStatusUpdate(context.getString(R.string.wazuh_connecting_to_manager, serverIp))
            socket = Socket()
            socket?.connect(InetSocketAddress(serverIp, agentPort), 5000)
            writer = socket?.getOutputStream()

            // 1. STARTUP
            sendStartup(agentId, rawAgentKey)
            
            delay(1000) // Small delay for manager processing
            true
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.wazuh_connection_interrupted, e.message ?: "")
            Log.e("WazuhLogger", errorMsg)
            onStatusUpdate(errorMsg)
            disconnect()
            false
        }
    }

    fun isConnected(): Boolean = socket != null && socket!!.isConnected && !socket!!.isClosed

    suspend fun sendStartup(agentId: String, rawAgentKey: String) = withContext(Dispatchers.IO) {
        synchronized(this@WazuhLogger) {
            globalCounter++
            val startupText = "#!-agent startup {\"version\":\"Resistine-Android/1.0\"}"
            val startupPacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, startupText, globalCounter)
            writer?.write(packForWazuhTcp(startupPacket))
            writer?.flush()
        }
    }

    suspend fun sendKeepalive(agentId: String, rawAgentKey: String, agentName: String) = withContext(Dispatchers.IO) {
        synchronized(this@WazuhLogger) {
            globalCounter++
            val safeAgentName = agentName.replace(" ", "_")
            val keepaliveText = "#!-Wazuh v4.7.0 Linux $safeAgentName 14.0 Android aarch64\n\nd41d8cd98f00b204e9800998ecf8427e\ndefault\n"
            val keepalivePacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, keepaliveText, globalCounter)
            writer?.write(packForWazuhTcp(keepalivePacket))
            writer?.flush()
            Log.d("WazuhLogger", "Keepalive sent")
        }
    }

    suspend fun connectOneShot(serverIp: String, agentPort: Int, agentId: String, rawAgentKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = Socket()
            socket?.connect(InetSocketAddress(serverIp, agentPort), 10000) // Longer timeout
            writer = socket?.getOutputStream()

            // Startup handshake is mandatory for Wazuh to accept following logs
            val currentCounter = globalCounter + 1
            globalCounter = currentCounter
            
            val startupText = "#!-agent startup {\"version\":\"Resistine-Android/1.0\"}"
            val startupPacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, startupText, currentCounter)
            writer?.write(packForWazuhTcp(startupPacket))
            writer?.flush()
            
            // Critical delay: give the manager time to register the connection
            delay(3000)
            true
        } catch (e: Exception) {
            Log.e("WazuhLogger", "OneShot connection failed: ${e.message}")
            false
        }
    }

    suspend fun sendSingleLog(agentId: String, rawAgentKey: String, logMessage: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (socket?.isConnected != true || writer == null) {
                Log.e("WazuhLogger", context.getString(R.string.wazuh_cannot_send_log_not_connected))
                return@withContext false
            }
            try {
                globalCounter++
                // Prefix the log with '1:WazuhAgent:' as required by Wazuh protocol for log events.
                // '1' indicates a log message, followed by the location/source name.
                val formattedMessage = "1:WazuhAgent: ${logMessage.trim()}"

                val logPacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, formattedMessage, globalCounter)
//                val logPacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, logMessage, globalCounter)

                synchronized(this) {
                    writer?.write(packForWazuhTcp(logPacket))
                    writer?.flush()
                }
                Log.d("WazuhLogger", context.getString(R.string.wazuh_manual_log_sent, globalCounter))
                true
            } catch (e: Exception) {
                Log.e("WazuhLogger", context.getString(R.string.wazuh_error_sending_log, e.message ?: ""))
                false
            }
        }
    }

    fun disconnect() {
        try { socket?.close() } catch (e: Exception) {}
        socket = null
        writer = null
    }
}