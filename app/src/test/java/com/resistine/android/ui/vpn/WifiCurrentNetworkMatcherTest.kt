package com.resistine.android.ui.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiCurrentNetworkMatcherTest {

    @Test
    fun `uses verified bssid match when available`() {
        val match = WifiCurrentNetworkMatcher.match(
            currentSsid = "Cafe WiFi",
            currentBssid = "aa:bb:cc:dd:ee:ff",
            scanResults = listOf(
                WifiScanNetworkSnapshot(
                    ssid = "Cafe WiFi",
                    bssid = "aa:bb:cc:dd:ee:ff",
                    capabilities = "[RSN-SAE-CCMP][MFPR][ESS]"
                )
            )
        )

        assertEquals(WifiMatchConfidence.VERIFIED_BSSID, match.confidence)
        assertEquals("aa:bb:cc:dd:ee:ff", match.matchedSnapshot?.bssid)
    }

    @Test
    fun `marks ssid only match as ambiguous when multiple same name aps exist`() {
        val match = WifiCurrentNetworkMatcher.match(
            currentSsid = "Cafe WiFi",
            currentBssid = null,
            scanResults = listOf(
                WifiScanNetworkSnapshot(
                    ssid = "Cafe WiFi",
                    bssid = "aa:bb:cc:dd:ee:01",
                    capabilities = "[ESS]"
                ),
                WifiScanNetworkSnapshot(
                    ssid = "Cafe WiFi",
                    bssid = "aa:bb:cc:dd:ee:02",
                    capabilities = "[RSN-PSK-CCMP][ESS]"
                )
            )
        )

        assertEquals(WifiMatchConfidence.AMBIGUOUS_SSID, match.confidence)
        assertEquals(2, match.sameSsidCandidates)
        assertNull(match.matchedSnapshot)
    }
}
