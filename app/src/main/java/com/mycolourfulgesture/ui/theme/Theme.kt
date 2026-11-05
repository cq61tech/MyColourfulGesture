package com.mycolourfulgesture.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyColourfulGestureTheme(
    darkTheme: Boolean,
    dynamic: Boolean,
    seedHue: Int,
    amoled: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var scheme = when {
        dynamic && darkTheme -> dynamicDarkColorScheme(context)
        dynamic -> dynamicLightColorScheme(context)
        else -> seedScheme(seedHue.toFloat(), darkTheme)
    }
    if (darkTheme && amoled) {
        scheme = scheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF070707),
            surfaceContainer = Color(0xFF101010),
            surfaceContainerHigh = Color(0xFF181818),
            surfaceContainerHighest = Color(0xFF202020),
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

private fun hsv(h: Float, s: Float, v: Float): Color =
    Color.hsv(((h % 360f) + 360f) % 360f, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))

private fun seedScheme(h: Float, dark: Boolean): ColorScheme =
    if (dark) {
        darkColorScheme(
            primary = hsv(h, .45f, .95f), onPrimary = hsv(h, .80f, .25f),
            primaryContainer = hsv(h, .60f, .40f), onPrimaryContainer = hsv(h, .20f, .98f),
            secondary = hsv(h + 20f, .25f, .85f), onSecondary = hsv(h + 20f, .50f, .22f),
            secondaryContainer = hsv(h + 20f, .35f, .33f), onSecondaryContainer = hsv(h + 20f, .12f, .95f),
            tertiary = hsv(h + 60f, .40f, .90f), onTertiary = hsv(h + 60f, .70f, .25f),
            tertiaryContainer = hsv(h + 60f, .50f, .38f), onTertiaryContainer = hsv(h + 60f, .15f, .97f),
            background = hsv(h, .12f, .10f), onBackground = hsv(h, .05f, .93f),
            surface = hsv(h, .12f, .10f), onSurface = hsv(h, .05f, .93f),
            surfaceVariant = hsv(h, .15f, .26f), onSurfaceVariant = hsv(h, .08f, .80f),
            outline = hsv(h, .10f, .60f), outlineVariant = hsv(h, .12f, .30f),
            surfaceContainerLowest = hsv(h, .12f, .07f), surfaceContainerLow = hsv(h, .12f, .12f),
            surfaceContainer = hsv(h, .12f, .15f), surfaceContainerHigh = hsv(h, .12f, .19f),
            surfaceContainerHighest = hsv(h, .12f, .23f),
        )
    } else {
        lightColorScheme(
            primary = hsv(h, .70f, .55f), onPrimary = Color.White,
            primaryContainer = hsv(h, .20f, .97f), onPrimaryContainer = hsv(h, .80f, .25f),
            secondary = hsv(h + 20f, .35f, .50f), onSecondary = Color.White,
            secondaryContainer = hsv(h + 20f, .12f, .95f), onSecondaryContainer = hsv(h + 20f, .50f, .22f),
            tertiary = hsv(h + 60f, .50f, .55f), onTertiary = Color.White,
            tertiaryContainer = hsv(h + 60f, .15f, .96f), onTertiaryContainer = hsv(h + 60f, .70f, .25f),
            background = hsv(h, .03f, .99f), onBackground = hsv(h, .10f, .12f),
            surface = hsv(h, .03f, .99f), onSurface = hsv(h, .10f, .12f),
            surfaceVariant = hsv(h, .08f, .91f), onSurfaceVariant = hsv(h, .12f, .30f),
            outline = hsv(h, .10f, .50f), outlineVariant = hsv(h, .08f, .80f),
            surfaceContainerLowest = Color.White, surfaceContainerLow = hsv(h, .03f, .97f),
            surfaceContainer = hsv(h, .04f, .95f), surfaceContainerHigh = hsv(h, .05f, .93f),
            surfaceContainerHighest = hsv(h, .06f, .91f),
        )
    }
