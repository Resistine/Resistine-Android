package com.resistine.android.ui

import android.graphics.Bitmap
import android.content.ComponentName
import android.content.pm.PackageManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.resistine.android.MainActivity
import com.resistine.android.R
import com.resistine.android.ui.icon.VpnLauncherIconManager
import com.resistine.android.ui.theme.AppThemeManager
import com.resistine.android.ui.theme.AppThemeMode
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class MainActivityUiInstrumentedTest {

    @Before
    fun resetLauncherIcon() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        VpnLauncherIconManager.resetForTest(context)
    }

    @After
    fun restoreDefaults() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        AppThemeManager.set(context, AppThemeMode.SYSTEM)
        VpnLauncherIconManager.resetForTest(context)
    }

    @Test
    fun primaryDestinationsUseExpandableDrawer() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                navController(activity).navigate(R.id.nav_home)
            }
            onView(withId(R.id.drawerLayout)).check(matches(isDisplayed()))

            scenario.onActivity { activity ->
                activity.findViewById<DrawerLayout>(R.id.drawerLayout)
                    .openDrawer(GravityCompat.START, false)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.navigationView)).check(matches(isDisplayed()))
            onView(withId(R.id.nav_chat)).perform(click())
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            Thread.sleep(500)
            onView(withId(R.id.homeNavigationButton)).check(matches(isDisplayed()))

            scenario.onActivity { activity ->
                val drawer = activity.findViewById<DrawerLayout>(R.id.drawerLayout)
                assertFalse(drawer.isDrawerOpen(GravityCompat.START))
                assertEquals(R.id.nav_chat, navController(activity).currentDestination?.id)
                assertEquals(
                    DrawerLayout.LOCK_MODE_UNLOCKED,
                    drawer.getDrawerLockMode(GravityCompat.START)
                )
                navController(activity).navigate(R.id.nav_settings)
                assertEquals(
                    DrawerLayout.LOCK_MODE_UNLOCKED,
                    drawer.getDrawerLockMode(GravityCompat.START)
                )
            }
            onView(withId(R.id.themeModeGroup)).check(matches(isDisplayed()))
            onView(withId(R.id.homeNavigationButton)).perform(click())
            scenario.onActivity { activity ->
                assertEquals(R.id.nav_home, navController(activity).currentDestination?.id)
            }
        }
    }

    @Test
    fun markdownRendererRemovesSourceMarkersAndKeepsContent() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val textView = TextView(context)
        val markwon = Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .build()

        markwon.setMarkdown(
            textView,
            "# Network review\n\n**VPN active**\n\n- DNS protected\n- TCP routed"
        )

        val rendered = textView.text.toString()
        assertTrue(rendered.contains("Network review"))
        assertTrue(rendered.contains("VPN active"))
        assertTrue(rendered.contains("DNS protected"))
        assertFalse(rendered.contains("**"))
        assertFalse(rendered.contains("# Network"))
    }

    @Test
    fun themeChoicePersists() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        AppThemeManager.set(context, AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, AppThemeManager.current(context))
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun launcherStatusChangeWaitsUntilActivityStops() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val connected = ComponentName(context, "com.resistine.android.LauncherConnected")
        val disconnected = ComponentName(context, "com.resistine.android.LauncherDisconnected")
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity {
            VpnLauncherIconManager.requestConnected(context, true)
            assertEquals(
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                context.packageManager.getComponentEnabledSetting(connected)
            )
            assertEquals(
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                context.packageManager.getComponentEnabledSetting(disconnected)
            )
        }

        scenario.close()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            context.packageManager.getComponentEnabledSetting(connected)
        )
        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            context.packageManager.getComponentEnabledSetting(disconnected)
        )
    }

    @Test
    fun capturesPrimarySurfacesForVisualReview() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            navigateAndCapture(scenario, R.id.nav_chat, "chat-light.png")
            navigateAndCapture(scenario, R.id.nav_wifi_security, "wifi-light.png")
            scenario.onActivity { activity ->
                AppThemeManager.set(activity, AppThemeMode.DARK)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            navigateAndCapture(scenario, R.id.nav_settings, "settings-dark.png")
        }
    }

    private fun navController(activity: MainActivity) =
        (activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            as NavHostFragment).navController

    private fun navigateAndCapture(
        scenario: ActivityScenario<MainActivity>,
        destinationId: Int,
        fileName: String
    ) {
        scenario.onActivity { activity ->
            if (navController(activity).currentDestination?.id != destinationId) {
                navController(activity).navigate(destinationId)
            }
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        Thread.sleep(500)
        val targetContext = instrumentation.targetContext
        val outputDirectory = File(targetContext.getExternalFilesDir(null), "ui-test-screens")
        assertTrue(outputDirectory.exists() || outputDirectory.mkdirs())
        FileOutputStream(File(outputDirectory, fileName)).use { output ->
            assertTrue(instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

}
