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

/** Hem uygulama hem SystemUI hook'u kullanır. Varsayılanlar = stok pill (isIdentity). */
data class PillConfig(
    val hookEnabled: Boolean = true,
    val debug: Boolean = false,
    val enabled: Boolean = true,
    val colorMode: Int = COLOR_STOCK,
    val color: Int = -1,
    val monetSource: Int = 1,
    val monetShade: Int = 5,
    val alphaPct: Int = 100,
    val gradient: Boolean = false,
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
) {
    val isIdentity: Boolean
        get() = colorMode == COLOR_STOCK && alphaPct == 100 && !gradient && animMode == ANIM_NONE &&
            shape == SHAPE_BAR && widthPct == 100 && heightPct == 100 && cornerPct == 100 &&
            offsetDp == 0 && hAlign == 0 && strokeDp10 == 0

    private fun entries(): Map<String, Int> = linkedMapOf(
        "c_hook" to b(hookEnabled), "c_debug" to b(debug), "c_enabled" to b(enabled),
        "c_color_mode" to colorMode, "c_color" to color,
        "c_monet_src" to monetSource, "c_monet_shade" to monetShade,
        "c_alpha" to alphaPct, "c_gradient" to b(gradient), "c_color2" to color2,
        "c_anim" to animMode, "c_anim_speed" to animSpeed, "c_shape" to shape,
        "c_width" to widthPct, "c_height" to heightPct, "c_corner" to cornerPct,
        "c_offset" to offsetDp, "c_halign" to hAlign,
        "c_stroke" to strokeDp10, "c_stroke_color" to strokeColor,
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
                gradient = g("c_gradient", 0) == 1,
                color2 = g("c_color2", d.color2),
                animMode = g("c_anim", d.animMode).coerceIn(0, 2),
                animSpeed = g("c_anim_speed", d.animSpeed).coerceIn(30, 300),
                shape = g("c_shape", d.shape).coerceIn(0, 2),
                widthPct = g("c_width", d.widthPct).coerceIn(5, 100),
                heightPct = g("c_height", d.heightPct).coerceIn(30, 600),
                cornerPct = g("c_corner", d.cornerPct).coerceIn(0, 100),
                offsetDp = g("c_offset", d.offsetDp).coerceIn(-8, 30),
                hAlign = g("c_halign", d.hAlign).coerceIn(-100, 100),
                strokeDp10 = g("c_stroke", d.strokeDp10).coerceIn(0, 40),
                strokeColor = g("c_stroke_color", d.strokeColor),
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

/** Monet (sistem paleti) renklerini çözer. Hem uygulama hem SystemUI tarafında kullanılır. */
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
