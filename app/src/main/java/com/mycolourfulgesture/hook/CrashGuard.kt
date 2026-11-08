package com.mycolourfulgesture.hook

import android.app.Application
import android.content.Context

// süreç kısa sürede üst üste açılıyorsa (açılış döngüsü) modül kendini kapatır
object CrashGuard {
    private const val PREFS = "mcg_guard"
    private const val WINDOW_MS = 60_000L
    private const val LIMIT = 5

    fun onStart(ctx: Context): Boolean {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (sp.getBoolean("safe", false)) return true
        val key = "starts_" + (Application.getProcessName() ?: "main")
        val now = System.currentTimeMillis()
        val starts = (sp.getString(key, "") ?: "").split(',')
            .mapNotNull { it.toLongOrNull() }
            .filter { now - it in 0L until WINDOW_MS } + now
        val tripped = starts.size >= LIMIT
        sp.edit()
            .putString(key, if (tripped) "" else starts.joinToString(","))
            .putBoolean("safe", tripped)
            .commit()
        return tripped
    }

    fun markStable(ctx: Context) {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = "starts_" + (Application.getProcessName() ?: "main")
        sp.edit().putString(key, "").apply()
    }

    // uygulamadan gelen "yeniden etkinleştir" isteği: bir sonraki başlatmada normal çalışır
    fun applyToken(ctx: Context, token: Int) {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (sp.getInt("seen", 0) != token) {
            sp.edit().putInt("seen", token).putBoolean("safe", false).apply()
        }
    }
}
