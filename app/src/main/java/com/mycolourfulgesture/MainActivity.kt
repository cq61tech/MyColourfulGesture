package com.mycolourfulgesture

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import com.mycolourfulgesture.ui.AppRoot
import com.mycolourfulgesture.ui.theme.MyColourfulGestureTheme

class MainActivity : AppCompatActivity() {
    private lateinit var settings: AppSettings
    private lateinit var pill: PillState

    override fun onResume() {
        super.onResume()
        if (::settings.isInitialized) settings.bumpResume()
        if (::pill.isInitialized) pill.reload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings(applicationContext)
        pill = PillState(applicationContext)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val dark = when (settings.themeMode) {
                AppSettings.THEME_LIGHT -> false
                AppSettings.THEME_DARK -> true
                else -> systemDark
            }

            DisposableEffect(dark) {
                this@MainActivity.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(
                        AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT
                    ) { dark },
                )
                onDispose { }
            }

            MyColourfulGestureTheme(
                darkTheme = dark,
                dynamic = settings.dynamicColor.value,
                seedHue = settings.seedHue.value,
                amoled = settings.amoled.value,
            ) {
                AppRoot(settings, pill)
            }
        }
    }
}
