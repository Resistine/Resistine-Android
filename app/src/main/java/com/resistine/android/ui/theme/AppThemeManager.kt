package com.resistine.android.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Enumeration of application theme modes.
 */
enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Manager responsible for storing, retrieving, and applying application night/light theme modes.
 */
object AppThemeManager {
    private const val PREFERENCES = "appearance"
    private const val KEY_MODE = "theme_mode"

    /**
     * Retrieves the currently selected [AppThemeMode].
     *
     * @param context Application context.
     * @return Current [AppThemeMode].
     */
    fun current(context: Context): AppThemeMode {
        val stored = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_MODE, AppThemeMode.SYSTEM.name)
        return runCatching { AppThemeMode.valueOf(stored.orEmpty()) }
            .getOrDefault(AppThemeMode.SYSTEM)
    }

    /**
     * Applies the stored theme mode.
     *
     * @param context Application context.
     */
    fun applyStoredMode(context: Context) {
        apply(current(context))
    }

    /**
     * Sets and persists a new theme mode, applying it immediately.
     *
     * @param context Application context.
     * @param mode The [AppThemeMode] to set.
     */
    fun set(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.name)
            .apply()
        apply(mode)
    }

    /**
     * Applies the specified theme mode using [AppCompatDelegate].
     */
    private fun apply(mode: AppThemeMode) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                AppThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
}
