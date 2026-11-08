package com.mycolourfulgesture.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

class ConfigProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val ctx = context ?: return null
        return when (method) {
            ConfigContract.METHOD_GET -> {
                if (extras != null && extras.containsKey(HookStatus.K_FRAMEWORK)) writeStatus(ctx, extras)
                val csp = ctx.getSharedPreferences(ConfigContract.PREFS_CONFIG, Context.MODE_PRIVATE)
                val out = PillConfig.load(csp).toBundle()
                out.putInt("guard_token", csp.getInt("guard_token", 0))
                RuleStore.load(ctx.getSharedPreferences(RuleStore.PREFS, Context.MODE_PRIVATE)).forEach { (n, c) ->
                    out.putAll(c.toBundle("$n:"))
                    out.putBoolean("$n:on", true)
                }
                out
            }

            ConfigContract.METHOD_HEARTBEAT -> {
                writeStatus(ctx, extras)
                Bundle.EMPTY
            }

            else -> null
        }
    }

    private fun writeStatus(ctx: Context, extras: Bundle?) {
        val p = HookStatus.slotPrefix(extras?.getString(HookStatus.K_PKG))
        ctx.getSharedPreferences(ConfigContract.PREFS_STATUS, Context.MODE_PRIVATE).edit()
            .putLong(p + HookStatus.K_TS, System.currentTimeMillis())
            .putString(p + HookStatus.K_FRAMEWORK, extras?.getString(HookStatus.K_FRAMEWORK) ?: "")
            .putInt(p + HookStatus.K_API, extras?.getInt(HookStatus.K_API) ?: 0)
            .putString(p + HookStatus.K_CLASS, extras?.getString(HookStatus.K_CLASS) ?: "")
            .putString(p + HookStatus.K_ERROR, extras?.getString(HookStatus.K_ERROR) ?: "")
            .putString(p + HookStatus.K_PROBE, extras?.getString(HookStatus.K_PROBE) ?: "")
            .putInt(p + HookStatus.K_DRAWS, extras?.getInt(HookStatus.K_DRAWS) ?: 0)
            .putBoolean(p + HookStatus.K_SAFE, extras?.getBoolean(HookStatus.K_SAFE) ?: false)
            .apply()
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}
