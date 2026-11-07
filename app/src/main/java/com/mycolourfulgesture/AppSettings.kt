package com.mycolourfulgesture

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class IntPref(private val sp: SharedPreferences, private val key: String, private val default: Int) {
    var value by mutableIntStateOf(sp.getInt(key, default))
        private set

    fun set(v: Int) {
        value = v
        sp.edit().putInt(key, v).apply()
    }

    fun reset() = set(default)
}

class BoolPref(private val sp: SharedPreferences, private val key: String, private val default: Boolean) {
    var value by mutableStateOf(sp.getBoolean(key, default))
        private set

    fun set(v: Boolean) {
        value = v
        sp.edit().putBoolean(key, v).apply()
    }

    fun reset() = set(default)
}

class AppSettings(context: Context) {
    private val sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var themeMode by mutableIntStateOf(sp.getInt(KEY_THEME, THEME_SYSTEM))
        private set

    var resumeTick by mutableIntStateOf(0)
        private set

    val dynamicColor = BoolPref(sp, "dynamic_color", true)
    val seedHue = IntPref(sp, "seed_hue", 270)
    val amoled = BoolPref(sp, "amoled", false)

    val dockFloating = BoolPref(sp, "dock_floating", false)
    val dockWidth = IntPref(sp, "dock_width", 90)
    val dockHeight = IntPref(sp, "dock_height", 72)
    val dockOpacity = IntPref(sp, "dock_opacity", 100)
    val dockText = IntPref(sp, "dock_text", 12)
    val dockLabels = BoolPref(sp, "dock_labels", true)
    val dockBottom = IntPref(sp, "dock_bottom", 12)
    val dockAutoHide = BoolPref(sp, "dock_autohide", true)

    fun bumpResume() {
        resumeTick++
    }

    fun updateThemeMode(mode: Int) {
        themeMode = mode
        sp.edit().putInt(KEY_THEME, mode).apply()
    }

    fun resetAppSettings() {
        updateThemeMode(THEME_SYSTEM)
        dynamicColor.reset(); seedHue.reset(); amoled.reset()
        dockFloating.reset(); dockWidth.reset(); dockHeight.reset()
        dockOpacity.reset(); dockText.reset(); dockLabels.reset(); dockBottom.reset(); dockAutoHide.reset()
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
