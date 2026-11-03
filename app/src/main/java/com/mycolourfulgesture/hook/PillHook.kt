package com.mycolourfulgesture.hook

import android.graphics.Canvas
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class PillHook : XposedModule() {

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != SYSTEMUI) return

        HookClient.frameworkInfo = "$frameworkName $frameworkVersion"
        HookClient.apiVersion = apiVersion

        var lastError: String? = null
        for (name in HANDLE_CLASSES) {
            try {
                val clazz = param.classLoader.loadClass(name)
                val method = clazz.getDeclaredMethod("onDraw", Canvas::class.java)
                hook(method).intercept(XposedInterface.Hooker { chain -> PillDrawer.onDraw(chain) })
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
        val HANDLE_CLASSES = listOf(
            "com.android.systemui.navigationbar.gestural.NavigationHandle",
            "com.android.systemui.navigationbar.views.buttons.NavigationHandle",
            "com.android.systemui.statusbar.phone.NavigationHandle",
        )
    }
}
