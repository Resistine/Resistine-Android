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

class WazuhAuthdManager() {

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
                // Wazuh uses its own self-signed certificates, for now we must trust all
                val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAll, SecureRandom())

                // Create TLS socket
                Log.d("WazuhAuth", "Connecting to socket at $serverIp:$authPort...")
                socket = sslContext.socketFactory.createSocket(serverIp, authPort) as SSLSocket
                socket.soTimeout = 15000 // 15 seconds timeout
                
                Log.d("WazuhAuth", "Starting SSL Handshake...")
                socket.startHandshake()
                Log.d("WazuhAuth", "Handshake successful.")

                val writer = OutputStreamWriter(socket.outputStream, Charsets.UTF_8)
                val reader = InputStreamReader(socket.inputStream, Charsets.UTF_8)

                // 1. Send registration payload
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

                // 2. Read response
                Log.d("WazuhAuth", "Waiting for server response...")
                val responseBuffer = CharArray(1024)
                val bytesRead = reader.read(responseBuffer)
                if (bytesRead == -1) {
                    Log.e("WazuhAuth", "Server closed connection without data.")
                    throw Exception(context.getString(R.string.wazuh_server_closed_connection))
                }

                val response = String(responseBuffer, 0, bytesRead).trim()
                Log.d("WazuhAuth", "Raw response from server: $response")

                // 3. Process result
                if (response.startsWith("ERROR") || response.startsWith("ERR")) {
                    throw Exception(context.getString(R.string.wazuh_authd_server_error, response))
                }

                if (response.startsWith("OSSEC K:'")) {
                    // Remove header and trailing apostrophe -> "001 Name 10.0.0.50 a1b2c3d4..."
                    val content = response.substringAfter("OSSEC K:'").substringBeforeLast("'")
                    val parts = content.split(" ")

                    if (parts.size >= 4) {
                        val agentId = parts[0]
                        val agentKey = parts[3] // Get clean hexadecimal key
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