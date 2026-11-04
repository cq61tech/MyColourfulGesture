package com.mycolourfulgesture.hook

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.mycolourfulgesture.core.PillConfig
import java.lang.ref.WeakReference

/**
 * Pill view'ının arka planı olarak takılır. ColorDrawable'dan türer, böylece orijinal kod
 * arka planı ColorDrawable'a çevirip rengini animasyonlarsa çökme olmaz; stok renk de buradan okunur.
 */
class PillDrawable(view: View, stock: Int?) : ColorDrawable(stock ?: Color.WHITE) {
    @Volatile var cfg: PillConfig? = null
    private val ref = WeakReference(view)

    override fun draw(canvas: Canvas) {
        val c = cfg
        val view = ref.get()
        if (c == null || view == null) {
            super.draw(canvas)
            return
        }
        val b = bounds
        val w = b.width().toFloat()
        val h = b.height().toFloat()
        if (w <= 0f || h <= 0f) return
        val d = view.resources.displayMetrics.density
        val gap = h * 0.9f
        val totalW = when (c.shape) {
            PillConfig.SHAPE_DOT -> h
            PillConfig.SHAPE_DOTS -> h * 3f + gap * 2f
            else -> w
        }.coerceAtMost(w)
        val left = b.left + (w - totalW) / 2f
        val corner = h / 2f * c.cornerPct / 100f
        val animating = PillRenderer.render(canvas, view.context, c, left, b.top.toFloat(), totalW, h, gap, corner, d, color)
        if (animating) view.postInvalidateDelayed(40L)
    }
}
