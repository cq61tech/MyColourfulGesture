package com.mycolourfulgesture.core

import android.content.SharedPreferences

object RuleStore {
    const val PREFS = "rules"
    val NAMES = listOf("charging", "low", "landscape")

    fun load(sp: SharedPreferences): Map<String, PillConfig> {
        val out = LinkedHashMap<String, PillConfig>()
        for (n in NAMES) {
            val c = sp.getString(n, null)?.let { PillConfig.fromJson(it) }
            if (c != null) out[n] = c
        }
        return out
    }

    fun save(sp: SharedPreferences, name: String, cfg: PillConfig?) {
        val e = sp.edit()
        if (cfg == null) e.remove(name) else e.putString(name, cfg.toJson())
        e.apply()
    }
}
