package com.mycolourfulgesture.hook

import android.content.Context
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.View
import com.mycolourfulgesture.core.ConfigContract
import com.mycolourfulgesture.core.HookStatus
import com.mycolourfulgesture.core.PillConfig
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

/** SystemUI sürecinde çalışır: ayarları modül uygulamasından alır, değişince view'ları yeniler. */
object HookClient {
    @Volatile var config: PillConfig? = null
    @Volatile var frameworkInfo: String = ""
    @Volatile var apiVersion: Int = 0
    @Volatile var hookedClass: String? = null
    @Volatile var hookError: String? = null
    @Volatile var probe: String? = null

    private val views: MutableSet<View> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<View, Boolean>()))
    private val started = AtomicBoolean(false)
    private val main = Handler(Looper.getMainLooper())
    private var handler: Handler? = null
    private var observer: ContentObserver? = null
    private var appContext: Context? = null

    fun attach(view: View) {
        views.add(view)
        if (started.compareAndSet(false, true)) {
            start(view.context.applicationContext ?: view.context)
        }
    }

    private fun start(ctx: Context) {
        appContext = ctx
        val thread = HandlerThread("MCG-config").also { it.start() }
        val h = Handler(thread.looper)
        handler = h
        val obs = object : ContentObserver(h) {
            override fun onChange(selfChange: Boolean) {
                load(0)
            }
        }
        observer = obs
        try {
            ctx.contentResolver.registerContentObserver(ConfigContract.URI, false, obs)
        } catch (_: Throwable) {
        }
        h.post {
            load(0)
            sendHeartbeat()
        }
    }

    private fun load(attempt: Int) {
        val ctx = appContext ?: return
        try {
            val b = ctx.contentResolver.call(ConfigContract.URI, ConfigContract.METHOD_GET, null, null)
            config = PillConfig.fromBundle(b)
            invalidateAll()
        } catch (_: Throwable) {
            // Modül uygulaması henüz hazır değil olabilir; birkaç kez tekrar dene.
            if (attempt < 12) handler?.postDelayed({ load(attempt + 1) }, 5000L)
        }
    }

    fun sendHeartbeatAsync() {
        handler?.post { sendHeartbeat() }
    }

    private fun sendHeartbeat() {
        val ctx = appContext ?: return
        try {
            val extras = Bundle().apply {
                putString(HookStatus.K_FRAMEWORK, frameworkInfo)
                putInt(HookStatus.K_API, apiVersion)
                putString(HookStatus.K_CLASS, hookedClass ?: "")
                putString(HookStatus.K_ERROR, hookError ?: "")
                putString(HookStatus.K_PROBE, probe ?: "")
            }
            ctx.contentResolver.call(ConfigContract.URI, ConfigContract.METHOD_HEARTBEAT, null, extras)
        } catch (_: Throwable) {
        }
    }

    private fun invalidateAll() {
        val snapshot = synchronized(views) { views.toList() }
        main.post { snapshot.forEach { it.invalidate() } }
    }
}
