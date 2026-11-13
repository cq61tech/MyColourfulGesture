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
import com.mycolourfulgesture.core.PillGeometry
import java.util.Arrays
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

object PillRenderer {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val matrix = Matrix()
    private val hsv = floatArrayOf(0f, 0.75f, 1f)

    private var cached: LinearGradient? = null
    private var cColors: IntArray? = null
    private var cLeft = 0f
    private var cWidth = 0f

    private var monetKey = -1
    private var monetAt = 0L
    private var monetColor = Color.WHITE

    fun render(
        canvas: Canvas, ctx: Context, cfg: PillConfig,
        left: Float, top: Float, totalW: Float, pillH: Float, corner: Float,
        density: Float, stockColor: Int?,
    ): Boolean {
        var c1 = when (cfg.colorMode) {
            PillConfig.COLOR_CUSTOM -> cfg.color
            PillConfig.COLOR_MONET -> monet(ctx, cfg)
            else -> stockColor ?: Color.WHITE
        }
        var baseAlpha = if (cfg.colorMode == PillConfig.COLOR_STOCK) Color.alpha(c1) else 255
        val now = SystemClock.uptimeMillis()
        val speed = cfg.animSpeed / 100f

        if (cfg.animMode == PillConfig.ANIM_HUE) {
            val period = (6000f / speed).toLong().coerceAtLeast(600L)
            hsv[0] = (now % period).toFloat() / period * 360f
            c1 = Color.HSVToColor(hsv)
            baseAlpha = 255
        }

        var alphaF = cfg.alphaPct / 100f
        if (cfg.animMode == PillConfig.ANIM_BREATHE) {
            val period = (2400f / speed).toLong().coerceAtLeast(300L)
            val phase = (now % period).toFloat() / period
            alphaF *= 0.55f + 0.45f * (0.5f + 0.5f * sin(2.0 * PI * phase).toFloat())
        }

        var h = pillH
        var t = top
        var cr = corner
        if (cfg.animMode == PillConfig.ANIM_PULSE &&
            (cfg.shape == PillConfig.SHAPE_BAR || cfg.shape == PillConfig.SHAPE_DASHES)
        ) {
            val period = (1800f / speed).toLong().coerceAtLeast(300L)
            val phase = (now % period).toFloat() / period
            val s = 1f + 0.4f * (0.5f + 0.5f * sin(2.0 * PI * phase).toFloat())
            h = pillH * s
            t = top + pillH - h
            cr = corner * s
        }

        if (cfg.gradient != 0) {
            val colors = if (cfg.gradient == 2) PillConfig.RAINBOW else cfg.stops(c1)
            val sh = gradient(colors, left, totalW)
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
        val a0 = baseAlpha * alphaF

        if (cfg.glowPct > 0) {
            val glowPx = cfg.glowPct / 100f * 8f * density
            for (i in 3 downTo 1) {
                fill.alpha = (a0 * (0.22f - 0.05f * i)).toInt().coerceIn(0, 255)
                drawShape(canvas, fill, left, t, totalW, h, cr, cfg, -2f * glowPx * i / 3f)
            }
        }
        fill.alpha = a0.toInt().coerceIn(0, 255)
        drawShape(canvas, fill, left, t, totalW, h, cr, cfg, 0f)

        if (cfg.strokeDp10 > 0) {
            val sw = cfg.strokeDp10 / 10f * density
            stroke.strokeWidth = sw
            stroke.color = cfg.strokeColor
            stroke.alpha = (Color.alpha(cfg.strokeColor) * alphaF).toInt().coerceIn(0, 255)
            drawShape(canvas, stroke, left, t, totalW, h, cr, cfg, sw)
        }
        return cfg.animMode != PillConfig.ANIM_NONE
    }

    private fun gradient(colors: IntArray, left: Float, width: Float): LinearGradient {
        val g = cached
        if (g != null && Arrays.equals(cColors, colors) && cLeft == left && cWidth == width) return g
        val n = LinearGradient(left, 0f, left + width, 0f, colors, null, Shader.TileMode.MIRROR)
        cached = n
        cColors = colors.copyOf()
        cLeft = left
        cWidth = width
        return n
    }

    private fun drawShape(
        c: Canvas, p: Paint, left: Float, top: Float, totalW: Float, h: Float,
        corner: Float, cfg: PillConfig, inset: Float,
    ) {
        val i = inset / 2f
        val gap = PillGeometry.gap(cfg, h)
        when (cfg.shape) {
            PillConfig.SHAPE_DOTS -> {
                val r = max(h / 2f - i, 0f)
                for (k in 0 until cfg.segCount) {
                    val x = left + k * (h + gap)
                    c.drawRoundRect(x + i, top + i, x + h - i, top + h - i, r, r, p)
                }
            }
            PillConfig.SHAPE_DASHES -> {
                val n = cfg.segCount
                val g = minOf(gap, totalW / n * 0.8f)
                val segW = (totalW - g * (n - 1)) / n
                val rr = max(corner - i, 0f)
                for (k in 0 until n) {
                    val x = left + k * (segW + g)
                    c.drawRoundRect(x + i, top + i, x + segW - i, top + h - i, rr, rr, p)
                }
            }
            PillConfig.SHAPE_DOT -> {
                val r = max(h / 2f - i, 0f)
                c.drawRoundRect(left + i, top + i, left + h - i, top + h - i, r, r, p)
            }
            else -> {
                val rr = max(corner - i, 0f)
                c.drawRoundRect(left + i, top + i, left + totalW - i, top + h - i, rr, rr, p)
            }
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
