package com.resistine.android.network.flow

import org.junit.Assert.assertEquals
import org.junit.Test

class FlowWazuhDeliveryStoreTest {
    @Test
    fun `empty settings default to local queue only`() {
        val store = FlowWazuhDeliveryStore(FakeValues())

        assertEquals(FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY, store.load())
    }

    @Test
    fun `invalid settings fall back to local queue only`() {
        val values = FakeValues().apply {
            putString(FlowWazuhDeliveryStore.KEY_DELIVERY_MODE, "UNKNOWN")
        }

        assertEquals(
            FlowWazuhDeliveryMode.LOCAL_QUEUE_ONLY,
            FlowWazuhDeliveryStore(values).load()
        )
    }

    @Test
    fun `remote delivery requires explicit persistence`() {
        val store = FlowWazuhDeliveryStore(FakeValues())

        store.save(FlowWazuhDeliveryMode.REMOTE_MANAGER)

        assertEquals(FlowWazuhDeliveryMode.REMOTE_MANAGER, store.load())
    }

    private class FakeValues : FlowWazuhDeliveryValues {
        private val values = mutableMapOf<String, String>()

        override fun getString(key: String): String? = values[key]

        override fun putString(key: String, value: String) {
            values[key] = value
        }
    }
}
