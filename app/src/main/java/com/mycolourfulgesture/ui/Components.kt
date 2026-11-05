package com.mycolourfulgesture.ui

import android.content.Context
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
    SliderRow(
        label = label,
        valueText = display?.invoke(value) ?: "$value$suffix",
        value = value.toFloat(),
        range = range.first.toFloat()..range.last.toFloat(),
        onChange = { onChange(it.roundToInt()) },
        onFinished = onFinished,
    )
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
internal fun ColorPicker(color: Int, onChange: (Int) -> Unit, onCommit: () -> Unit) {
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
    var hue by remember { mutableFloatStateOf(hueOf(color)) }
    SliderRow(
        label = stringResource(R.string.cust_hue),
        valueText = "${hue.roundToInt()}°",
        value = hue,
        range = 0f..360f,
        onChange = {
            hue = it
            onChange(Color.hsv(it, 1f, 1f).toArgb())
        },
        onFinished = onCommit,
    )
}

private fun hueOf(argb: Int): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    return hsv[0]
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
