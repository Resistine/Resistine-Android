package com.resistine.android.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WazuhEnrollmentSecretStoreInstrumentedTest {
    @Test
    fun encryptsAndRestoresPasswordWithAndroidKeystore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = WazuhEnrollmentSecretStore(context)
        val previous = store.loadPassword()

        try {
            store.savePassword("local-enrollment-test")
            assertTrue(store.hasPassword())
            assertEquals("local-enrollment-test", store.loadPassword())

            store.clear()
            assertFalse(store.hasPassword())
            assertEquals("", store.loadPassword())
        } finally {
            if (previous.isEmpty()) store.clear() else store.savePassword(previous)
        }
    }
}
