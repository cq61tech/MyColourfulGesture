package com.mycolourfulgesture.hook

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.mycolourfulgesture.core.PillConfig
import java.util.WeakHashMap

/**
 * Arka plan + çıkıntı (outline) ile çizilen pill'ler için (Launcher StashedHandleView).
 * Arka planı kendi çizimimizle değiştirir; genişlik/yükseklik ölçekle, konum ötelemeyle ayarlanır.
 * Ana iş parçacığında çağrılmalıdır.
 */
object ViewStyler {
    private class Saved(
        val bg: Drawable?, val clip: Boolean,
        val sx: Float, val sy: Float, val tx: Float, val ty: Float,
    )

    private val saved = WeakHashMap<View, Saved>()
    private val drawables = WeakHashMap<View, PillDrawable>()

    fun apply(v: View, cfg: PillConfig?) {
        try {
            if (cfg == null) return
            val active = cfg.hookEnabled && cfg.enabled && !cfg.isIdentity
            if (!active) {
                restore(v)
                return
            }
            val s = saved.getOrPut(v) {
                Saved(v.background, v.clipToOutline, v.scaleX, v.scaleY, v.translationX, v.translationY)
            }
            val d = drawables.getOrPut(v) { PillDrawable(v, (s.bg as? ColorDrawable)?.color) }
            d.cfg = cfg
            if (v.background !== d) v.background = d
            v.clipToOutline = false

            val density = v.resources.displayMetrics.density
            val sy = cfg.heightPct / 100f
            v.pivotX = v.width / 2f
            v.pivotY = v.height.toFloat()
            v.scaleX = if (cfg.shape == PillConfig.SHAPE_BAR) cfg.widthPct / 100f else sy
            v.scaleY = sy
            v.translationY = s.ty - cfg.offsetDp * density
            val parentW = (v.parent as? View)?.width ?: v.width
            v.translationX = s.tx + (parentW - v.width) / 2f * cfg.hAlign / 100f

            HookClient.entered += 1
            v.invalidate()
        } catch (t: Throwable) {
            HookClient.errors += 1
            HookClient.log(Log.WARN, "style failed: $t")
        }
    }

    fun restore(v: View) {
        val s = saved.remove(v) ?: return
        drawables.remove(v)
        v.background = s.bg
        v.clipToOutline = s.clip
        v.scaleX = s.sx
        v.scaleY = s.sy
        v.translationX = s.tx
        v.translationY = s.ty
    }
}
