package com.resistine.android.ui.integrity

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayIntegrityRepositoryTest {

    @Test
    fun `canonical action is stable across client and verifier`() {
        assertEquals(
            "com.resistine.android|action-id|1750000000000|environment_check",
            PlayIntegrityRepository.canonicalAction(
                packageName = "com.resistine.android",
                actionId = "action-id",
                timestampMillis = 1_750_000_000_000
            )
        )
    }
}
