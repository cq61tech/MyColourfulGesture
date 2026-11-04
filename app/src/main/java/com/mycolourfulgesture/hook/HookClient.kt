package com.mycolourfulgesture.hook

import android.content.Context
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
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
    @Volatile var logger: ((Int, String) -> Unit)? = null

    private val views: MutableSet<View> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<View, Boolean>()))
    private val started = AtomicBoolean(false)
    private val main = Handler(Looper.getMainLooper())
    private var handler: Handler? = null
    private var observer: ContentObserver? = null
    private var appContext: Context? = null
    private var loggedOk = false

    fun log(priority: Int, msg: String) {
        try {
            logger?.invoke(priority, msg)
        } catch (_: Throwable) {
        }
    }

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
        } catch (t: Throwable) {
            log(Log.WARN, "observer register failed: ${t.javaClass.simpleName}")
        }
        h.post { load(0) }
    }

    private fun buildExtras(): Bundle = Bundle().apply {
        putString(HookStatus.K_FRAMEWORK, frameworkInfo)
        putInt(HookStatus.K_API, apiVersion)
        putString(HookStatus.K_CLASS, hookedClass ?: "")
        putString(HookStatus.K_ERROR, hookError ?: "")
        putString(HookStatus.K_PROBE, probe ?: "")
    }

    /** "get" çağrısı hem ayarı getirir hem de durumu (heartbeat) uygulamaya yazar. */
    private fun load(attempt: Int) {
        val ctx = appContext ?: return
        try {
            val b = ctx.contentResolver.call(ConfigContract.URI, ConfigContract.METHOD_GET, null, buildExtras())
            val cfg = PillConfig.fromBundle(b)
            config = cfg
            if (!loggedOk || cfg.debug) {
                loggedOk = true
                log(Log.INFO, "config loaded (attempt $attempt): identity=${cfg.isIdentity} enabled=${cfg.enabled} hook=${cfg.hookEnabled}")
            }
            invalidateAll()
        } catch (t: Throwable) {
            log(Log.WARN, "config load failed (attempt $attempt): ${t.javaClass.simpleName}: ${t.message}")
            if (attempt < 20) handler?.postDelayed({ load(attempt + 1) }, 3000L)
        }
    }

    fun sendHeartbeatAsync() {
        handler?.post {
            val ctx = appContext ?: return@post
            try {
                ctx.contentResolver.call(ConfigContract.URI, ConfigContract.METHOD_HEARTBEAT, null, buildExtras())
            } catch (t: Throwable) {
                log(Log.WARN, "heartbeat failed: ${t.javaClass.simpleName}")
            }
        }
    }

    private fun invalidateAll() {
        val snapshot = synchronized(views) { views.toList() }
        main.post { snapshot.forEach { it.invalidate() } }
    }
}
