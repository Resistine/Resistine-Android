package com.resistine.android.network

import android.content.Context
import android.util.Log
import com.resistine.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

/**
 * Manager responsible for automatic agent registration via the Wazuh 'authd' service.
 * 
 * It communicates over a secure TLS socket to request a unique Agent ID and Key
 * using the device and user metadata.
 */
class WazuhAuthdManager(
    private val socketFactoryProvider: (String?) -> SSLSocketFactory = ::createSocketFactory
) {

    /**
     * Connects to the Wazuh authd service and performs registration.
     * 
     * @param context Application context for resource access.
     * @param serverIp IP address of the Wazuh Manager.
     * @param authPort Port of the authd service (typically 1515).
     * @param agentName Desired name for the new agent.
     * @param agentGroup A group that already exists on the manager.
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
        agentGroup: String = "default",
        enrollmentPassword: String = "",
        agentIp: String? = null,
        managerCaPem: String? = null
    ): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            var socket: SSLSocket? = null
            try {
                Log.d("WazuhAuth", "Connecting to socket at $serverIp:$authPort...")
                socket = socketFactoryProvider(managerCaPem).createSocket() as SSLSocket
                socket.sslParameters = socket.sslParameters.apply {
                    endpointIdentificationAlgorithm = "HTTPS"
                }
                socket.soTimeout = READ_TIMEOUT_MS
                socket.connect(InetSocketAddress(serverIp, authPort), CONNECT_TIMEOUT_MS)
                
                Log.d("WazuhAuth", "Starting SSL Handshake...")
                socket.startHandshake()
                Log.d("WazuhAuth", "Handshake successful.")

                val writer = OutputStreamWriter(socket.outputStream, Charsets.UTF_8)
                val reader = InputStreamReader(socket.inputStream, Charsets.UTF_8)

                // 1. Prepare and send the registration payload
                val payload = buildEnrollmentPayload(
                    agentName = agentName,
                    agentGroup = agentGroup,
                    enrollmentPassword = enrollmentPassword,
                    agentIp = agentIp
                )
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
                if (response.startsWith("ERROR") || response.startsWith("ERR")) {
                    throw Exception(context.getString(R.string.wazuh_authd_server_error, response))
                }

                val registration = parseEnrollmentResponse(response)
                if (registration == null) {
                    throw Exception(context.getString(R.string.wazuh_unknown_server_response, response))
                }
                return@withContext registration
            } catch (error: Exception) {
                Log.e(TAG, "Wazuh enrollment failed: ${error.message}", error)
                throw error
            } finally {
                socket?.close()
            }
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val READ_TIMEOUT_MS = 15_000
        private const val TAG = "WazuhAuth"

        internal fun buildEnrollmentPayload(
            agentName: String,
            agentGroup: String,
            enrollmentPassword: String,
            agentIp: String?
        ): String {
            require('\n' !in agentName && '\'' !in agentName) { "Invalid agent name" }
            require(agentGroup.isNotBlank() && '\n' !in agentGroup && '\'' !in agentGroup) {
                "Invalid agent group"
            }
            require('\n' !in enrollmentPassword) { "Invalid enrollment password" }
            require(agentIp == null || ('\n' !in agentIp && '\'' !in agentIp)) { "Invalid agent IP" }

            return buildString {
                if (enrollmentPassword.isNotEmpty()) {
                    append("OSSEC PASS: ")
                    append(enrollmentPassword)
                    append('\n')
                }
                append("OSSEC A:'")
                append(agentName)
                append("' G:'")
                append(agentGroup)
                append('\'')
                agentIp?.takeIf { it.isNotBlank() }?.let {
                    append(" IP:'")
                    append(it)
                    append('\'')
                }
                append('\n')
            }
        }

        internal fun parseEnrollmentResponse(response: String): Pair<String, String>? {
            if (!response.startsWith("OSSEC K:'") || !response.endsWith('\'')) return null
            val content = response.removePrefix("OSSEC K:'").dropLast(1)
            val parts = content.split(' ', limit = 4)
            if (parts.size != 4 || parts[0].isBlank() || parts[3].isBlank()) return null
            return parts[0] to parts[3]
        }

        private fun createSocketFactory(managerCaPem: String?): SSLSocketFactory {
            if (managerCaPem.isNullOrBlank()) {
                return SSLContext.getDefault().socketFactory
            }

            val certificate = ByteArrayInputStream(managerCaPem.toByteArray(Charsets.US_ASCII)).use {
                CertificateFactory.getInstance("X.509").generateCertificate(it) as X509Certificate
            }
            certificate.checkValidity()

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null)
                setCertificateEntry("wazuh-manager-ca", certificate)
            }
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            ).apply {
                init(keyStore)
            }
            return SSLContext.getInstance("TLS").apply {
                init(null, trustManagerFactory.trustManagers, SecureRandom())
            }.socketFactory
        }
    }
}
