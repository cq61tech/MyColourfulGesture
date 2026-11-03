package com.mycolourfulgesture

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class AppSettings(context: Context) {
    private val sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var themeMode by mutableIntStateOf(sp.getInt(KEY_THEME, THEME_SYSTEM))
        private set

    fun updateThemeMode(mode: Int) {
        themeMode = mode
        sp.edit().putInt(KEY_THEME, mode).apply()
    }

    companion object {
        const val FILE = "settings"
        const val KEY_THEME = "theme_mode"
        const val KEY_LOCALE_INIT = "locale_initialized"
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }
}
