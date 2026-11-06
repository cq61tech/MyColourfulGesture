package com.mycolourfulgesture.ui

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mycolourfulgesture.R
import com.mycolourfulgesture.core.UpdateChecker
import kotlin.math.roundToInt

internal val SWATCHES = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFFFF5C8A, 0xFFB05CC4, 0xFF5B8CFF, 0xFF4CD39B, 0xFFFFB84D,
).map { it.toInt() }

@Composable
internal fun Block(content: @Composable ColumnScope.() -> Unit) {
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
internal fun BlockTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
internal fun SwitchRow(title: String, desc: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
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
internal fun SliderRow(
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
        Slider(value = value, onValueChange = onChange, valueRange = range, onValueChangeFinished = onFinished)
    }
}

@Composable
internal fun IntSlider(
    label: String,
    suffix: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    onFinished: () -> Unit = {},
    display: ((Int) -> String)? = null,
) {
    var editing by remember { mutableStateOf(false) }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                display?.invoke(value) ?: "$value$suffix",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = if (display == null) Modifier.clickable { editing = true } else Modifier,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            onValueChangeFinished = onFinished,
        )
    }
    if (editing) {
        var text by remember { mutableStateOf(value.toString()) }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text(label) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() || c == '-' }.take(5) },
                    singleLine = true,
                    suffix = { Text(suffix.trim()) },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    text.toIntOrNull()?.let {
                        onChange(it.coerceIn(range.first, range.last))
                        onFinished()
                    }
                    editing = false
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        )
    }
}

@Composable
internal fun Segments(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selected,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(label, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun ColorPicker(
    color: Int,
    onChange: (Int) -> Unit,
    onCommit: () -> Unit,
    compact: Boolean = false,
) {
    var hsv by remember { mutableStateOf(hsvOf(color)) }
    val current = android.graphics.Color.HSVToColor(hsv)
    LaunchedEffect(color) {
        if (color != current) hsv = hsvOf(color)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SWATCHES.forEach { c ->
            val selected = c == color
            Row(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(c))
                    .border(
                        if (selected) 3.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape,
                    )
                    .clickable {
                        onChange(c)
                        onCommit()
                    }
            ) {}
        }
    }

    if (!compact) {
        SliderRow(
            label = stringResource(R.string.cust_hue),
            valueText = "${hsv[0].roundToInt()}°",
            value = hsv[0],
            range = 0f..360f,
            onChange = {
                hsv = floatArrayOf(it, hsv[1], hsv[2])
                onChange(android.graphics.Color.HSVToColor(hsv))
            },
            onFinished = onCommit,
        )
        SliderRow(
            label = stringResource(R.string.cust_sat),
            valueText = "${(hsv[1] * 100).roundToInt()}%",
            value = hsv[1] * 100f,
            range = 0f..100f,
            onChange = {
                hsv = floatArrayOf(hsv[0], it / 100f, hsv[2])
                onChange(android.graphics.Color.HSVToColor(hsv))
            },
            onFinished = onCommit,
        )
        SliderRow(
            label = stringResource(R.string.cust_val),
            valueText = "${(hsv[2] * 100).roundToInt()}%",
            value = hsv[2] * 100f,
            range = 0f..100f,
            onChange = {
                hsv = floatArrayOf(hsv[0], hsv[1], it / 100f)
                onChange(android.graphics.Color.HSVToColor(hsv))
            },
            onFinished = onCommit,
        )
    }

    var hex by remember(color) { mutableStateOf("%06X".format(color and 0xFFFFFF)) }
    OutlinedTextField(
        value = hex,
        onValueChange = { v ->
            val f = v.filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }.take(6).uppercase()
            hex = f
            if (f.length == 6) {
                onChange(0xFF000000.toInt() or f.toInt(16))
                onCommit()
            }
        },
        singleLine = true,
        label = { Text("HEX") },
        prefix = { Text("#") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun ChipRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { i, label ->
            FilterChip(selected = i == selected, onClick = { onSelect(i) }, label = { Text(label) })
        }
    }
}

private fun hsvOf(argb: Int): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    return hsv
}

internal fun restartSystemUi(context: Context) = restartProcess(context, "com.android.systemui")

internal fun restartProcess(context: Context, pkg: String) {
    val app = context.applicationContext
    Thread {
        var ok = false
        try {
            val last = if (pkg == "com.android.systemui") "am crash $pkg" else "am force-stop $pkg"
            // bazı ROM'larda killall yok, sırayla dene
            val cmd = "killall $pkg || pkill -f $pkg || $last"
            ok = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)).waitFor() == 0
        } catch (_: Throwable) {
        }
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(app, app.getString(if (ok) R.string.restart_ok else R.string.restart_fail), Toast.LENGTH_LONG).show()
        }
    }.start()
}

internal fun defaultHomePackage(context: Context): String? = try {
    val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME)
    context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
} catch (_: Throwable) {
    null
}

@Composable
internal fun UpdateRow() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var result by remember { mutableStateOf<UpdateChecker.Result?>(null) }
    var busy by remember { mutableStateOf(false) }
    val current = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        }.getOrDefault(0)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            enabled = !busy,
            onClick = {
                busy = true
                UpdateChecker.check {
                    result = it
                    busy = false
                }
            },
        ) {
            Text(stringResource(if (busy) R.string.upd_checking else R.string.upd_check))
        }
        val r = result
        if (r != null) {
            if (!r.ok) {
                Text(
                    stringResource(R.string.upd_error, r.error ?: "?"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (UpdateChecker.CODE_OFFSET + r.latestBuild > current) {
                Text(stringResource(R.string.upd_available, r.tag), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { uriHandler.openUri(r.url) }) {
                    Text(stringResource(R.string.upd_open))
                }
            } else {
                Text(stringResource(R.string.upd_latest), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
