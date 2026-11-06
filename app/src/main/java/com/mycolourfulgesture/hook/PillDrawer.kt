package com.mycolourfulgesture.hook

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import com.mycolourfulgesture.core.PillConfig
import com.mycolourfulgesture.core.PillGeometry
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Field

object PillDrawer {
    private val fieldCache = HashMap<String, Field?>()

    fun onDraw(chain: XposedInterface.Chain): Any? {
        val view = chain.thisObject as? View
        val canvas = chain.args.firstOrNull() as? Canvas
        if (view == null || canvas == null) return chain.proceed()

        HookClient.attach(view)
        HookClient.draws += 1
        if (HookClient.draws == 1) {
            HookClient.viewInfo = "${view.javaClass.simpleName} ${view.width}x${view.height} " +
                "r=${readFloat(view, "mRadius")} b=${readFloat(view, "mBottom")}"
            HookClient.log(Log.INFO, "first draw: ${HookClient.viewInfo}")
            HookClient.sendHeartbeatAsync()
        }
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

        val baseHeight = (readFloat(view, "mRadius") ?: (2f * d)) * 2f
        val bottomMargin = readFloat(view, "mBottom") ?: (8f * d)

        val pillH = (baseHeight * cfg.heightPct / 100f).coerceIn(1f, h)
        val totalW = PillGeometry.totalWidth(cfg, w, pillH, w)
        val free = w - totalW
        val left = (free / 2f * (1f + cfg.hAlign / 100f)).coerceIn(0f, free)
        val bottom = (h - bottomMargin - cfg.offsetDp * d).coerceIn(pillH, h)
        val top = bottom - pillH
        val corner = pillH / 2f * cfg.cornerPct / 100f

        val animating = PillRenderer.render(canvas, view.context, cfg, left, top, totalW, pillH, corner, d, readPaintColor(view))
        if (animating) view.postInvalidateDelayed(40L)
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
