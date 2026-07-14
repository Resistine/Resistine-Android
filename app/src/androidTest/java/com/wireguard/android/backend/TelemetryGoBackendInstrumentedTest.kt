package com.wireguard.android.backend

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.resistine.android.network.flow.PacketTelemetryResult
import com.resistine.android.network.flow.PacketTelemetrySink
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TelemetryGoBackendInstrumentedTest {
    @Test
    fun loadsPinnedNativeBackendThroughJni() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val backend = TelemetryGoBackend(
            context,
            PacketTelemetrySink { _, _, _, _ -> PacketTelemetryResult.ACCEPTED },
            object : TelemetryGoBackend.TelemetryHealthListener {
                override fun onNativePacketDrops(count: Long) = Unit

                override fun onTelemetryReaderFailure(error: Throwable) = Unit
            }
        )

        assertEquals("f333402", backend.version)
    }
}
