package com.mycolourfulgesture.hook

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.os.SystemClock
import com.mycolourfulgesture.core.MonetColors
import com.mycolourfulgesture.core.PillConfig
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

object PillRenderer {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val matrix = Matrix()
    private var monetKey = -1
    private var monetAt = 0L
    private var monetColor = Color.WHITE

    
    fun render(
        canvas: Canvas, ctx: Context, cfg: PillConfig,
        left: Float, top: Float, totalW: Float, pillH: Float, gap: Float, corner: Float,
        density: Float, stockColor: Int?,
    ): Boolean {
        val c1 = when (cfg.colorMode) {
            PillConfig.COLOR_CUSTOM -> cfg.color
            PillConfig.COLOR_MONET -> monet(ctx, cfg)
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

        if (cfg.gradient != 0) {
            val sh = gradient(cfg.gradient, c1, cfg.color2, left, totalW)
            var shift = 0f
            if (cfg.animMode == PillConfig.ANIM_FLOW) {
                val period = (3000f / speed).toLong().coerceAtLeast(400L)
                shift = (now % period).toFloat() / period * totalW * 2f
            }
            matrix.setTranslate(shift, 0f)
            sh.setLocalMatrix(matrix)
            fill.shader = sh
        } else {
            fill.shader = null
        }
        fill.color = c1
        fill.alpha = (baseAlpha * alphaF).toInt().coerceIn(0, 255)
        drawShape(canvas, fill, left, top, totalW, pillH, gap, corner, cfg.shape, 0f)

        if (cfg.strokeDp10 > 0) {
            val sw = cfg.strokeDp10 / 10f * density
            stroke.strokeWidth = sw
            stroke.color = cfg.strokeColor
            stroke.alpha = (Color.alpha(cfg.strokeColor) * alphaF).toInt().coerceIn(0, 255)
            drawShape(canvas, stroke, left, top, totalW, pillH, gap, corner, cfg.shape, sw)
        }
        return cfg.animMode != PillConfig.ANIM_NONE
    }

    private var cached: LinearGradient? = null
    private var cMode = -1
    private var cC1 = 0
    private var cC2 = 0
    private var cLeft = 0f
    private var cWidth = 0f

    private fun gradient(mode: Int, c1: Int, c2: Int, left: Float, width: Float): LinearGradient {
        val g = cached
        if (g != null && cMode == mode && cC1 == c1 && cC2 == c2 && cLeft == left && cWidth == width) return g
        val n = if (mode == 2) {
            LinearGradient(left, 0f, left + width, 0f, PillConfig.RAINBOW, null, Shader.TileMode.MIRROR)
        } else {
            LinearGradient(left, 0f, left + width, 0f, c1, c2, Shader.TileMode.MIRROR)
        }
        cached = n; cMode = mode; cC1 = c1; cC2 = c2; cLeft = left; cWidth = width
        return n
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

    private fun monet(ctx: Context, cfg: PillConfig): Int {
        val key = cfg.monetSource * 100 + cfg.monetShade
        val now = SystemClock.uptimeMillis()
        if (key != monetKey || now - monetAt > 5000L) {
            monetKey = key
            monetAt = now
            monetColor = MonetColors.resolve(ctx, cfg.monetSource, cfg.monetShade)
        }
        return monetColor
    }
}
