package com.mycolourfulgesture.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mycolourfulgesture.PillState
import com.mycolourfulgesture.R
import com.mycolourfulgesture.core.PillConfig
import kotlin.math.roundToInt

private val SWATCHES = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFFFF5C8A, 0xFFB05CC4, 0xFF5B8CFF, 0xFF4CD39B, 0xFFFFB84D,
).map { it.toInt() }

@Composable
fun CustomizationScreen(state: PillState) {
    val cfg = state.config
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
                state.commit()
            }
        }

        Block {
            Text(stringResource(R.string.cust_color_section), style = MaterialTheme.typography.titleMedium)
            SwitchRow(
                title = stringResource(R.string.cust_use_custom),
                desc = null,
                checked = cfg.useCustomColor,
            ) {
                state.update(state.config.copy(useCustomColor = it))
                state.commit()
            }
            if (cfg.useCustomColor) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SWATCHES.forEach { c ->
                        val selected = c == cfg.color
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    if (selected) 3.dp else 1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape,
                                )
                                .clickable {
                                    state.update(state.config.copy(color = c))
                                    state.commit()
                                }
                        )
                    }
                }
                var hue by remember { mutableFloatStateOf(hueOf(cfg.color)) }
                SliderRow(
                    label = stringResource(R.string.cust_hue),
                    valueText = "${hue.roundToInt()}°",
                    value = hue,
                    range = 0f..360f,
                    onChange = {
                        hue = it
                        state.update(state.config.copy(color = Color.hsv(it, 1f, 1f).toArgb()))
                    },
                    onFinished = { state.commit() },
                )
            }
            SliderRow(
                label = stringResource(R.string.cust_opacity),
                valueText = "${cfg.alphaPct}%",
                value = cfg.alphaPct.toFloat(),
                range = 10f..100f,
                onChange = { state.update(state.config.copy(alphaPct = it.roundToInt())) },
                onFinished = { state.commit() },
            )
        }

        Block {
            Text(stringResource(R.string.cust_size_section), style = MaterialTheme.typography.titleMedium)
            SliderRow(
                label = stringResource(R.string.cust_width),
                valueText = "${cfg.widthPct}%",
                value = cfg.widthPct.toFloat(),
                range = 30f..100f,
                onChange = { state.update(state.config.copy(widthPct = it.roundToInt())) },
                onFinished = { state.commit() },
            )
            SliderRow(
                label = stringResource(R.string.cust_height),
                valueText = "${cfg.heightPct}%",
                value = cfg.heightPct.toFloat(),
                range = 50f..400f,
                onChange = { state.update(state.config.copy(heightPct = it.roundToInt())) },
                onFinished = { state.commit() },
            )
            SliderRow(
                label = stringResource(R.string.cust_corner),
                valueText = "${cfg.cornerPct}%",
                value = cfg.cornerPct.toFloat(),
                range = 0f..100f,
                onChange = { state.update(state.config.copy(cornerPct = it.roundToInt())) },
                onFinished = { state.commit() },
            )
        }

        OutlinedButton(onClick = { state.resetToDefaults() }) {
            Text(stringResource(R.string.cust_reset))
        }
    }
}

@Composable
private fun Block(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun SwitchRow(title: String, desc: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (desc != null) {
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    onFinished: () -> Unit,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(valueText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            onValueChangeFinished = onFinished,
        )
    }
}

@Composable
private fun PillPreview(cfg: PillConfig) {
    val base = if (cfg.useCustomColor) Color(cfg.color) else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = density
            val w = (108f * d * cfg.widthPct / 100f).coerceAtMost(size.width)
            val h = (4f * d * cfg.heightPct / 100f).coerceAtMost(size.height / 2f)
            val x = (size.width - w) / 2f
            val y = size.height - 20f * d - h
            val r = h / 2f * cfg.cornerPct / 100f
            drawRoundRect(
                color = base.copy(alpha = cfg.alphaPct / 100f),
                topLeft = Offset(x, y),
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r),
            )
        }
    }
}

private fun hueOf(argb: Int): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    return hsv[0]
}
