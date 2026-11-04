package com.mycolourfulgesture.ui

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycolourfulgesture.AppSettings
import com.mycolourfulgesture.Constants
import com.mycolourfulgesture.core.HookStatus
import com.mycolourfulgesture.core.isModuleActive
import com.mycolourfulgesture.core.moduleFramework
import com.mycolourfulgesture.R

@Composable
fun HomeScreen(settings: AppSettings, bottomPadding: Dp = 0.dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeroCard()
        StatusCard(settings)
        QuickCards()
        DeviceCard()
        WhatCard()
        AboutCard()
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun HeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(32.dp)),
    ) {
        Image(
            painter = painterResource(R.drawable.hero_sky),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000)),
                        startY = 120f,
                    )
                )
        )
        Text(
            text = stringResource(R.string.hero_title),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp,
                shadow = Shadow(Color(0x66000000), Offset(0f, 3f), 10f),
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
        )
    }
}

@Composable
private fun StatusCard(settings: AppSettings) {
    val context = LocalContext.current
    val tick = settings.resumeTick
    val status = remember(tick) { HookStatus.read(context) }
    val selfActive = remember(tick) { runCatching { isModuleActive() }.getOrDefault(false) }
    val selfFw = remember(tick) { runCatching { moduleFramework() }.getOrDefault("") }
    val sysUi = status.activeThisBoot
    val fw = status.framework.ifBlank { selfFw }.ifBlank { "?" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle(Icons.Rounded.Info, stringResource(R.string.status_title))
            InfoRow(
                stringResource(R.string.status_lsposed),
                when {
                    sysUi -> stringResource(R.string.status_lsposed_active, fw)
                    selfActive -> stringResource(R.string.status_lsposed_partial, fw)
                    else -> stringResource(R.string.status_lsposed_unknown)
                },
            )
            InfoRow(
                stringResource(R.string.status_pill),
                stringResource(if (sysUi) R.string.status_pill_active else R.string.status_pill_inactive),
            )
            val cls = status.hookedClass
            if (cls != null) {
                Text(
                    stringResource(R.string.status_hook_class, cls.substringAfterLast('.')),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            val err = status.error
            if (err != null) {
                Text(
                    stringResource(R.string.status_hook_error, err),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            val probe = status.probe
            if (probe != null) {
                Text(
                    stringResource(R.string.status_diag, probe),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            if (sysUi && status.draws == 0) {
                Text(
                    stringResource(R.string.status_no_draw),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                stringResource(
                    when {
                        sysUi -> R.string.status_note
                        selfActive -> R.string.status_partial_hint
                        else -> R.string.status_hint_none
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { restartSystemUi(context) }) {
                Text(stringResource(R.string.status_restart))
            }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { settings.bumpResume() }) {
                Text(stringResource(R.string.status_refresh))
            }
        }
    }
}

@Composable
private fun QuickCards() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickCard(Modifier.weight(1f), stringResource(R.string.quick_style), stringResource(R.string.quick_style_default))
        QuickCard(Modifier.weight(1f), stringResource(R.string.quick_monet), stringResource(R.string.quick_monet_off))
        QuickCard(Modifier.weight(1f), stringResource(R.string.quick_profile), stringResource(R.string.quick_profile_none))
    }
}

@Composable
private fun QuickCard(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DeviceCard() {
    val isPixel = Build.MANUFACTURER.equals("google", ignoreCase = true)
    val supported = Build.VERSION.SDK_INT in 31..37
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle(Icons.Rounded.PhoneAndroid, stringResource(R.string.device_title))
            InfoRow(
                stringResource(R.string.device_android),
                "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            )
            InfoRow(stringResource(R.string.device_model), "${Build.MANUFACTURER} ${Build.MODEL}")
            InfoRow(
                stringResource(R.string.device_ui),
                stringResource(if (isPixel) R.string.device_ui_pixel else R.string.device_ui_aosp),
            )
            InfoRow(
                stringResource(R.string.device_support),
                stringResource(if (supported) R.string.device_supported else R.string.device_unsupported),
            )
        }
    }
}

@Composable
private fun WhatCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(Icons.Rounded.AutoAwesome, stringResource(R.string.what_title))
            listOf(R.string.what_1, R.string.what_2, R.string.what_3, R.string.what_4).forEach {
                Text(
                    "•  " + stringResource(it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun AboutCard() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "?"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle(Icons.Rounded.Code, stringResource(R.string.about_title))
            Text(stringResource(R.string.about_version, version), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.about_credits),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { uriHandler.openUri(Constants.REPO_URL) }) {
                Text(stringResource(R.string.about_github))
            }
            UpdateRow()
        }
    }
}

@Composable
internal fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null)
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
