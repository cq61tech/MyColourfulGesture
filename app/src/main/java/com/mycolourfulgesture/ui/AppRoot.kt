package com.mycolourfulgesture.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.mycolourfulgesture.AppSettings
import com.mycolourfulgesture.R

private data class NavItem(val labelRes: Int, val icon: ImageVector)

@Composable
fun AppRoot(settings: AppSettings) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        NavItem(R.string.nav_home, Icons.Rounded.Home),
        NavItem(R.string.nav_customization, Icons.Rounded.Palette),
        NavItem(R.string.nav_settings, Icons.Rounded.Settings),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) },
                    )
                }
            }
        },
    ) { inner ->
        Box(Modifier.padding(inner)) {
            when (tab) {
                0 -> HomeScreen()
                1 -> CustomizationScreen()
                else -> SettingsScreen(settings)
            }
        }
    }
}
