package com.mycolourfulgesture.core

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import org.json.JSONObject

object ConfigContract {
    const val AUTHORITY = "com.mycolourfulgesture.config"
    val URI: Uri = Uri.parse("content://$AUTHORITY/pill")
    const val METHOD_GET = "get"
    const val METHOD_HEARTBEAT = "heartbeat"
    const val PREFS_CONFIG = "pill_config"
    const val PREFS_STATUS = "hook_status"
}

private fun b(v: Boolean) = if (v) 1 else 0

data class PillConfig(
    val hookEnabled: Boolean = true,
    val debug: Boolean = false,
    val enabled: Boolean = true,
    val colorMode: Int = COLOR_STOCK,
    val color: Int = -1,
    val monetSource: Int = 1,
    val monetShade: Int = 5,
    val alphaPct: Int = 100,
    val gradient: Int = 0,
    val color2: Int = 0xFFB05CC4.toInt(),
    val animMode: Int = ANIM_NONE,
    val animSpeed: Int = 100,
    val shape: Int = SHAPE_BAR,
    val widthPct: Int = 100,
    val heightPct: Int = 100,
    val cornerPct: Int = 100,
    val offsetDp: Int = 0,
    val hAlign: Int = 0,
    val strokeDp10: Int = 0,
    val strokeColor: Int = -1,
    val color3: Int = 0xFFFFB84D.toInt(),
    val color4: Int = 0xFF4CD39B.toInt(),
    val color5: Int = 0xFF5B8CFF.toInt(),
    val gradStops: Int = 2,
    val segCount: Int = 3,
    val gapPct: Int = 90,
    val glowPct: Int = 0,
) {
    fun stops(first: Int): IntArray = intArrayOf(first, color2, color3, color4, color5).copyOfRange(0, gradStops)

    val isIdentity: Boolean
        get() = colorMode == COLOR_STOCK && alphaPct == 100 && gradient == 0 && animMode == ANIM_NONE &&
            shape == SHAPE_BAR && widthPct == 100 && heightPct == 100 && cornerPct == 100 &&
            offsetDp == 0 && hAlign == 0 && strokeDp10 == 0 && glowPct == 0

    private fun entries(): Map<String, Int> = linkedMapOf(
        "c_hook" to b(hookEnabled), "c_debug" to b(debug), "c_enabled" to b(enabled),
        "c_color_mode" to colorMode, "c_color" to color,
        "c_monet_src" to monetSource, "c_monet_shade" to monetShade,
        "c_alpha" to alphaPct, "c_gradient" to gradient, "c_color2" to color2,
        "c_anim" to animMode, "c_anim_speed" to animSpeed, "c_shape" to shape,
        "c_width" to widthPct, "c_height" to heightPct, "c_corner" to cornerPct,
        "c_offset" to offsetDp, "c_halign" to hAlign,
        "c_stroke" to strokeDp10, "c_stroke_color" to strokeColor,
        "c_color3" to color3, "c_color4" to color4, "c_color5" to color5,
        "c_stops" to gradStops, "c_segs" to segCount, "c_gap" to gapPct, "c_glow" to glowPct,
    )

    fun toBundle(): Bundle = Bundle().also { bundle -> entries().forEach { (k, v) -> bundle.putInt(k, v) } }

    fun save(sp: SharedPreferences) {
        val e = sp.edit()
        entries().forEach { (k, v) -> e.putInt(k, v) }
        e.apply()
    }

    fun toJson(): String = JSONObject().also { j -> entries().forEach { (k, v) -> j.put(k, v) } }.toString(2)

    companion object {
        const val COLOR_STOCK = 0
        const val COLOR_CUSTOM = 1
        const val COLOR_MONET = 2
        const val ANIM_NONE = 0
        const val ANIM_BREATHE = 1
        const val ANIM_FLOW = 2
        const val SHAPE_BAR = 0
        const val SHAPE_DOT = 1
        const val SHAPE_DOTS = 2
        const val SHAPE_DASHES = 3
        const val ANIM_HUE = 3
        const val ANIM_PULSE = 4
        val RAINBOW = intArrayOf(
            0xFFFF3B30.toInt(), 0xFFFF9500.toInt(), 0xFFFFCC00.toInt(),
            0xFF34C759.toInt(), 0xFF007AFF.toInt(), 0xFFAF52DE.toInt(),
        )
        val MONET_SHADES = intArrayOf(0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)

        private fun from(g: (String, Int) -> Int): PillConfig {
            val d = PillConfig()
            return PillConfig(
                hookEnabled = g("c_hook", 1) == 1,
                debug = g("c_debug", 0) == 1,
                enabled = g("c_enabled", 1) == 1,
                colorMode = g("c_color_mode", d.colorMode).coerceIn(0, 2),
                color = g("c_color", d.color),
                monetSource = g("c_monet_src", d.monetSource).coerceIn(1, 5),
                monetShade = g("c_monet_shade", d.monetShade).coerceIn(0, MONET_SHADES.size - 1),
                alphaPct = g("c_alpha", d.alphaPct).coerceIn(0, 100),
                gradient = g("c_gradient", 0).coerceIn(0, 2),
                color2 = g("c_color2", d.color2),
                animMode = g("c_anim", d.animMode).coerceIn(0, 4),
                animSpeed = g("c_anim_speed", d.animSpeed).coerceIn(30, 300),
                shape = g("c_shape", d.shape).coerceIn(0, 3),
                widthPct = g("c_width", d.widthPct).coerceIn(5, 400),
                heightPct = g("c_height", d.heightPct).coerceIn(30, 600),
                cornerPct = g("c_corner", d.cornerPct).coerceIn(0, 100),
                offsetDp = g("c_offset", d.offsetDp).coerceIn(-16, 40),
                hAlign = g("c_halign", d.hAlign).coerceIn(-100, 100),
                strokeDp10 = g("c_stroke", d.strokeDp10).coerceIn(0, 40),
                strokeColor = g("c_stroke_color", d.strokeColor),
                color3 = g("c_color3", d.color3),
                color4 = g("c_color4", d.color4),
                color5 = g("c_color5", d.color5),
                gradStops = g("c_stops", d.gradStops).coerceIn(2, 5),
                segCount = g("c_segs", d.segCount).coerceIn(1, 9),
                gapPct = g("c_gap", d.gapPct).coerceIn(0, 300),
                glowPct = g("c_glow", d.glowPct).coerceIn(0, 100),
            )
        }

        fun fromBundle(bundle: Bundle?): PillConfig =
            if (bundle == null) PillConfig() else from { k, d -> bundle.getInt(k, d) }

        fun load(sp: SharedPreferences): PillConfig = from { k, d -> sp.getInt(k, d) }

        fun fromJson(text: String): PillConfig? = try {
            val j = JSONObject(text)
            if (!j.has("c_enabled")) null else from { k, d -> j.optInt(k, d) }
        } catch (_: Throwable) {
            null
        }
    }
}

object MonetColors {
    private val PREFIX = arrayOf("", "system_accent1", "system_accent2", "system_accent3", "system_neutral1", "system_neutral2")

    fun resolve(ctx: Context, source: Int, shadeIdx: Int): Int {
        val p = PREFIX.getOrNull(source) ?: return -1
        if (p.isEmpty()) return -1
        val shade = PillConfig.MONET_SHADES.getOrElse(shadeIdx) { 500 }
        val id = ctx.resources.getIdentifier("${p}_$shade", "color", "android")
        return if (id != 0) ctx.getColor(id) else -1
    }
}

object PillGeometry {
    fun gap(cfg: PillConfig, pillH: Float): Float = pillH * cfg.gapPct / 100f

    fun totalWidth(cfg: PillConfig, baseW: Float, pillH: Float, maxW: Float): Float {
        val w = when (cfg.shape) {
            PillConfig.SHAPE_DOT -> pillH
            PillConfig.SHAPE_DOTS -> pillH * cfg.segCount + gap(cfg, pillH) * (cfg.segCount - 1)
            else -> baseW * cfg.widthPct / 100f
        }
        return w.coerceIn(1f, maxW)
    }
}
