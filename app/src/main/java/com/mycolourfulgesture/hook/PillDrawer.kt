package com.mycolourfulgesture.hook

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.mycolourfulgesture.core.PillConfig
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Field

/**
 * NavigationHandle.onDraw yerine geçer. Hata olursa veya ayar "stok" ise orijinal çizime düşer.
 * Stok çizimde: pill = view genişliğinde, yüksekliği 2*mRadius, alt kenarı (yükseklik - mBottom).
 */
object PillDrawer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fieldCache = HashMap<String, Field?>()
    private var probed = false

    fun onDraw(chain: XposedInterface.Chain): Any? {
        val view = chain.thisObject as? View
        val canvas = chain.args.firstOrNull() as? Canvas
        if (view == null || canvas == null) return chain.proceed()

        HookClient.attach(view)
        val cfg = HookClient.config
        if (cfg == null || !cfg.enabled || cfg.isIdentity) return chain.proceed()

        return try {
            draw(view, canvas, cfg)
            null
        } catch (t: Throwable) {
            HookClient.hookError = "draw: " + t.javaClass.simpleName
            HookClient.sendHeartbeatAsync()
            chain.proceed()
        }
    }

    private fun draw(view: View, canvas: Canvas, cfg: PillConfig) {
        val density = view.resources.displayMetrics.density
        val w = view.width.toFloat()
        val h = view.height.toFloat()
        if (w <= 0f || h <= 0f) return

        val radius0 = readFloat(view, "mRadius")
        val bottom0 = readFloat(view, "mBottom")
        val stockPaintColor = readPaintColor(view)
        if (!probed) {
            probed = true
            HookClient.probe = "mRadius=$radius0 mBottom=$bottom0 paint=${stockPaintColor != null} view=${w.toInt()}x${h.toInt()}"
            HookClient.sendHeartbeatAsync()
        }

        val baseHeight = (radius0 ?: (2f * density)) * 2f
        val bottomMargin = bottom0 ?: (8f * density)

        val pillH = (baseHeight * cfg.heightPct / 100f).coerceIn(1f, h)
        val pillW = (w * cfg.widthPct / 100f).coerceIn(1f, w)
        val left = (w - pillW) / 2f
        val bottom = (h - bottomMargin).coerceIn(pillH, h)
        val top = bottom - pillH
        val corner = pillH / 2f * cfg.cornerPct / 100f

        val baseColor = if (cfg.useCustomColor) cfg.color else (stockPaintColor ?: Color.WHITE)
        val baseAlpha = if (cfg.useCustomColor) 255 else Color.alpha(baseColor)
        paint.color = baseColor
        paint.alpha = (baseAlpha * cfg.alphaPct / 100f).toInt().coerceIn(0, 255)

        canvas.drawRoundRect(left, top, left + pillW, bottom, corner, corner, paint)
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
