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

class WazuhLogger(private val context: Context, private val serverIp: String, private val agentPort: Int = 1514) {

    private var socket: Socket? = null
    private var writer: OutputStream? = null

    // ANTI-REPLAY: Začínáme na aktuálním čase v sekundách, aby bylo číslo vždy vyšší než dřív
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

    suspend fun connectAndStartKeepalive(
        agentId: String,
        rawAgentKey: String,
        agentName: String,
        onStatusUpdate: (String) -> Unit,
        onConnected: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                onStatusUpdate(context.getString(R.string.wazuh_connecting_to_manager, serverIp))
                socket = Socket()
                socket?.connect(InetSocketAddress(serverIp, agentPort), 5000)
                writer = socket?.getOutputStream()

                // 1. STARTUP
                globalCounter++
                val startupText = "#!-agent startup {\"version\":\"Resistine-Android/1.0\"}"
                val startupPacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, startupText, globalCounter)
                writer?.write(packForWazuhTcp(startupPacket))
                writer?.flush()

                delay(2000)

                onConnected()
                onStatusUpdate(context.getString(R.string.wazuh_agent_connected_keepalive))

                // 2. KEEPALIVE LOOP
                while (isActive && socket?.isConnected == true) {
                    globalCounter++

                    val safeAgentName = agentName.replace(" ", "_")

                    val keepaliveText = "#!-Wazuh v4.7.0 Linux $safeAgentName 14.0 Android aarch64\n\nd41d8cd98f00b204e9800998ecf8427e\ndefault\n"
//                    val keepaliveText = "#!-$safeAgentName - Android - Android 14 - arm64\n\nd41d8cd98f00b204e9800998ecf8427e\n"

                    val keepalivePacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, keepaliveText, globalCounter)
                    writer?.write(packForWazuhTcp(keepalivePacket))
                    writer?.flush()

                    delay(30000) // Každých 30 vteřin držíme agenta Active
                }

            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.wazuh_connection_interrupted, e.message ?: "")
                Log.e("WazuhLogger", errorMsg)
                onStatusUpdate(errorMsg)
                disconnect()
            }
        }
    }

    suspend fun sendSingleLog(agentId: String, rawAgentKey: String, logMessage: String) {
        withContext(Dispatchers.IO) {
            if (socket?.isConnected != true || writer == null) {
                Log.e("WazuhLogger", context.getString(R.string.wazuh_cannot_send_log_not_connected))
                return@withContext
            }
            try {
                globalCounter++
                val logPacket = WazuhCrypto.buildPacket(agentId, rawAgentKey, logMessage, globalCounter)

                synchronized(this) {
                    writer?.write(packForWazuhTcp(logPacket))
                    writer?.flush()
                }
                Log.d("WazuhLogger", context.getString(R.string.wazuh_manual_log_sent, globalCounter))
            } catch (e: Exception) {
                Log.e("WazuhLogger", context.getString(R.string.wazuh_error_sending_log, e.message ?: ""))
            }
        }
    }

    fun disconnect() {
        try { socket?.close() } catch (e: Exception) {}
        socket = null
        writer = null
    }
}