package com.resistine.android.ui.login

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WireGuardProvisioningConfigFormatterInstrumentedTest {
    @Test
    fun acceptsDnsArrayFromBackend() {
        val result = WireGuardProvisioningConfigFormatter.format(
            jsonConfig = configJson(dns = """["/1.1.1.1", "8.8.8.8"]"""),
            privateKey = "private-key"
        )

        assertTrue(result.contains("DNS = 1.1.1.1, 8.8.8.8"))
    }

    @Test
    fun acceptsDnsStringFromBackend() {
        val result = WireGuardProvisioningConfigFormatter.format(
            jsonConfig = configJson(dns = """"1.1.1.1, 8.8.8.8""""),
            privateKey = "private-key"
        )

        assertTrue(result.contains("DNS = 1.1.1.1, 8.8.8.8"))
    }

    @Test
    fun allowsBackendToOmitDns() {
        val result = WireGuardProvisioningConfigFormatter.format(
            jsonConfig = configJson(dns = null),
            privateKey = "private-key"
        )

        assertFalse(result.contains("DNS ="))
    }

    private fun configJson(dns: String?): String {
        val dnsEntry = dns?.let { ", \"DNS\": $it" }.orEmpty()
        return """
            {
              "Interface": {
                "Address": "10.0.0.2/32"$dnsEntry
              },
              "Peer": {
                "PublicKey": "public-key",
                "AllowedIPs": ["0.0.0.0/0", "::/0"],
                "Endpoint": "vpn.example.com:51820"
              }
            }
        """.trimIndent()
    }
}
