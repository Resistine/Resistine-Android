package com.resistine.android.ui.home

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.resistine.android.R
import com.resistine.android.ui.vpn.runtime.VpnRuntimeMode
import com.resistine.android.ui.vpn.runtime.VpnRuntimeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeVpnStatusInstrumentedTest {

    @Test
    fun connectingTunnelIsNotReportedAsConnected() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        lateinit var state: HomeUiState

        instrumentation.runOnMainSync {
            val viewModel = HomeViewModel(context.applicationContext as Application)
            viewModel.refresh(
                VpnRuntimeStatus(
                    mode = VpnRuntimeMode.WIREGUARD_TELEMETRY,
                    isRunning = false,
                    isConnecting = true,
                    label = "WireGuard with flow telemetry",
                    detail = "Waiting for WireGuard peer handshake"
                )
            )
            state = checkNotNull(viewModel.uiState.value)
        }

        assertFalse(state.isProtected)
        assertTrue(state.isVpnConnecting)
        assertEquals(3, state.cards.size)
        assertEquals(
            context.getString(R.string.home_vpn_verifying),
            state.cards.single { it.destinationFragmentId == R.id.nav_vpn }.status
        )
    }

    @Test
    fun confirmedRuntimeIsReportedAsConnected() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        lateinit var state: HomeUiState

        instrumentation.runOnMainSync {
            val viewModel = HomeViewModel(context.applicationContext as Application)
            viewModel.refresh(
                VpnRuntimeStatus(
                    mode = VpnRuntimeMode.WIREGUARD_TELEMETRY,
                    isRunning = true,
                    isConnecting = false,
                    label = "WireGuard with flow telemetry",
                    detail = "Connected; WireGuard handshake confirmed"
                )
            )
            state = checkNotNull(viewModel.uiState.value)
        }

        assertTrue(state.isProtected)
        assertFalse(state.isVpnConnecting)
        assertEquals(
            context.getString(R.string.home_card_connected),
            state.cards.single { it.destinationFragmentId == R.id.nav_vpn }.status
        )
    }
}
