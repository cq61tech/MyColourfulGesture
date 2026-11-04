package com.mycolourfulgesture.hook

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.mycolourfulgesture.core.PillConfig
import java.util.WeakHashMap

/**
 * Arka plan + çıkıntı (outline) ile çizilen pill'ler için (Launcher StashedHandleView).
 * Arka planı kendi çizimimizle değiştirir; ölçüler PillDrawable içinde stok pill'in outline'ından türetilir.
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

            // Boyut ve konum artık Drawable içinde, stok pill'in outline'ına göre hesaplanır.
            // View'ın kendi ölçek/öteleme değerleri orijinal haline döner (önceki sürümden kalmasın).
            v.scaleX = s.sx
            v.scaleY = s.sy
            v.translationX = s.tx
            v.translationY = s.ty

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
