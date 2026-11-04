package com.mycolourfulgesture.hook

import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
    @Volatile var viewInfo: String? = null
    @Volatile var draws: Int = 0
    @Volatile var entered: Int = 0
    @Volatile var errors: Int = 0
    @Volatile var discovery: String? = null
    @Volatile var hooker: ((Class<*>) -> String)? = null
    @Volatile var logger: ((Int, String) -> Unit)? = null

    private val views: MutableSet<View> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<View, Boolean>()))
    private val started = AtomicBoolean(false)
    private val main = Handler(Looper.getMainLooper())
    private var handler: Handler? = null
    private var observer: ContentObserver? = null
    private var appContext: Context? = null
    private var loggedOk = false
    private var lastDiscoveryLog: String? = null

    fun log(priority: Int, msg: String) {
        try {
            logger?.invoke(priority, msg)
        } catch (_: Throwable) {
        }
    }

    fun attach(view: View) {
        views.add(view)
        ensureStarted(view.context.applicationContext ?: view.context)
    }

    private fun ensureStarted(ctx: Context) {
        if (started.compareAndSet(false, true)) start(ctx)
    }

    /** Çizim beklemeden başlar: uygulama bağlamı hazır olana kadar saniyede bir dener. */
    fun startEarly() {
        val h = Handler(Looper.getMainLooper())
        var tries = 0
        val poll = object : Runnable {
            override fun run() {
                val app = try {
                    Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as? Context
                } catch (_: Throwable) {
                    null
                }
                if (app != null) {
                    ensureStarted(app)
                } else if (++tries < 90) {
                    h.postDelayed(this, 1000L)
                } else {
                    log(Log.WARN, "no application context after 90s")
                }
            }
        }
        h.post(poll)
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
        log(Log.INFO, "device: ${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE} / ${Build.FINGERPRINT}")
        longArrayOf(5000L, 20000L, 60000L, 180000L).forEach { d -> main.postDelayed({ discover() }, d) }
        val beat = object : Runnable {
            override fun run() {
                sendHeartbeatAsync()
                h.postDelayed(this, 30000L)
            }
        }
        h.postDelayed(beat, 30000L)
    }

    private fun buildExtras(): Bundle = Bundle().apply {
        putString(HookStatus.K_FRAMEWORK, frameworkInfo)
        putInt(HookStatus.K_API, apiVersion)
        putString(HookStatus.K_CLASS, hookedClass ?: "")
        putString(HookStatus.K_ERROR, hookError ?: "")
        putString(
            HookStatus.K_PROBE,
            ("entered=$entered draws=$draws err=$errors cfg=${if (config != null) "ok" else "none"} " +
                "${discovery ?: ""} ${viewInfo ?: ""}").trim().take(480),
        )
        putInt(HookStatus.K_DRAWS, entered)
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

    /**
     * SystemUI pencerelerini tarar, pill view'ını (NavigationHandle / home_handle) bulur,
     * sınıfının çizim metodunu yakalar ve ne bulduğunu günlüğe yazar.
     */
    private fun discover() {
        val found = ArrayList<View>()
        val names = LinkedHashSet<String>()
        var roots = 0
        try {
            val wmg = Class.forName("android.view.WindowManagerGlobal")
            val inst = wmg.getMethod("getInstance").invoke(null)
            val rootNames = wmg.getMethod("getViewRootNames").invoke(inst) as? Array<*>
            val getRoot = wmg.getMethod("getRootView", String::class.java)
            rootNames?.forEach { n ->
                val root = getRoot.invoke(inst, n) as? View
                if (root != null) {
                    roots++
                    walk(root, found, names, 0)
                }
            }
        } catch (t: Throwable) {
            discovery = "scan failed: " + t.javaClass.simpleName
            log(Log.WARN, "discover failed: $t")
            return
        }

        val sb = StringBuilder("roots=$roots found=${found.size}")
        for (v in found.take(3)) {
            views.add(v)
            val res = try {
                hooker?.invoke(v.javaClass) ?: "no hooker"
            } catch (t: Throwable) {
                "hook failed: " + t.javaClass.simpleName
            }
            sb.append(" | ").append(v.javaClass.simpleName)
                .append("[").append(idOf(v)).append(" ").append(v.width).append("x").append(v.height)
                .append(" shown=").append(v.isShown).append("] ").append(res)
        }
        if (found.isEmpty() && names.isNotEmpty()) {
            sb.append(" | nav: ").append(names.take(6).joinToString())
        }
        val text = sb.toString()
        discovery = text
        if (text != lastDiscoveryLog) {
            lastDiscoveryLog = text
            log(Log.INFO, "discover: $text")
        }
        sendHeartbeatAsync()
        invalidateAll()
    }

    private fun walk(v: View, out: MutableList<View>, names: MutableSet<String>, depth: Int) {
        if (depth > 40) return
        val cn = v.javaClass.name
        val id = idOf(v)
        val isHandle = cn.contains("NavigationHandle", true) || cn.contains("NavHandle", true) ||
            cn.contains("HandleView", true) || id == "home_handle"
        if (isHandle) out.add(v)
        if (cn.contains("Navigation", true) || id.contains("handle", true)) {
            names.add(cn.substringAfterLast('.') + (if (id.isNotEmpty()) "#$id" else ""))
        }
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) walk(v.getChildAt(i), out, names, depth + 1)
        }
    }

    private fun idOf(v: View): String = try {
        if (v.id != View.NO_ID) v.resources.getResourceEntryName(v.id) else ""
    } catch (_: Throwable) {
        ""
    }

    private fun invalidateAll() {
        val snapshot = synchronized(views) { views.toList() }
        main.post { snapshot.forEach { it.invalidate() } }
    }
}
