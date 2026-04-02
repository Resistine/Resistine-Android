package com.resistine.android.network

import android.content.Context
import com.resistine.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class WazuhAuthdManager(private val serverIp: String, private val authPort: Int = 1515) {

    suspend fun registerAndGetKey(context: Context, agentName: String, enrollmentPassword: String = ""): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            var socket: SSLSocket? = null
            try {
                // Wazuh používá vlastní self-signed certifikáty, prozatím musíme věřit všem
                val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAll, SecureRandom())

                // Vytvoření TLS socketu
                socket = sslContext.socketFactory.createSocket(serverIp, authPort) as SSLSocket
                socket.soTimeout = 10000 // 10 sekund timeout
                socket.startHandshake()

                val writer = OutputStreamWriter(socket.outputStream, Charsets.UTF_8)
                val reader = InputStreamReader(socket.inputStream, Charsets.UTF_8)

                // 1. Odeslání registračního payloadu (pozor na zalomení řádku na konci)
                val payload = if (enrollmentPassword.isNotEmpty()) {
                    "OSSEC PASS: $enrollmentPassword OSSEC A:'$agentName'\n"
                } else {
                    "OSSEC A:'$agentName'\n"
                }

                writer.write(payload)
                writer.flush()

                // 2. Čtení odpovědi
                val responseBuffer = CharArray(1024)
                val bytesRead = reader.read(responseBuffer)
                if (bytesRead == -1) throw Exception(context.getString(R.string.wazuh_server_closed_connection))

                val response = String(responseBuffer, 0, bytesRead).trim()

                // 3. Zpracování výsledku
                if (response.startsWith("ERROR") || response.startsWith("ERR")) {
                    throw Exception(context.getString(R.string.wazuh_authd_server_error, response))
                }

                if (response.startsWith("OSSEC K:'")) {
                    // Odstranění hlavičky a koncového apostrofu -> "001 Jmeno 10.0.0.50 a1b2c3d4..."
                    val content = response.substringAfter("OSSEC K:'").substringBeforeLast("'")
                    val parts = content.split(" ")

                    if (parts.size >= 4) {
                        val agentId = parts[0]
                        val agentKey = parts[3] // Získáme čistý hexadecimální klíč
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