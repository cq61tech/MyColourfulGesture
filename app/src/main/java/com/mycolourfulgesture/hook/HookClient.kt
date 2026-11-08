package com.mycolourfulgesture.hook

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.BatteryManager
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
import com.mycolourfulgesture.core.RuleStore
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

private val HANDLE_IDS = setOf(
    "home_handle", "navigation_handle", "nav_handle", "gesture_handle", "gesture_hint", "nav_bar_handle",
)

object HookClient {
    @Volatile var base: PillConfig? = null
    @Volatile var rules: Map<String, PillConfig> = emptyMap()
    @Volatile var frameworkInfo: String = ""
    @Volatile var apiVersion: Int = 0
    @Volatile var hookedClass: String? = null
    @Volatile var hookError: String? = null
    @Volatile var viewInfo: String? = null
    @Volatile var draws: Int = 0
    @Volatile var entered: Int = 0
    @Volatile var safeMode: Boolean = false
    @Volatile var errors: Int = 0
    @Volatile var discovery: String? = null
    @Volatile var hooker: ((Class<*>) -> String)? = null
    @Volatile var suppressHooker: ((Class<*>) -> String)? = null
    @Volatile var pkg: String = ""
    @Volatile var logger: ((Int, String) -> Unit)? = null

    private val views: MutableSet<View> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<View, Boolean>()))
    private val styled: MutableSet<View> =
        Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap<View, Boolean>()))
    private var foundOnce = false
    private val started = AtomicBoolean(false)
    private val main = Handler(Looper.getMainLooper())
    private var handler: Handler? = null
    private var observer: ContentObserver? = null
    private var appContext: Context? = null
    private var loggedOk = false
    private var lastDiscoveryLog: String? = null
    @Volatile private var charging = false
    @Volatile private var lowBattery = false
    private var mergedKey: String? = null
    private var mergedFrom: PillConfig? = null
    private var merged: PillConfig? = null

    val config: PillConfig? get() {
        if (safeMode) return null
        val ctx = appContext ?: return base
        return current(ctx)
    }

    // düşük pil > şarj > yatay sırasıyla, tanımlı olan ilk kural geçerli olur
    private fun current(ctx: Context): PillConfig? {
        val b = base ?: return null
        if (rules.isEmpty()) return b
        val landscape = ctx.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val key = when {
            lowBattery && rules.containsKey("low") -> "low"
            charging && rules.containsKey("charging") -> "charging"
            landscape && rules.containsKey("landscape") -> "landscape"
            else -> null
        } ?: return b
        if (key == mergedKey && b === mergedFrom) return merged
        val m = rules.getValue(key).copy(hookEnabled = b.hookEnabled, debug = b.debug, enabled = b.enabled)
        mergedKey = key
        mergedFrom = b
        merged = m
        return m
    }

    private fun updateBattery(i: Intent) {
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        charging = plugged != 0 || status == BatteryManager.BATTERY_STATUS_CHARGING
        lowBattery = !charging && level >= 0 && level * 100 / scale <= 15
    }

    val isSystemUi: Boolean get() = pkg == "com.android.systemui"

    fun isStyled(v: Any?): Boolean = v is View && styled.contains(v)

    private fun applyStylesNow() {
        val cfg = config
        val snap = synchronized(styled) { styled.toList() }
        if (snap.isEmpty()) return
        snap.forEach { ViewStyler.apply(it, cfg) }
    }

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
        if (started.compareAndSet(false, true)) {
            pkg = ctx.packageName ?: ""
            start(ctx)
        }
    }

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
        safeMode = CrashGuard.onStart(ctx)
        if (safeMode) log(Log.WARN, "safe mode: repeated restarts detected, hooks are off")
        main.postDelayed({ CrashGuard.markStable(ctx) }, 120_000L)
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
        try {
            val sticky = ctx.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(c: Context, i: Intent) {
                    updateBattery(i)
                    invalidateAll()
                    main.post { applyStylesNow() }
                }
            }, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (sticky != null) updateBattery(sticky)
        } catch (t: Throwable) {
            log(Log.WARN, "battery receiver failed: ${t.javaClass.simpleName}")
        }
        log(Log.INFO, "device: ${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE} / ${Build.FINGERPRINT}")
        longArrayOf(4000L, 10000L, 20000L, 40000L, 70000L).forEach { d -> main.postDelayed({ discover() }, d) }
        if (!isSystemUi) {
            val restyle = object : Runnable {
                override fun run() {
                    applyStylesNow()
                    main.postDelayed(this, 4000L)
                }
            }
            main.postDelayed(restyle, 4000L)
        }
        val beat = object : Runnable {
            override fun run() {
                sendHeartbeatAsync()
                if (!foundOnce || (!isSystemUi && styled.isEmpty())) main.post { discover() }
                h.postDelayed(this, 60000L)
            }
        }
        h.postDelayed(beat, 60000L)
    }

    private fun buildExtras(): Bundle = Bundle().apply {
        putString(HookStatus.K_PKG, pkg)
        putString(HookStatus.K_FRAMEWORK, frameworkInfo)
        putInt(HookStatus.K_API, apiVersion)
        putString(HookStatus.K_CLASS, hookedClass ?: "")
        putString(HookStatus.K_ERROR, hookError ?: "")
        putString(
            HookStatus.K_PROBE,
            ("[$pkg] entered=$entered draws=$draws err=$errors cfg=${if (config != null) "ok" else "none"} " +
                "${discovery ?: ""} ${viewInfo ?: ""}").trim().take(480),
        )
        putInt(HookStatus.K_DRAWS, entered)
        putBoolean(HookStatus.K_SAFE, safeMode)
    }

    private fun load(attempt: Int) {
        val ctx = appContext ?: return
        try {
            val b = ctx.contentResolver.call(ConfigContract.URI, ConfigContract.METHOD_GET, null, buildExtras())
            val cfg = PillConfig.fromBundle(b)
            if (b != null) CrashGuard.applyToken(ctx, b.getInt("guard_token", 0))
            base = cfg
            rules = if (b == null) emptyMap() else RuleStore.NAMES
                .filter { b.getBoolean("$it:on", false) }
                .associateWith { PillConfig.fromBundle(b, "$it:") }
            if (!loggedOk || cfg.debug) {
                loggedOk = true
                log(Log.INFO, "config loaded (attempt $attempt): identity=${cfg.isIdentity} enabled=${cfg.enabled} hook=${cfg.hookEnabled}")
            }
            invalidateAll()
            main.post { applyStylesNow() }
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

    // pencere ağacında pill view'ını bulur ve ilk bulunanı stillendirir
    private fun discover() {
        if (safeMode) return
        val found = ArrayList<View>()
        val names = LinkedHashSet<String>()
        var roots = 0
        try {
            rootViews().forEach { root ->
                roots++
                walk(root, found, names, 0)
            }
        } catch (t: Throwable) {
            discovery = "scan failed: " + t.javaClass.simpleName
            log(Log.WARN, "discover failed: $t")
            return
        }

        val sb = StringBuilder("roots=$roots found=${found.size}")
        for (v in found.take(3)) {
            views.add(v)
            foundOnce = true
            val res = try {
                if (isSystemUi) {
                    hooker?.invoke(v.javaClass) ?: "no hooker"
                } else {
                    styled.add(v)
                    ViewStyler.apply(v, config)
                    hookedClass = v.javaClass.name
                    hookError = null
                    "styled; " + (suppressHooker?.invoke(v.javaClass) ?: "no suppress")
                }
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

    private fun rootViews(): List<View> {
        val wmg = Class.forName("android.view.WindowManagerGlobal")
        val inst = wmg.getMethod("getInstance").invoke(null)
        try {
            val names = wmg.getMethod("getViewRootNames").invoke(inst) as? Array<*> ?: emptyArray<Any>()
            val getRoot = wmg.getMethod("getRootView", String::class.java)
            val out = names.mapNotNull { getRoot.invoke(inst, it) as? View }
            if (out.isNotEmpty()) return out
        } catch (_: Throwable) {
        }
        val field = wmg.getDeclaredField("mViews").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        return (field.get(inst) as? List<View>) ?: emptyList()
    }

    private fun walk(v: View, out: MutableList<View>, names: MutableSet<String>, depth: Int) {
        if (depth > 40) return
        val cn = v.javaClass.name
        val id = idOf(v)
        val isHandle = cn.contains("NavigationHandle", true) || cn.contains("NavHandle", true) ||
            cn.contains("HandleView", true) || id in HANDLE_IDS
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
