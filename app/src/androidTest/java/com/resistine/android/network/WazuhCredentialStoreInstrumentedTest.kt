package com.resistine.android.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WazuhCredentialStoreInstrumentedTest {
    @Test
    fun encryptsAndRestoresCredentialsWithAndroidKeystore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = WazuhCredentialStore(context)
        val previous = store.load()
        val expected = WazuhAgentCredentials("999", "local-test-key", "local-test-agent")

        try {
            store.save(expected)
            assertEquals(expected, store.load())
            store.clear()
            assertNull(store.load())
        } finally {
            if (previous == null) store.clear() else store.save(previous)
        }
    }
}
