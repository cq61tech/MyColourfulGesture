package com.mycolourfulgesture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycolourfulgesture.AppSettings
import com.mycolourfulgesture.PillState
import com.mycolourfulgesture.R

private data class NavItem(val labelRes: Int, val icon: ImageVector)

@Composable
fun AppRoot(settings: AppSettings, pill: PillState) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        NavItem(R.string.nav_home, Icons.Rounded.Home),
        NavItem(R.string.nav_customization, Icons.Rounded.Palette),
        NavItem(R.string.nav_settings, Icons.Rounded.Settings),
    )
    val floating = settings.dockFloating.value

    Scaffold(
        bottomBar = {
            if (!floating) {
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
            }
        },
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            val reserve = if (floating) (settings.dockHeight.value + settings.dockBottom.value + 8).dp else 0.dp
            Box(Modifier.fillMaxSize().padding(bottom = reserve)) {
                when (tab) {
                    0 -> HomeScreen(settings)
                    1 -> CustomizationScreen(pill)
                    else -> SettingsScreen(settings, pill)
                }
            }
            if (floating) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = settings.dockBottom.value.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    FloatingDock(settings, items, tab) { tab = it }
                }
            }
        }
    }
}

@Composable
private fun FloatingDock(settings: AppSettings, items: List<NavItem>, tab: Int, onSelect: (Int) -> Unit) {
    val h = settings.dockHeight.value
    Surface(
        modifier = Modifier
            .fillMaxWidth(settings.dockWidth.value / 100f)
            .height(h.dp),
        shape = RoundedCornerShape((h / 2f).dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = settings.dockOpacity.value / 100f),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                        .clickable { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (settings.dockLabels.value) {
                        Text(
                            stringResource(item.labelRes),
                            fontSize = settings.dockText.value.sp,
                            maxLines = 1,
                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
