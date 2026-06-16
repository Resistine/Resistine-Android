package com.resistine.android.network

import android.content.Context
import android.util.Log
import com.resistine.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Manager responsible for automatic agent registration via the Wazuh 'authd' service.
 * 
 * It communicates over a secure TLS socket to request a unique Agent ID and Key
 * using the device and user metadata.
 */
class WazuhAuthdManager() {

    /**
     * Connects to the Wazuh authd service and performs registration.
     * 
     * @param context Application context for resource access.
     * @param serverIp IP address of the Wazuh Manager.
     * @param authPort Port of the authd service (typically 1515).
     * @param agentName Desired name for the new agent.
     * @param userEmail Email associated with the agent (used for grouping).
     * @param enrollmentPassword Optional password if authd is password-protected.
     * @param agentIp Optional specific IP to register for the agent.
     * @return A [Pair] containing the (Agent ID, Agent Key).
     * @throws Exception if connection fails, handshake fails, or server returns an error.
     */
    suspend fun registerAndGetKey(
        context: Context,
        serverIp: String,
        authPort: Int,
        agentName: String,
        userEmail: String,
        enrollmentPassword: String = "",
        agentIp: String? = null
    ): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            var socket: SSLSocket? = null
            try {
                // SSL SETUP: Wazuh often uses self-signed certs. 
                // Currently trusting all to facilitate dynamic registration.
                val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAll, SecureRandom())

                // TLS Socket Initialization
                Log.d("WazuhAuth", "Connecting to socket at $serverIp:$authPort...")
                socket = sslContext.socketFactory.createSocket(serverIp, authPort) as SSLSocket
                socket.soTimeout = 15000 // 15 seconds timeout
                
                Log.d("WazuhAuth", "Starting SSL Handshake...")
                socket.startHandshake()
                Log.d("WazuhAuth", "Handshake successful.")

                val writer = OutputStreamWriter(socket.outputStream, Charsets.UTF_8)
                val reader = InputStreamReader(socket.inputStream, Charsets.UTF_8)

                // 1. Prepare and send the registration payload
                val group = userEmail.replace("@", "-")
                val ip = agentIp
                val payload = if (enrollmentPassword.isNotEmpty()) {
                    "OSSEC PASS: $enrollmentPassword\nOSSEC A:'$agentName' G:'$group' IP:'$ip'\n"
                } else {
                    "OSSEC A:'$agentName' G:'$group' IP:'$ip'\n"
                }

                Log.d("WazuhAuth", "Sending payload: ${payload.trim()}")
                writer.write(payload)
                writer.flush()

                // 2. Read and parse the server response
                Log.d("WazuhAuth", "Waiting for server response...")
                val responseBuffer = CharArray(1024)
                val bytesRead = reader.read(responseBuffer)
                if (bytesRead == -1) {
                    Log.e("WazuhAuth", "Server closed connection without data.")
                    throw Exception(context.getString(R.string.wazuh_server_closed_connection))
                }

                val response = String(responseBuffer, 0, bytesRead).trim()
                Log.d("WazuhAuth", "Raw response from server: $response")

                // 3. Process registration result
                if (response.startsWith("ERROR") || response.startsWith("ERR")) {
                    throw Exception(context.getString(R.string.wazuh_authd_server_error, response))
                }

                if (response.startsWith("OSSEC K:'")) {
                    // Extract data from format: OSSEC K:'ID Name IP KEY'
                    val content = response.substringAfter("OSSEC K:'").substringBeforeLast("'")
                    val parts = content.split(" ")

                    if (parts.size >= 4) {
                        val agentId = parts[0]
                        val agentKey = parts[3] 
                        return@withContext Pair(agentId, agentKey)
                    } else {
                        throw Exception(context.getString(R.string.wazuh_invalid_key_format, content))
                    }
                } else {
                    throw Exception(context.getString(R.string.wazuh_unknown_server_response, response))
                }
            } finally {
                socket?.close()
            }
        }
    }
}
