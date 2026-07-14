package com.resistine.android.ui.login

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.resistine.android.ui.vpn.runtime.WireGuardConfigReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoredWireGuardConfigInstrumentedTest {
    @Test
    fun parsesStoredProvisioningConfigWithoutExposingKeys() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = requireNotNull(WireGuardConfigReader.loadConfig(context)) {
            "No stored WireGuard configuration. Complete OTP login before running this check."
        }
        val interfaceConfig = config.`interface`
        val peers = config.peers
        assertEquals("Expected exactly one WireGuard peer", 1, peers.size)

        val addresses = interfaceConfig.addresses.map { "${it.address.hostAddress}/${it.mask}" }
        val dnsServers = interfaceConfig.dnsServers.mapNotNull { it.hostAddress }
        val peer = peers.single()
        val allowedIps = peer.allowedIps.map { "${it.address.hostAddress}/${it.mask}" }
        val endpoint = peer.endpoint.orElse(null)?.toString().orEmpty()

        assertTrue("Stored WireGuard address is missing", addresses.isNotEmpty())
        assertTrue("Stored WireGuard AllowedIPs are missing", allowedIps.isNotEmpty())
        assertTrue("Stored WireGuard endpoint is missing", endpoint.isNotBlank())

        Log.i(
            TAG,
            "addresses=$addresses dns=$dnsServers allowedIps=$allowedIps endpoint=$endpoint"
        )
    }

    private companion object {
        private const val TAG = "StoredWgConfig"
    }
}
