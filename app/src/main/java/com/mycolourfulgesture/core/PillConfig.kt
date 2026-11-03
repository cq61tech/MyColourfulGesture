package com.mycolourfulgesture.core

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle

object ConfigContract {
    const val AUTHORITY = "com.mycolourfulgesture.config"
    val URI: Uri = Uri.parse("content://$AUTHORITY/pill")
    const val METHOD_GET = "get"
    const val METHOD_HEARTBEAT = "heartbeat"
    const val PREFS_CONFIG = "pill_config"
    const val PREFS_STATUS = "hook_status"
}

/** Hem uygulama hem SystemUI tarafındaki hook bu sınıfı kullanır. Varsayılanlar = stok pill. */
data class PillConfig(
    val enabled: Boolean = true,
    val useCustomColor: Boolean = false,
    val color: Int = -1, // 0xFFFFFFFF
    val alphaPct: Int = 100,
    val widthPct: Int = 100,
    val heightPct: Int = 100,
    val cornerPct: Int = 100,
) {
    /** Hiçbir şey değişmediyse stok çizim kullanılır (en güvenli yol). */
    val isIdentity: Boolean
        get() = !useCustomColor && alphaPct == 100 && widthPct == 100 &&
            heightPct == 100 && cornerPct == 100

    fun toBundle(): Bundle = Bundle().apply {
        putBoolean(K_ENABLED, enabled)
        putBoolean(K_CUSTOM, useCustomColor)
        putInt(K_COLOR, color)
        putInt(K_ALPHA, alphaPct)
        putInt(K_WIDTH, widthPct)
        putInt(K_HEIGHT, heightPct)
        putInt(K_CORNER, cornerPct)
    }

    fun save(sp: SharedPreferences) {
        sp.edit()
            .putBoolean(K_ENABLED, enabled)
            .putBoolean(K_CUSTOM, useCustomColor)
            .putInt(K_COLOR, color)
            .putInt(K_ALPHA, alphaPct)
            .putInt(K_WIDTH, widthPct)
            .putInt(K_HEIGHT, heightPct)
            .putInt(K_CORNER, cornerPct)
            .apply()
    }

    companion object {
        const val K_ENABLED = "enabled"
        const val K_CUSTOM = "custom_color"
        const val K_COLOR = "color"
        const val K_ALPHA = "alpha_pct"
        const val K_WIDTH = "width_pct"
        const val K_HEIGHT = "height_pct"
        const val K_CORNER = "corner_pct"

        fun fromBundle(b: Bundle?): PillConfig {
            val d = PillConfig()
            if (b == null) return d
            return PillConfig(
                enabled = b.getBoolean(K_ENABLED, d.enabled),
                useCustomColor = b.getBoolean(K_CUSTOM, d.useCustomColor),
                color = b.getInt(K_COLOR, d.color),
                alphaPct = b.getInt(K_ALPHA, d.alphaPct),
                widthPct = b.getInt(K_WIDTH, d.widthPct),
                heightPct = b.getInt(K_HEIGHT, d.heightPct),
                cornerPct = b.getInt(K_CORNER, d.cornerPct),
            )
        }

        fun load(sp: SharedPreferences): PillConfig {
            val d = PillConfig()
            return PillConfig(
                enabled = sp.getBoolean(K_ENABLED, d.enabled),
                useCustomColor = sp.getBoolean(K_CUSTOM, d.useCustomColor),
                color = sp.getInt(K_COLOR, d.color),
                alphaPct = sp.getInt(K_ALPHA, d.alphaPct),
                widthPct = sp.getInt(K_WIDTH, d.widthPct),
                heightPct = sp.getInt(K_HEIGHT, d.heightPct),
                cornerPct = sp.getInt(K_CORNER, d.cornerPct),
            )
        }
    }
}
