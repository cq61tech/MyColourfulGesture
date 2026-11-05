package com.mycolourfulgesture

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val sp = getSharedPreferences(AppSettings.FILE, MODE_PRIVATE)
        if (!sp.getBoolean(AppSettings.KEY_LOCALE_INIT, false)) {
            sp.edit().putBoolean(AppSettings.KEY_LOCALE_INIT, true).apply()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
        }
    }
}
