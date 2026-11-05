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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mycolourfulgesture.PillState
import com.mycolourfulgesture.R
import com.mycolourfulgesture.core.MonetColors
import com.mycolourfulgesture.core.PillConfig

private data class Preset(val nameRes: Int, val cfg: PillConfig)

private val PRESETS = listOf(
    Preset(R.string.preset_pride, PillConfig(
        gradient = 2, widthPct = 100, heightPct = 200, animMode = PillConfig.ANIM_FLOW, animSpeed = 70)),
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
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFF00E5FF.toInt(), heightPct = 150, animMode = PillConfig.ANIM_BREATHE)),
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
        colorMode = PillConfig.COLOR_MONET, monetSource = 1, monetShade = 5, shape = PillConfig.SHAPE_DOTS, heightPct = 130)),
)

@Composable
fun CustomizationScreen(state: PillState, bottomPadding: Dp = 0.dp) {
    val cfg = state.config
    val commit = { state.commit() }

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

        Block {
            SwitchRow(
                title = stringResource(R.string.cust_enable),
                desc = stringResource(R.string.cust_enable_desc),
                checked = cfg.enabled,
            ) {
                state.update(state.config.copy(enabled = it))
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
            BlockTitle(stringResource(R.string.cust_color_section))
            Segments(
                options = listOf(
                    stringResource(R.string.color_stock),
                    stringResource(R.string.color_custom),
                    stringResource(R.string.color_monet),
                ),
                selected = cfg.colorMode,
            ) {
                state.update(state.config.copy(colorMode = it))
                commit()
            }

            if (cfg.colorMode == PillConfig.COLOR_CUSTOM) {
                ColorPicker(
                    color = cfg.color,
                    onChange = { state.update(state.config.copy(color = it)) },
                    onCommit = commit,
                )
            }

            if (cfg.colorMode == PillConfig.COLOR_MONET) {
                val names = listOf(
                    R.string.monet_accent1, R.string.monet_accent2, R.string.monet_accent3,
                    R.string.monet_neutral1, R.string.monet_neutral2,
                )
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    names.forEachIndexed { i, res ->
                        FilterChip(
                            selected = cfg.monetSource == i + 1,
                            onClick = {
                                state.update(state.config.copy(monetSource = i + 1))
                                commit()
                            },
                            label = { Text(stringResource(res)) },
                        )
                    }
                }
                IntSlider(
                    label = stringResource(R.string.cust_monet_shade),
                    suffix = "",
                    value = cfg.monetShade,
                    range = 0..(PillConfig.MONET_SHADES.size - 1),
                    onChange = { state.update(state.config.copy(monetShade = it)) },
                    onFinished = commit,
                    display = { PillConfig.MONET_SHADES[it].toString() },
                )
            }

            IntSlider(
                label = stringResource(R.string.cust_opacity),
                suffix = "%",
                value = cfg.alphaPct,
                range = 0..100,
                onChange = { state.update(state.config.copy(alphaPct = it)) },
                onFinished = commit,
            )

            Text(stringResource(R.string.cust_gradient), style = MaterialTheme.typography.bodyMedium)
            Segments(
                options = listOf(
                    stringResource(R.string.gradient_off),
                    stringResource(R.string.gradient_two),
                    stringResource(R.string.gradient_rainbow),
                ),
                selected = cfg.gradient,
            ) {
                state.update(state.config.copy(gradient = it))
                commit()
            }
            if (cfg.gradient == 1) {
                Text(stringResource(R.string.cust_gradient_color), style = MaterialTheme.typography.bodyMedium)
                ColorPicker(
                    color = cfg.color2,
                    onChange = { state.update(state.config.copy(color2 = it)) },
                    onCommit = commit,
                )
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_anim_section))
            Segments(
                options = listOf(
                    stringResource(R.string.anim_none),
                    stringResource(R.string.anim_breathe),
                    stringResource(R.string.anim_flow),
                ),
                selected = cfg.animMode,
            ) {
                state.update(state.config.copy(animMode = it))
                commit()
            }
            if (cfg.animMode != PillConfig.ANIM_NONE) {
                IntSlider(
                    label = stringResource(R.string.cust_anim_speed),
                    suffix = "%",
                    value = cfg.animSpeed,
                    range = 30..300,
                    onChange = { state.update(state.config.copy(animSpeed = it)) },
                    onFinished = commit,
                )
            }
        }

        Block {
            BlockTitle(stringResource(R.string.cust_size_section))
            Segments(
                options = listOf(
                    stringResource(R.string.shape_bar),
                    stringResource(R.string.shape_dot),
                    stringResource(R.string.shape_dots),
                ),
                selected = cfg.shape,
            ) {
                state.update(state.config.copy(shape = it))
                commit()
            }
            if (cfg.shape == PillConfig.SHAPE_BAR) {
                IntSlider(
                    label = stringResource(R.string.cust_width),
                    suffix = "%",
                    value = cfg.widthPct,
                    range = 10..100,
                    onChange = { state.update(state.config.copy(widthPct = it)) },
                    onFinished = commit,
                )
            }
            IntSlider(
                label = stringResource(R.string.cust_height),
                suffix = "%",
                value = cfg.heightPct,
                range = 30..600,
                onChange = { state.update(state.config.copy(heightPct = it)) },
                onFinished = commit,
            )
            if (cfg.shape == PillConfig.SHAPE_BAR) {
                IntSlider(
                    label = stringResource(R.string.cust_corner),
                    suffix = "%",
                    value = cfg.cornerPct,
                    range = 0..100,
                    onChange = { state.update(state.config.copy(cornerPct = it)) },
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
                range = -8..30,
                onChange = { state.update(state.config.copy(offsetDp = it)) },
                onFinished = commit,
            )
            IntSlider(
                label = stringResource(R.string.cust_halign),
                suffix = "",
                value = cfg.hAlign,
                range = -100..100,
                onChange = { state.update(state.config.copy(hAlign = it)) },
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
                onChange = { state.update(state.config.copy(strokeDp10 = it)) },
                onFinished = commit,
                display = { "%.1f dp".format(it / 10f) },
            )
            if (cfg.strokeDp10 > 0) {
                Text(stringResource(R.string.cust_stroke_color), style = MaterialTheme.typography.bodyMedium)
                ColorPicker(
                    color = cfg.strokeColor,
                    onChange = { state.update(state.config.copy(strokeColor = it)) },
                    onCommit = commit,
                )
            }
        }

        OutlinedButton(onClick = { state.resetLook() }) {
            Text(stringResource(R.string.cust_reset))
        }
        Spacer(Modifier.height(bottomPadding))
    }
}

@Composable
private fun PillPreview(cfg: PillConfig) {
    val context = LocalContext.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    val c1 = when (cfg.colorMode) {
        PillConfig.COLOR_CUSTOM -> Color(cfg.color)
        PillConfig.COLOR_MONET -> Color(MonetColors.resolve(context, cfg.monetSource, cfg.monetShade))
        else -> onSurface
    }
    val c2 = Color(cfg.color2)
    val sc = Color(cfg.strokeColor)
    val a = cfg.alphaPct / 100f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            val pillH = (4f * d * cfg.heightPct / 100f).coerceIn(1f, size.height / 2f)
            val gap = pillH * 0.9f
            val totalW = when (cfg.shape) {
                PillConfig.SHAPE_DOT -> pillH
                PillConfig.SHAPE_DOTS -> pillH * 3f + gap * 2f
                else -> 108f * d * cfg.widthPct / 100f
            }.coerceIn(1f, size.width)
            val free = size.width - totalW
            val left = (free / 2f * (1f + cfg.hAlign / 100f)).coerceIn(0f, free)
            val bottom = size.height - 20f * d - cfg.offsetDp * d
            val top = bottom - pillH
            val corner = pillH / 2f * cfg.cornerPct / 100f

            val brush: Brush = if (cfg.gradient == 2) {
                Brush.horizontalGradient(PillConfig.RAINBOW.map { Color(it) }, startX = left, endX = left + totalW)
            } else if (cfg.gradient == 1) {
                Brush.horizontalGradient(listOf(c1, c2), startX = left, endX = left + totalW)
            } else {
                SolidColor(c1)
            }

            fun shapes(inset: Float, draw: (Float, Float, Float, Float, Float) -> Unit) {
                val i = inset / 2f
                if (cfg.shape == PillConfig.SHAPE_DOTS) {
                    for (k in 0..2) {
                        val x = left + k * (pillH + gap)
                        draw(x + i, top + i, pillH - inset, pillH - inset, (pillH - inset) / 2f)
                    }
                } else {
                    val r = if (cfg.shape == PillConfig.SHAPE_DOT) pillH / 2f else corner
                    draw(left + i, top + i, totalW - inset, pillH - inset, maxOf(r - i, 0f))
                }
            }

            shapes(0f) { x, y, w, h, r ->
                if (w > 0f && h > 0f) {
                    drawRoundRect(brush = brush, topLeft = Offset(x, y), size = Size(w, h), cornerRadius = CornerRadius(r, r), alpha = a)
                }
            }
            if (cfg.strokeDp10 > 0) {
                val sw = cfg.strokeDp10 / 10f * d
                shapes(sw) { x, y, w, h, r ->
                    if (w > 0f && h > 0f) {
                        drawRoundRect(color = sc, topLeft = Offset(x, y), size = Size(w, h), cornerRadius = CornerRadius(r, r), alpha = a, style = Stroke(width = sw))
                    }
                }
            }
        }
    }
}
