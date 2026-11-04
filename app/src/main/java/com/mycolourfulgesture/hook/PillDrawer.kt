package com.mycolourfulgesture.hook

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.mycolourfulgesture.core.MonetColors
import com.mycolourfulgesture.core.PillConfig
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Field
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/**
 * NavigationHandle.onDraw yerine geçer. Hata olursa veya ayar "stok" ise orijinal çizime düşer.
 * Stok çizim: pill = view genişliğinde, yüksekliği 2*mRadius, alt kenarı (yükseklik - mBottom).
 */
object PillDrawer {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val matrix = Matrix()
    private val fieldCache = HashMap<String, Field?>()
    private var probed = false
    private var monetKey = -1
    private var monetAt = 0L
    private var monetColor = Color.WHITE

    fun onDraw(chain: XposedInterface.Chain): Any? {
        val view = chain.thisObject as? View
        val canvas = chain.args.firstOrNull() as? Canvas
        if (view == null || canvas == null) return chain.proceed()

        HookClient.attach(view)
        val cfg = HookClient.config
        if (cfg == null || !cfg.hookEnabled || !cfg.enabled || cfg.isIdentity) return chain.proceed()

        return try {
            draw(view, canvas, cfg)
            null
        } catch (t: Throwable) {
            HookClient.hookError = "draw: " + t.javaClass.simpleName
            HookClient.log(Log.ERROR, "draw failed: $t")
            HookClient.sendHeartbeatAsync()
            chain.proceed()
        }
    }

    private fun draw(view: View, canvas: Canvas, cfg: PillConfig) {
        val d = view.resources.displayMetrics.density
        val w = view.width.toFloat()
        val h = view.height.toFloat()
        if (w <= 0f || h <= 0f) return

        val radius0 = readFloat(view, "mRadius")
        val bottom0 = readFloat(view, "mBottom")
        val stockColor = readPaintColor(view)
        if (!probed) {
            probed = true
            HookClient.probe = "mRadius=$radius0 mBottom=$bottom0 paint=${stockColor != null} view=${w.toInt()}x${h.toInt()}"
            HookClient.log(Log.INFO, "probe: ${HookClient.probe}")
            HookClient.sendHeartbeatAsync()
        }

        val baseHeight = (radius0 ?: (2f * d)) * 2f
        val bottomMargin = bottom0 ?: (8f * d)

        // ---- geometri ----
        val pillH = (baseHeight * cfg.heightPct / 100f).coerceIn(1f, h)
        val gap = pillH * 0.9f
        val totalW = when (cfg.shape) {
            PillConfig.SHAPE_DOT -> pillH
            PillConfig.SHAPE_DOTS -> pillH * 3f + gap * 2f
            else -> w * cfg.widthPct / 100f
        }.coerceIn(1f, w)
        val free = w - totalW
        val left = (free / 2f * (1f + cfg.hAlign / 100f)).coerceIn(0f, free)
        val bottom = (h - bottomMargin - cfg.offsetDp * d).coerceIn(pillH, h)
        val top = bottom - pillH
        val corner = pillH / 2f * cfg.cornerPct / 100f

        // ---- renk / animasyon ----
        val c1 = when (cfg.colorMode) {
            PillConfig.COLOR_CUSTOM -> cfg.color
            PillConfig.COLOR_MONET -> monet(view, cfg)
            else -> stockColor ?: Color.WHITE
        }
        val baseAlpha = if (cfg.colorMode == PillConfig.COLOR_STOCK) Color.alpha(c1) else 255
        val now = SystemClock.uptimeMillis()
        val speed = cfg.animSpeed / 100f
        var alphaF = cfg.alphaPct / 100f
        if (cfg.animMode == PillConfig.ANIM_BREATHE) {
            val period = (2400f / speed).toLong().coerceAtLeast(300L)
            val phase = (now % period).toFloat() / period
            alphaF *= 0.55f + 0.45f * (0.5f + 0.5f * sin(2.0 * PI * phase).toFloat())
        }

        if (cfg.gradient) {
            val sh = LinearGradient(left, 0f, left + totalW, 0f, c1, cfg.color2, Shader.TileMode.MIRROR)
            if (cfg.animMode == PillConfig.ANIM_FLOW) {
                val period = (3000f / speed).toLong().coerceAtLeast(400L)
                matrix.setTranslate((now % period).toFloat() / period * totalW * 2f, 0f)
                sh.setLocalMatrix(matrix)
            }
            fill.shader = sh
        } else {
            fill.shader = null
        }
        fill.color = c1
        fill.alpha = (baseAlpha * alphaF).toInt().coerceIn(0, 255)

        drawShape(canvas, fill, left, top, totalW, pillH, gap, corner, cfg.shape, 0f)

        if (cfg.strokeDp10 > 0) {
            val sw = cfg.strokeDp10 / 10f * d
            stroke.strokeWidth = sw
            stroke.color = cfg.strokeColor
            stroke.alpha = (Color.alpha(cfg.strokeColor) * alphaF).toInt().coerceIn(0, 255)
            drawShape(canvas, stroke, left, top, totalW, pillH, gap, corner, cfg.shape, sw)
        }

        if (cfg.animMode != PillConfig.ANIM_NONE) view.postInvalidateDelayed(40L)
    }

    private fun drawShape(
        c: Canvas, p: Paint, left: Float, top: Float, totalW: Float, pillH: Float,
        gap: Float, corner: Float, shape: Int, inset: Float,
    ) {
        val i = inset / 2f
        if (shape == PillConfig.SHAPE_DOTS) {
            for (k in 0..2) {
                val x = left + k * (pillH + gap)
                c.drawRoundRect(x + i, top + i, x + pillH - i, top + pillH - i, pillH / 2f, pillH / 2f, p)
            }
        } else {
            val r = if (shape == PillConfig.SHAPE_DOT) pillH / 2f else corner
            val rr = max(r - i, 0f)
            c.drawRoundRect(left + i, top + i, left + totalW - i, top + pillH - i, rr, rr, p)
        }
    }

    private fun monet(view: View, cfg: PillConfig): Int {
        val key = cfg.monetSource * 100 + cfg.monetShade
        val now = SystemClock.uptimeMillis()
        if (key != monetKey || now - monetAt > 5000L) {
            monetKey = key
            monetAt = now
            monetColor = MonetColors.resolve(view.context, cfg.monetSource, cfg.monetShade)
        }
        return monetColor
    }

    private fun readFloat(view: View, name: String): Float? = try {
        (findField(view.javaClass, name)?.get(view) as? Number)?.toFloat()
    } catch (_: Throwable) {
        null
    }

    private fun readPaintColor(view: View): Int? = try {
        (findField(view.javaClass, "mPaint")?.get(view) as? Paint)?.color
    } catch (_: Throwable) {
        null
    }

    private fun findField(cls: Class<*>, name: String): Field? {
        val key = cls.name + "#" + name
        if (fieldCache.containsKey(key)) return fieldCache[key]
        var k: Class<*>? = cls
        var found: Field? = null
        while (k != null && found == null) {
            try {
                found = k.getDeclaredField(name).apply { isAccessible = true }
            } catch (_: NoSuchFieldException) {
                k = k.superclass
            }
        }
        fieldCache[key] = found
        return found
    }
}
