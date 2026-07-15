package com.resistine.android.ui.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRiskScorerTest {

    @Test
    fun `recognized store app without elevated signals has no concerns`() {
        val score = AppRiskScorer.score(baseInput())

        assertEquals(0, score.value)
        assertEquals(RiskVerdict.NO_CONCERN, score.verdict)
    }

    @Test
    fun `local install alone is context and not an urgent finding`() {
        val score = AppRiskScorer.score(
            baseInput().copy(provenance = InstallProvenance.LOCAL_OR_ADB)
        )

        assertEquals(10, score.value)
        assertEquals(RiskVerdict.NO_CONCERN, score.verdict)
    }

    @Test
    fun `unknown source with active accessibility is urgent review`() {
        val score = AppRiskScorer.score(
            baseInput().copy(
                provenance = InstallProvenance.UNKNOWN,
                accessibilityEnabled = true
            )
        )

        assertEquals(60, score.value)
        assertEquals(RiskVerdict.URGENT_REVIEW, score.verdict)
    }

    @Test
    fun `combined local installer and overlay capabilities are escalated`() {
        val score = AppRiskScorer.score(
            baseInput().copy(
                provenance = InstallProvenance.LOCAL_OR_ADB,
                declaresInstallerCapability = true,
                declaresOverlayCapability = true
            )
        )

        assertEquals(51, score.value)
        assertEquals(RiskVerdict.URGENT_REVIEW, score.verdict)
    }

    @Test
    fun `legacy target sdk is review rather than malware verdict`() {
        val score = AppRiskScorer.score(baseInput().copy(targetSdk = 28))

        assertEquals(10, score.value)
        assertEquals(RiskVerdict.NO_CONCERN, score.verdict)
    }

    @Test
    fun `effective overlay access is weighted more than a declaration`() {
        val declared = AppRiskScorer.score(
            baseInput().copy(declaresOverlayCapability = true)
        )
        val enabled = AppRiskScorer.score(
            baseInput().copy(declaresOverlayCapability = true, overlayEnabled = true)
        )

        assertEquals(8, declared.value)
        assertEquals(20, enabled.value)
        assertEquals(RiskVerdict.REVIEW, enabled.verdict)
    }

    @Test
    fun `unknown app with enabled overlay is escalated for review`() {
        val score = AppRiskScorer.score(
            baseInput().copy(
                provenance = InstallProvenance.UNKNOWN,
                overlayEnabled = true
            )
        )

        assertEquals(40, score.value)
        assertEquals(RiskVerdict.REVIEW, score.verdict)
    }

    private fun baseInput() = AppRiskInput(
        provenance = InstallProvenance.PLAY_STORE,
        targetSdk = 35,
        deviceSdk = 35,
        isDebuggable = false,
        sensitivePermissionWeight = 0,
        accessibilityEnabled = false,
        deviceAdminActive = false,
        notificationAccessEnabled = false,
        overlayEnabled = false,
        usageAccessEnabled = false,
        declaresInstallerCapability = false,
        declaresOverlayCapability = false
    )
}
