package com.mycolourfulgesture.core

import android.content.Context
import android.os.SystemClock

data class HookStatus(
    val lastSeen: Long,
    val framework: String,
    val api: Int,
    val hookedClass: String?,
    val error: String?,
    val probe: String?,
    val draws: Int,
) {

    val activeThisBoot: Boolean
        get() {
            val bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            return lastSeen >= bootTime
        }

    companion object {
        const val K_TS = "ts"
        const val K_PKG = "pkg"
        const val K_FRAMEWORK = "framework"
        const val K_API = "api"
        const val K_CLASS = "class"
        const val K_ERROR = "error"
        const val K_PROBE = "probe"
        const val K_DRAWS = "draws"

        private fun slot(sp: android.content.SharedPreferences, p: String) = HookStatus(
            lastSeen = sp.getLong(p + K_TS, 0L),
            framework = sp.getString(p + K_FRAMEWORK, "") ?: "",
            api = sp.getInt(p + K_API, 0),
            hookedClass = sp.getString(p + K_CLASS, null)?.takeIf { it.isNotEmpty() },
            error = sp.getString(p + K_ERROR, null)?.takeIf { it.isNotEmpty() },
            probe = sp.getString(p + K_PROBE, null)?.takeIf { it.isNotEmpty() },
            draws = sp.getInt(p + K_DRAWS, 0),
        )

        fun slotPrefix(pkg: String?) = if (pkg == "com.android.systemui") "s_" else "l_"

        fun read(context: Context): HookStatus {
            val sp = context.getSharedPreferences(ConfigContract.PREFS_STATUS, Context.MODE_PRIVATE)
            val a = slot(sp, "s_")
            val b = slot(sp, "l_")
            fun score(x: HookStatus) = (if (x.draws > 0) 1L shl 60 else 0L) + x.lastSeen
            return if (score(b) > score(a)) b else a
        }
    }
}
