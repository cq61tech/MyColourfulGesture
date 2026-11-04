package com.mycolourfulgesture.hook

import android.graphics.Canvas
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class PillHook : XposedModule() {

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        when (param.packageName) {
            SYSTEMUI -> hookSystemUi(param)
            OWN_PACKAGE -> hookSelf(param)
        }
    }

    /** Uygulamanın kendi sürecinde: "LSPosed modülü yüklendi" bilgisini veren iki fonksiyonu değiştirir. */
    private fun hookSelf(param: XposedModuleInterface.PackageReadyParam) {
        try {
            val c = param.classLoader.loadClass("com.mycolourfulgesture.core.ModuleStatus")
            val fw = "$frameworkName $frameworkVersion"
            hook(c.getDeclaredMethod("isModuleActive")).intercept(XposedInterface.Hooker { true })
            hook(c.getDeclaredMethod("moduleFramework")).intercept(XposedInterface.Hooker { fw })
        } catch (t: Throwable) {
            log(Log.WARN, TAG, "self hook failed", t)
        }
    }

    private val hookedKeys = HashSet<String>()

    /** Her yakalanan çizim metodunun ortak girişi: hata olursa stok çizime düşer. */
    private fun guarded(chain: XposedInterface.Chain): Any? {
        HookClient.entered += 1
        return try {
            PillDrawer.onDraw(chain)
        } catch (t: Throwable) {
            HookClient.errors += 1
            if (HookClient.errors <= 3) HookClient.log(Log.ERROR, "intercept failed: $t")
            chain.proceed()
        }
    }

    /** Bulunan view'ın sınıf zincirinde (android.* hariç) ilk onDraw / draw / dispatchDraw'ı yakalar. */
    private fun hookDrawOf(cls: Class<*>): String {
        var k: Class<*>? = cls
        while (k != null && !k.name.startsWith("android.") && !k.name.startsWith("java.")) {
            for (m in arrayOf("onDraw", "draw", "dispatchDraw")) {
                val method = try {
                    k.getDeclaredMethod(m, Canvas::class.java)
                } catch (_: NoSuchMethodException) {
                    null
                } ?: continue
                val key = k.name + "#" + m
                if (!hookedKeys.add(key)) return "already ${k.simpleName}.$m"
                hook(method).intercept(XposedInterface.Hooker { chain -> guarded(chain) })
                log(Log.INFO, TAG, "Hooked $key (discovered)")
                return "hooked ${k.simpleName}.$m"
            }
            k = k.superclass
        }
        return "no draw method in ${cls.simpleName}"
    }

    private fun hookSystemUi(param: XposedModuleInterface.PackageReadyParam) {
        HookClient.frameworkInfo = "$frameworkName $frameworkVersion"
        HookClient.apiVersion = apiVersion
        HookClient.logger = { priority, msg -> log(priority, TAG, msg) }
        HookClient.hooker = { cls -> hookDrawOf(cls) }

        HookClient.startEarly()

        var lastError: String? = null
        for (name in HANDLE_CLASSES) {
            try {
                val clazz = param.classLoader.loadClass(name)
                val method = clazz.getDeclaredMethod("onDraw", Canvas::class.java)
                hook(method).intercept(XposedInterface.Hooker { chain -> guarded(chain) })
                hookedKeys.add(name + "#onDraw")
                HookClient.hookedClass = name
                HookClient.hookError = null
                log(Log.INFO, TAG, "Hooked $name.onDraw")
                return
            } catch (_: ClassNotFoundException) {
                // Bu Android sürümünde sınıf farklı pakette olabilir, sıradakini dene.
            } catch (t: Throwable) {
                lastError = name.substringAfterLast('.') + ": " + t.javaClass.simpleName
                log(Log.ERROR, TAG, "Hook failed for $name", t)
            }
        }
        HookClient.hookError = lastError ?: "NavigationHandle not found"
        log(Log.WARN, TAG, "No handle class hooked: ${HookClient.hookError}")
    }

    private companion object {
        const val TAG = "MyColourfulGesture"
        const val SYSTEMUI = "com.android.systemui"
        const val OWN_PACKAGE = "com.mycolourfulgesture"
        val HANDLE_CLASSES = listOf(
            "com.android.systemui.navigationbar.gestural.NavigationHandle",
            "com.android.systemui.navigationbar.views.buttons.NavigationHandle",
            "com.android.systemui.statusbar.phone.NavigationHandle",
        )
    }
}
