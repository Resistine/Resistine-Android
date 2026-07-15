package com.resistine.android

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.resistine.android.databinding.ActivityMainBinding
import com.resistine.android.security.CryptoManager
import com.resistine.android.ui.vpn.VpnViewModel

/**
 * Single-activity shell for the Resistine application.
 *
 * Primary destinations use the Material 3 bottom navigation. Authentication and
 * detail destinations keep the same Navigation Component graph while hiding the
 * bottom bar, so runtime and back-stack behavior remain independent from styling.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private var hasRenderedDestination = false
    private val vpnViewModel: VpnViewModel by viewModels()

    private val primaryDestinations = setOf(
        R.id.nav_home,
        R.id.nav_wifi_security,
        R.id.nav_vpn,
        R.id.nav_apps,
        R.id.nav_chat
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        applySystemBarInsets()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(primaryDestinations + R.id.nav_welcome)

        setupActionBarWithNavController(navController, appBarConfiguration)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.bottomNavigation.setupWithNavController(navController)
        configureDestinationChrome(navController)
    }

    private fun configureDestinationChrome(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isRouter = destination.id == R.id.nav_router
            val isPrimary = destination.id in primaryDestinations

            binding.toolbar.visibility = if (isRouter) View.GONE else View.VISIBLE
            binding.bottomNavigation.visibility = if (isPrimary) View.VISIBLE else View.GONE
            animateDestinationChange()
            invalidateOptionsMenu()
        }
    }

    private fun applySystemBarInsets() {
        val toolbarBaseHeight = resources.getDimensionPixelSize(R.dimen.rs_toolbar_height)
        val bottomNavBaseMargin = resources.getDimensionPixelSize(R.dimen.rs_space_8)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = bars.top)
            binding.toolbar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = toolbarBaseHeight + bars.top
            }
            binding.bottomNavigation.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = bottomNavBaseMargin + bars.bottom
            }
            windowInsets
        }
    }

    private fun animateDestinationChange() {
        val host = binding.navHostFragmentContentMain
        host.animate().cancel()
        if (!hasRenderedDestination) {
            hasRenderedDestination = true
            host.alpha = 1f
            host.translationY = 0f
            return
        }
        host.alpha = 0.72f
        host.translationY = 18f
        host.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220L)
            .start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val destinationId = navController.currentDestination?.id
        val authDestination = destinationId in setOf(
            R.id.nav_router,
            R.id.nav_welcome,
            R.id.nav_email,
            R.id.nav_otp
        )
        val userIsLoggedIn = CryptoManager.loadDecryptedEmail(this) != null

        menu.findItem(R.id.action_login)?.isVisible = !authDestination && !userIsLoggedIn
        menu.findItem(R.id.action_logout)?.isVisible = !authDestination && userIsLoggedIn
        menu.findItem(R.id.action_settings)?.isVisible =
            !authDestination && destinationId != R.id.nav_settings && userIsLoggedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                if (navController.currentDestination?.id != R.id.nav_settings) {
                    navController.navigate(R.id.nav_settings)
                }
                true
            }
            R.id.action_login -> {
                navController.navigate(R.id.nav_email)
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmationDialog(navController)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog(navController: NavController) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_confirmation_title)
            .setMessage(R.string.logout_confirmation_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.action_logout) { _, _ ->
                vpnViewModel.logout(this)
                navController.navigate(R.id.nav_welcome)
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
