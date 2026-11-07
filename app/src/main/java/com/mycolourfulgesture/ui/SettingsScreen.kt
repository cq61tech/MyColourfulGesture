package com.mycolourfulgesture.ui

import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.mycolourfulgesture.AppSettings
import com.mycolourfulgesture.Constants
import com.mycolourfulgesture.PillState
import com.mycolourfulgesture.R
import com.mycolourfulgesture.core.HookStatus

private val LANGUAGES = listOf(
    "en" to "English", "tr" to "Türkçe", "ru" to "Русский",
    "es" to "Español", "de" to "Deutsch", "fr" to "Français",
)
private const val LAUNCHER_ALIAS = "com.mycolourfulgesture.LauncherAlias"

private fun currentLanguage(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty()) "system" else (locales.get(0)?.language ?: "en")
}

@Composable
fun SettingsScreen(settings: AppSettings, pill: PillState, bottomPadding: Dp = 0.dp) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var lang by remember { mutableStateOf(currentLanguage()) }
    var showResetDialog by remember { mutableStateOf(false) }
    val cfg = pill.config

    fun toast(res: Int) = Toast.makeText(context, context.getString(res), Toast.LENGTH_SHORT).show()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(pill.exportJson().toByteArray()) }
            }.isSuccess
            toast(if (ok) R.string.set_export_ok else R.string.set_export_fail)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (text != null && pill.importJson(text)) {
                toast(R.string.set_import_ok)
            } else {
                toast(R.string.set_import_fail)
            }
        }
    }

    val alias = remember { ComponentName(context.packageName, LAUNCHER_ALIAS) }
    var iconHidden by remember {
        mutableStateOf(
            context.packageManager.getComponentEnabledSetting(alias) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        )
    }

    val status = remember(settings.resumeTick) { HookStatus.read(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Block {
            BlockTitle(stringResource(R.string.settings_language))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = lang == "system",
                    onClick = {
                        lang = "system"
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    },
                    label = { Text(stringResource(R.string.settings_lang_system)) },
                )
                LANGUAGES.forEach { (tag, name) ->
                    FilterChip(
                        selected = lang == tag,
                        onClick = {
                            lang = tag
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        },
                        label = { Text(name) },
                    )
                }
            }
        }

        Block {
            BlockTitle(stringResource(R.string.set_appearance))
            Segments(
                options = listOf(
                    stringResource(R.string.settings_theme_system),
                    stringResource(R.string.settings_theme_light),
                    stringResource(R.string.settings_theme_dark),
                ),
                selected = settings.themeMode,
            ) { settings.updateThemeMode(it) }
            SwitchRow(
                title = stringResource(R.string.set_dynamic),
                desc = stringResource(R.string.set_dynamic_desc),
                checked = settings.dynamicColor.value,
            ) { settings.dynamicColor.set(it) }
            if (!settings.dynamicColor.value) {
                IntSlider(
                    label = stringResource(R.string.set_seed),
                    suffix = "°",
                    value = settings.seedHue.value,
                    range = 0..360,
                    onChange = { settings.seedHue.set(it) },
                )
            }
            SwitchRow(
                title = stringResource(R.string.set_amoled),
                desc = null,
                checked = settings.amoled.value,
            ) { settings.amoled.set(it) }
        }

        Block {
            BlockTitle(stringResource(R.string.set_dock))
            SwitchRow(
                title = stringResource(R.string.set_dock_floating),
                desc = stringResource(R.string.set_dock_floating_desc),
                checked = settings.dockFloating.value,
            ) { settings.dockFloating.set(it) }
            if (settings.dockFloating.value) {
                IntSlider(stringResource(R.string.set_dock_width), "%", settings.dockWidth.value, 50..100, { settings.dockWidth.set(it) })
                IntSlider(stringResource(R.string.set_dock_height), " dp", settings.dockHeight.value, 52..104, { settings.dockHeight.set(it) })
                IntSlider(stringResource(R.string.cust_opacity), "%", settings.dockOpacity.value, 30..100, { settings.dockOpacity.set(it) })
                IntSlider(stringResource(R.string.set_dock_text), " sp", settings.dockText.value, 9..16, { settings.dockText.set(it) })
                IntSlider(stringResource(R.string.set_dock_bottom), " dp", settings.dockBottom.value, 0..48, { settings.dockBottom.set(it) })
                SwitchRow(
                    title = stringResource(R.string.set_dock_autohide),
                    desc = stringResource(R.string.set_dock_autohide_desc),
                    checked = settings.dockAutoHide.value,
                ) { settings.dockAutoHide.set(it) }
                SwitchRow(
                    title = stringResource(R.string.set_dock_labels),
                    desc = null,
                    checked = settings.dockLabels.value,
                ) { settings.dockLabels.set(it) }
            }
        }

        Block {
            BlockTitle(stringResource(R.string.set_module))
            SwitchRow(
                title = stringResource(R.string.set_hook_enabled),
                desc = stringResource(R.string.set_hook_enabled_desc),
                checked = cfg.hookEnabled,
            ) {
                pill.update(pill.config.copy(hookEnabled = it))
                pill.commit()
            }
            SwitchRow(
                title = stringResource(R.string.set_debug),
                desc = stringResource(R.string.set_debug_desc),
                checked = cfg.debug,
            ) {
                pill.update(pill.config.copy(debug = it))
                pill.commit()
            }
            SwitchRow(
                title = stringResource(R.string.set_hide_icon),
                desc = stringResource(R.string.set_hide_icon_desc),
                checked = iconHidden,
            ) { hide ->
                iconHidden = hide
                context.packageManager.setComponentEnabledSetting(
                    alias,
                    if (hide) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { restartSystemUi(context) }) {
                Text(stringResource(R.string.status_restart))
            }
        }

        Block {
            BlockTitle(stringResource(R.string.set_diag))
            val none = stringResource(R.string.set_diag_none)
            DiagRow(stringResource(R.string.device_android), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            DiagRow(stringResource(R.string.device_model), "${Build.MANUFACTURER} ${Build.MODEL}")
            DiagRow(stringResource(R.string.set_diag_framework), status.framework.ifBlank { none })
            DiagRow(stringResource(R.string.set_diag_class), status.hookedClass?.substringAfterLast('.') ?: none)
            status.probe?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            status.error?.let {
                Text(stringResource(R.string.status_hook_error, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }

        Block {
            BlockTitle(stringResource(R.string.set_backup))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { exportLauncher.launch("MyColourfulGesture-backup.json") }) {
                    Text(stringResource(R.string.set_export))
                }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                    Text(stringResource(R.string.set_import))
                }
            }
        }

        Block {
            BlockTitle(stringResource(R.string.set_reset))
            OutlinedButton(onClick = { pill.resetLook() }) {
                Text(stringResource(R.string.set_reset_pill))
            }
            OutlinedButton(onClick = { showResetDialog = true }) {
                Text(stringResource(R.string.set_reset_all))
            }
        }

        Block {
            BlockTitle(stringResource(R.string.set_updates))
            UpdateRow()
        }

        Block {
            BlockTitle(stringResource(R.string.set_about))
            Text(stringResource(R.string.settings_license), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { uriHandler.openUri(Constants.REPO_URL) }) {
                    Text(stringResource(R.string.about_github))
                }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { uriHandler.openUri(Constants.ISSUES_URL) }) {
                    Text(stringResource(R.string.set_issues))
                }
            }
        }
        Spacer(Modifier.height(bottomPadding))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.set_reset_confirm_title)) },
            text = { Text(stringResource(R.string.set_reset_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    settings.resetAppSettings()
                    pill.resetAll()
                    showResetDialog = false
                }) { Text(stringResource(R.string.dialog_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        )
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
