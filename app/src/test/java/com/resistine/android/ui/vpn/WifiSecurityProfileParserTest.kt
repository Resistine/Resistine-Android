package com.resistine.android.ui.vpn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSecurityProfileParserTest {

    @Test
    fun `parses wpa3 transition mode distinctly from plain wpa2`() {
        val parsed = WifiSecurityProfileParser.parseCapabilities(
            "[RSN-PSK+SAE-CCMP][MFPC][ESS]"
        )

        assertEquals(WifiSecurityMode.TRANSITION, parsed.profile.mode)
        assertTrue(parsed.isTransitionMode)
    }

    @Test
    fun `parses owe and pmf requirement`() {
        val parsed = WifiSecurityProfileParser.parseCapabilities(
            "[RSN-OWE-CCMP][MFPR][ESS]"
        )

        assertEquals(WifiSecurityMode.OWE, parsed.profile.mode)
        assertEquals(WifiPmfState.REQUIRED, parsed.profile.pmfState)
    }
}
