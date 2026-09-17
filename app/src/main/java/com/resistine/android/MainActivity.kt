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
import androidx.drawerlayout.widget.DrawerLayout
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
 * Primary destinations use an expandable navigation drawer. Authentication and
 * detail destinations keep the same Navigation Component graph with the drawer
 * locked closed.
 */
class MainActivity : AppCompatActivity() {

    /** View binding instance for activity_main layout. */
    private lateinit var binding: ActivityMainBinding

    /** Configuration for the action bar navigation UI. */
    private lateinit var appBarConfiguration: AppBarConfiguration

    /** Navigation controller managing app navigation graph. */
    private lateinit var navController: NavController

    /** Flag indicating whether the initial destination has finished animating. */
    private var hasRenderedDestination = false

    /** ViewModel controlling VPN lifecycle and session actions. */
    private val vpnViewModel: VpnViewModel by viewModels()

    /** Set of primary top-level navigation destination IDs. */
    private val primaryDestinations = setOf(
        R.id.nav_home
    )

    /** Set of destination IDs that permit access to the navigation drawer. */
    private val drawerDestinations = primaryDestinations + setOf(
//        R.id.nav_settings,
        R.id.nav_log_viewer
    )

    /**
     * Called when the activity is starting. Initializes view bindings, edge-to-edge window insets,
     * toolbar, navigation component, and destination chrome listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
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
        appBarConfiguration = AppBarConfiguration.Builder(
            drawerDestinations + R.id.nav_welcome
        )
            .setOpenableLayout(binding.drawerLayout)
            .build()

        setupActionBarWithNavController(navController, appBarConfiguration)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.navigationView.setupWithNavController(navController)
        binding.homeNavigationButton.setOnClickListener { navigateHome() }
        configureDestinationChrome(navController)
    }

    /**
     * Configures toolbar visibility, drawer lock modes, and home button visibility based on the active destination.
     *
     * @param navController The active navigation controller.
     */
    private fun configureDestinationChrome(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isRouter = destination.id == R.id.nav_router
            val hasDrawer = destination.id in drawerDestinations

            binding.toolbar.visibility = if (isRouter) View.GONE else View.VISIBLE
            binding.drawerLayout.setDrawerLockMode(
                if (hasDrawer) {
                    DrawerLayout.LOCK_MODE_UNLOCKED
                } else {
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                }
            )
            if (!hasDrawer) {
                binding.drawerLayout.closeDrawers()
            }
            binding.homeNavigationButton.visibility =
                if (hasDrawer && destination.id != R.id.nav_home) View.VISIBLE else View.GONE
            if (destination.id == R.id.nav_welcome) {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                binding.toolbar.navigationIcon = null
            }
            animateDestinationChange()
            invalidateOptionsMenu()
        }
    }

    /**
     * Navigates back to the home destination if not already present.
     */
    private fun navigateHome() {
        if (navController.currentDestination?.id == R.id.nav_home) return
        if (!navController.popBackStack(R.id.nav_home, false)) {
            navController.navigate(R.id.nav_home)
        }
    }

    /**
     * Applies system window insets to toolbar, navigation view, and nav host fragments for edge-to-edge support.
     */
    private fun applySystemBarInsets() {
        val toolbarBaseHeight = resources.getDimensionPixelSize(R.dimen.rs_toolbar_height)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            binding.toolbar.updateLayoutParams {
                height = toolbarBaseHeight + bars.top
            }
            binding.navigationView.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom
            )
            binding.navHostFragmentContentMain.updatePadding(bottom = bars.bottom)
            windowInsets
        }
    }

    /**
     * Animates alpha and translation when transitioning between navigation destinations.
     */
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

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu The options menu in which you place your items.
     * @return True for the menu to be displayed; false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.
     *
     * @param menu The options menu as last shown or first initialized.
     * @return True as the menu should be displayed; false otherwise.
     */
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

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was clicked.
     * @return True if the item click was handled, false otherwise.
     */
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

    /**
     * Displays a confirmation dialog before logging out the current user session.
     *
     * @param navController The navigation controller used to redirect to the welcome destination upon logout.
     */
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

    /**
     * This method is called when the user clicks the Up button from the action bar.
     *
     * @return True if navigation was handled successfully.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
