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
    /** SystemUI her açılışta heartbeat gönderir; son açılıştan önceki kayıt "aktif" sayılmaz. */
    val activeThisBoot: Boolean
        get() {
            val bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            return lastSeen >= bootTime && hookedClass != null
        }

    companion object {
        const val K_TS = "ts"
        const val K_FRAMEWORK = "framework"
        const val K_API = "api"
        const val K_CLASS = "class"
        const val K_ERROR = "error"
        const val K_PROBE = "probe"
        const val K_DRAWS = "draws"

        fun read(context: Context): HookStatus {
            val sp = context.getSharedPreferences(ConfigContract.PREFS_STATUS, Context.MODE_PRIVATE)
            return HookStatus(
                lastSeen = sp.getLong(K_TS, 0L),
                framework = sp.getString(K_FRAMEWORK, "") ?: "",
                api = sp.getInt(K_API, 0),
                hookedClass = sp.getString(K_CLASS, null)?.takeIf { it.isNotEmpty() },
                error = sp.getString(K_ERROR, null)?.takeIf { it.isNotEmpty() },
                probe = sp.getString(K_PROBE, null)?.takeIf { it.isNotEmpty() },
                draws = sp.getInt(K_DRAWS, 0),
            )
        }
    }
}
