package com.mycolourfulgesture.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    var dockVisible by remember { mutableStateOf(true) }
    val items = listOf(
        NavItem(R.string.nav_home, Icons.Rounded.Home),
        NavItem(R.string.nav_customization, Icons.Rounded.Palette),
        NavItem(R.string.nav_settings, Icons.Rounded.Settings),
    )
    val floating = settings.dockFloating.value
    val autoHide = settings.dockAutoHide.value
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(tab, floating) { dockVisible = true }

    // aşağı kaydırınca dock gizlenir, yukarı kaydırınca döner
    val connection = remember(autoHide) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (autoHide) {
                    if (available.y < -6f) dockVisible = false
                    else if (available.y > 6f) dockVisible = true
                }
                return Offset.Zero
            }
        }
    }

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
        val bottomPad = if (floating) 0.dp else inner.calculateBottomPadding()
        val tail = if (floating) navBottom + (settings.dockHeight.value + settings.dockBottom.value + 16).dp else 0.dp

        Box(
            Modifier
                .fillMaxSize()
                .padding(top = inner.calculateTopPadding(), bottom = bottomPad)
                .nestedScroll(connection)
        ) {
            when (tab) {
                0 -> HomeScreen(settings, tail)
                1 -> CustomizationScreen(pill, tail)
                else -> SettingsScreen(settings, pill, tail)
            }
            if (floating) {
                AnimatedVisibility(
                    visible = dockVisible || !autoHide,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = navBottom + settings.dockBottom.value.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FloatingDock(settings, items, tab) { tab = it }
                    }
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
