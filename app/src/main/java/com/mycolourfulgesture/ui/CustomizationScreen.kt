package com.mycolourfulgesture.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mycolourfulgesture.PillState
import com.mycolourfulgesture.R
import com.mycolourfulgesture.core.PillConfig
import com.mycolourfulgesture.core.PillGeometry
import com.mycolourfulgesture.hook.PillRenderer

private data class Preset(val nameRes: Int, val cfg: PillConfig)

private val PRESETS = listOf(
    Preset(R.string.preset_pride, PillConfig(
        gradient = 2, widthPct = 100, heightPct = 200, animMode = PillConfig.ANIM_FLOW, animSpeed = 70)),
    Preset(R.string.preset_spectrum, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, animMode = PillConfig.ANIM_HUE, animSpeed = 90,
        glowPct = 55, heightPct = 160)),
    Preset(R.string.preset_aurora, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFF00E5A8.toInt(), gradient = 1, color2 = 0xFF7C4DFF.toInt(),
        widthPct = 85, heightPct = 150, animMode = PillConfig.ANIM_FLOW, animSpeed = 80)),
    Preset(R.string.preset_lava, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFFFF5722.toInt(), gradient = 1, color2 = 0xFFFFC107.toInt(),
        heightPct = 160, animMode = PillConfig.ANIM_FLOW, animSpeed = 120)),
    Preset(R.string.preset_candy, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFFFF80AB.toInt(), gradient = 1, color2 = 0xFF82B1FF.toInt(),
        widthPct = 70, heightPct = 130)),
    Preset(R.string.preset_ocean, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFF00B0FF.toInt(), gradient = 1, color2 = 0xFF00E5FF.toInt(),
        widthPct = 90, heightPct = 140, animMode = PillConfig.ANIM_BREATHE, animSpeed = 70)),
    Preset(R.string.preset_glass, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = -1, alphaPct = 45, widthPct = 80, heightPct = 150,
        strokeDp10 = 8, strokeColor = -1)),
    Preset(R.string.preset_neon, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFF00E5FF.toInt(), heightPct = 150, glowPct = 60,
        animMode = PillConfig.ANIM_BREATHE)),
    Preset(R.string.preset_sunset, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFFFF7A59.toInt(), gradient = 1,
        color2 = 0xFFB05CC4.toInt(), widthPct = 70, heightPct = 140)),
    Preset(R.string.preset_soft, PillConfig(
        colorMode = PillConfig.COLOR_MONET, monetSource = 1, monetShade = 4, alphaPct = 90, heightPct = 120)),
    Preset(R.string.preset_minimal, PillConfig(alphaPct = 70, widthPct = 40, heightPct = 60)),
    Preset(R.string.preset_chunky, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = -1, widthPct = 60, heightPct = 280, cornerPct = 40,
        strokeDp10 = 15, strokeColor = 0xFF000000.toInt())),
    Preset(R.string.preset_dots, PillConfig(
        colorMode = PillConfig.COLOR_MONET, monetSource = 1, monetShade = 5,
        shape = PillConfig.SHAPE_DOTS, heightPct = 130)),
)

@Composable
fun CustomizationScreen(state: PillState, bottomPadding: Dp = 0.dp) {
    val cfg = state.config
    val commit = { state.commit() }
    var showSave by remember { mutableStateOf(false) }

    fun edit(block: (PillConfig) -> PillConfig) = state.update(block(state.config))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.cust_preview),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp),
        )
        PillPreview(cfg)
        Text(
            stringResource(R.string.cust_preview_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Text(
            stringResource(R.string.cust_tip),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Block {
            SwitchRow(
                title = stringResource(R.string.cust_enable),
                desc = stringResource(R.string.cust_enable_desc),
                checked = cfg.enabled,
            ) {
                edit { c -> c.copy(enabled = it) }
                commit()
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_presets))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PRESETS.forEach { p ->
                    AssistChip(
                        onClick = {
                            val cur = state.config
                            state.update(p.cfg.copy(hookEnabled = cur.hookEnabled, debug = cur.debug, enabled = true))
                            commit()
                        },
                        label = { Text(stringResource(p.nameRes)) },
                    )
                }
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_my_styles))
            if (state.profiles.isEmpty()) {
                Text(
                    stringResource(R.string.cust_no_styles),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.profiles.forEachIndexed { i, p ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { state.applyProfile(i) }) { Text(stringResource(R.string.cust_apply)) }
                    TextButton(onClick = { state.deleteProfile(i) }) { Text(stringResource(R.string.cust_delete)) }
                }
            }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showSave = true }) {
                Text(stringResource(R.string.cust_save_style))
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_color_section))
            Segments(
                options = listOf(
                    stringResource(R.string.color_stock),
                    stringResource(R.string.color_custom),
                    stringResource(R.string.color_monet),
                ),
                selected = cfg.colorMode,
            ) {
                edit { c -> c.copy(colorMode = it) }
                commit()
            }

            if (cfg.colorMode == PillConfig.COLOR_CUSTOM) {
                ColorPicker(
                    color = cfg.color,
                    onChange = { v -> edit { c -> c.copy(color = v) } },
                    onCommit = commit,
                )
            }

            if (cfg.colorMode == PillConfig.COLOR_MONET) {
                ChipRow(
                    options = listOf(
                        stringResource(R.string.monet_accent1), stringResource(R.string.monet_accent2),
                        stringResource(R.string.monet_accent3), stringResource(R.string.monet_neutral1),
                        stringResource(R.string.monet_neutral2),
                    ),
                    selected = cfg.monetSource - 1,
                ) {
                    edit { c -> c.copy(monetSource = it + 1) }
                    commit()
                }
                IntSlider(
                    label = stringResource(R.string.cust_monet_shade),
                    suffix = "",
                    value = cfg.monetShade,
                    range = 0..(PillConfig.MONET_SHADES.size - 1),
                    onChange = { v -> edit { c -> c.copy(monetShade = v) } },
                    onFinished = commit,
                    display = { PillConfig.MONET_SHADES[it].toString() },
                )
            }

            IntSlider(
                label = stringResource(R.string.cust_opacity),
                suffix = "%",
                value = cfg.alphaPct,
                range = 0..100,
                onChange = { v -> edit { c -> c.copy(alphaPct = v) } },
                onFinished = commit,
            )

            Text(stringResource(R.string.cust_gradient), style = MaterialTheme.typography.bodyMedium)
            Segments(
                options = listOf(
                    stringResource(R.string.gradient_off),
                    stringResource(R.string.gradient_custom),
                    stringResource(R.string.gradient_rainbow),
                ),
                selected = cfg.gradient,
            ) {
                edit { c -> c.copy(gradient = it) }
                commit()
            }
            if (cfg.gradient == 1) {
                IntSlider(
                    label = stringResource(R.string.cust_stops),
                    suffix = "",
                    value = cfg.gradStops,
                    range = 2..5,
                    onChange = { v -> edit { c -> c.copy(gradStops = v) } },
                    onFinished = commit,
                )
                for (i in 2..cfg.gradStops) {
                    Text(stringResource(R.string.cust_stop_n, i), style = MaterialTheme.typography.bodyMedium)
                    ColorPicker(
                        color = when (i) {
                            2 -> cfg.color2
                            3 -> cfg.color3
                            4 -> cfg.color4
                            else -> cfg.color5
                        },
                        onChange = { v ->
                            edit { c ->
                                when (i) {
                                    2 -> c.copy(color2 = v)
                                    3 -> c.copy(color3 = v)
                                    4 -> c.copy(color4 = v)
                                    else -> c.copy(color5 = v)
                                }
                            }
                        },
                        onCommit = commit,
                        compact = true,
                    )
                }
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_anim_section))
            ChipRow(
                options = listOf(
                    stringResource(R.string.anim_none), stringResource(R.string.anim_breathe),
                    stringResource(R.string.anim_flow), stringResource(R.string.anim_hue),
                    stringResource(R.string.anim_pulse),
                ),
                selected = cfg.animMode,
            ) {
                edit { c -> c.copy(animMode = it) }
                commit()
            }
            if (cfg.animMode != PillConfig.ANIM_NONE) {
                IntSlider(
                    label = stringResource(R.string.cust_anim_speed),
                    suffix = "%",
                    value = cfg.animSpeed,
                    range = 30..300,
                    onChange = { v -> edit { c -> c.copy(animSpeed = v) } },
                    onFinished = commit,
                )
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_size_section))
            ChipRow(
                options = listOf(
                    stringResource(R.string.shape_bar), stringResource(R.string.shape_dot),
                    stringResource(R.string.shape_dots), stringResource(R.string.shape_dashes),
                ),
                selected = cfg.shape,
            ) {
                edit { c -> c.copy(shape = it) }
                commit()
            }
            val wide = cfg.shape == PillConfig.SHAPE_BAR || cfg.shape == PillConfig.SHAPE_DASHES
            val multi = cfg.shape == PillConfig.SHAPE_DOTS || cfg.shape == PillConfig.SHAPE_DASHES
            if (wide) {
                IntSlider(
                    label = stringResource(R.string.cust_width),
                    suffix = "%",
                    value = cfg.widthPct,
                    range = 10..400,
                    onChange = { v -> edit { c -> c.copy(widthPct = v) } },
                    onFinished = commit,
                )
            }
            if (multi) {
                IntSlider(
                    label = stringResource(R.string.cust_seg_count),
                    suffix = "",
                    value = cfg.segCount,
                    range = 1..9,
                    onChange = { v -> edit { c -> c.copy(segCount = v) } },
                    onFinished = commit,
                )
                IntSlider(
                    label = stringResource(R.string.cust_seg_gap),
                    suffix = "%",
                    value = cfg.gapPct,
                    range = 0..300,
                    onChange = { v -> edit { c -> c.copy(gapPct = v) } },
                    onFinished = commit,
                )
            }
            IntSlider(
                label = stringResource(R.string.cust_height),
                suffix = "%",
                value = cfg.heightPct,
                range = 30..600,
                onChange = { v -> edit { c -> c.copy(heightPct = v) } },
                onFinished = commit,
            )
            if (wide) {
                IntSlider(
                    label = stringResource(R.string.cust_corner),
                    suffix = "%",
                    value = cfg.cornerPct,
                    range = 0..100,
                    onChange = { v -> edit { c -> c.copy(cornerPct = v) } },
                    onFinished = commit,
                )
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_position_section))
            IntSlider(
                label = stringResource(R.string.cust_offset),
                suffix = " dp",
                value = cfg.offsetDp,
                range = -16..40,
                onChange = { v -> edit { c -> c.copy(offsetDp = v) } },
                onFinished = commit,
            )
            IntSlider(
                label = stringResource(R.string.cust_halign),
                suffix = "",
                value = cfg.hAlign,
                range = -100..100,
                onChange = { v -> edit { c -> c.copy(hAlign = v) } },
                onFinished = commit,
            )
        }

        Block {
            BlockTitle(stringResource(R.string.cust_stroke_section))
            IntSlider(
                label = stringResource(R.string.cust_stroke_width),
                suffix = "",
                value = cfg.strokeDp10,
                range = 0..40,
                onChange = { v -> edit { c -> c.copy(strokeDp10 = v) } },
                onFinished = commit,
                display = { "%.1f dp".format(it / 10f) },
            )
            if (cfg.strokeDp10 > 0) {
                Text(stringResource(R.string.cust_stroke_color), style = MaterialTheme.typography.bodyMedium)
                ColorPicker(
                    color = cfg.strokeColor,
                    onChange = { v -> edit { c -> c.copy(strokeColor = v) } },
                    onCommit = commit,
                    compact = true,
                )
            }
            IntSlider(
                label = stringResource(R.string.cust_glow),
                suffix = "%",
                value = cfg.glowPct,
                range = 0..100,
                onChange = { v -> edit { c -> c.copy(glowPct = v) } },
                onFinished = commit,
            )
        }

        OutlinedButton(onClick = { state.resetLook() }) {
            Text(stringResource(R.string.cust_reset))
        }
        Spacer(Modifier.height(bottomPadding))
    }

    if (showSave) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSave = false },
            title = { Text(stringResource(R.string.cust_save_style)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.cust_style_name)) },
                )
            },
            confirmButton = {
                TextButton(enabled = name.isNotBlank(), onClick = {
                    state.addProfile(name.trim())
                    showSave = false
                }) { Text(stringResource(R.string.dialog_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showSave = false }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        )
    }
}

// önizleme gerçek çizim motorunu kullanır, bu yüzden hook'taki görünümle aynıdır
@Composable
private fun PillPreview(cfg: PillConfig) {
    val context = LocalContext.current
    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
    var tick by remember { mutableLongStateOf(0L) }
    val animating = cfg.animMode != PillConfig.ANIM_NONE
    LaunchedEffect(animating) {
        while (animating) withFrameNanos { tick = it }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (tick < 0L) return@Canvas
            drawIntoCanvas { canvas ->
                val d = density
                val pillH = (4f * d * cfg.heightPct / 100f).coerceIn(1f, size.height / 2f)
                val totalW = PillGeometry.totalWidth(cfg, 108f * d, pillH, size.width)
                val free = size.width - totalW
                val left = (free / 2f * (1f + cfg.hAlign / 100f)).coerceIn(0f, free)
                val bottom = size.height - 20f * d - cfg.offsetDp * d
                val top = bottom - pillH
                val corner = pillH / 2f * cfg.cornerPct / 100f
                PillRenderer.render(canvas.nativeCanvas, context, cfg, left, top, totalW, pillH, corner, d, onSurface)
            }
        }
    }
}
