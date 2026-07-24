package com.resistine.android.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object AppThemeManager {
    private const val PREFERENCES = "appearance"
    private const val KEY_MODE = "theme_mode"

    fun current(context: Context): AppThemeMode {
        val stored = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_MODE, AppThemeMode.SYSTEM.name)
        return runCatching { AppThemeMode.valueOf(stored.orEmpty()) }
            .getOrDefault(AppThemeMode.SYSTEM)
    }

    fun applyStoredMode(context: Context) {
        apply(current(context))
    }

    fun set(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.name)
            .apply()
        apply(mode)
    }

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
