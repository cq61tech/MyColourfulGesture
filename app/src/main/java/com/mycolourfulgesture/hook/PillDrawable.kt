package com.mycolourfulgesture.hook

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.mycolourfulgesture.core.PillConfig
import com.mycolourfulgesture.core.PillGeometry
import java.lang.ref.WeakReference

// ColorDrawable: orijinal kod arka planı ColorDrawable sanıp cast ederse çökmesin
class PillDrawable(view: View, stock: Int?) : ColorDrawable(stock ?: Color.WHITE) {
    @Volatile var cfg: PillConfig? = null
    private val ref = WeakReference(view)
    private val outline = Outline()
    private val tmp = Rect()
    private val stockR = RectF()

    // launcher'da view tüm nav alanı kadar, asıl pill outline içinde küçük bir dikdörtgen
    private fun readStock(view: View, w: Float, h: Float, d: Float) {
        try {
            val p = view.outlineProvider
            if (p != null) {
                outline.setEmpty()
                p.getOutline(view, outline)
                if (outline.getRect(tmp) && !tmp.isEmpty) {
                    stockR.set(tmp)
                    return
                }
            }
        } catch (_: Throwable) {
        }
        val pw = minOf(108f * d, w)
        val ph = 4f * d
        val bottom = h - 8f * d
        stockR.set((w - pw) / 2f, bottom - ph, (w + pw) / 2f, bottom)
    }

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
        readStock(view, w, h, d)

        val pillH = (stockR.height() * c.heightPct / 100f).coerceIn(1f, h)
        val totalW = PillGeometry.totalWidth(c, stockR.width(), pillH, w)

        val leftMax = w - totalW
        val left0 = stockR.centerX() - totalW / 2f
        val left = (
            if (c.hAlign < 0) left0 + (0f - left0) * (-c.hAlign / 100f)
            else left0 + (leftMax - left0) * (c.hAlign / 100f)
        ).coerceIn(0f, leftMax)

        val bottom = (stockR.bottom - c.offsetDp * d).coerceIn(pillH, h)
        val top = bottom - pillH
        val corner = pillH / 2f * c.cornerPct / 100f

        val animating = PillRenderer.render(
            canvas, view.context, c, b.left + left, b.top + top, totalW, pillH, corner, d, color,
        )
        if (animating) view.postInvalidateDelayed(40L)
    }
}
