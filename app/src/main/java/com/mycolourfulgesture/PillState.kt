package com.mycolourfulgesture

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycolourfulgesture.core.ConfigContract
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.mycolourfulgesture.core.PillConfig
import com.mycolourfulgesture.core.Profile
import com.mycolourfulgesture.core.ProfileStore
import com.mycolourfulgesture.core.RuleStore
import org.json.JSONObject

class PillState(context: Context) {
    private val app = context.applicationContext
    private val sp = app.getSharedPreferences(ConfigContract.PREFS_CONFIG, Context.MODE_PRIVATE)

    private val profileSp = app.getSharedPreferences("profiles", Context.MODE_PRIVATE)

    var config by mutableStateOf(PillConfig.load(sp))
        private set

    private val ruleSp = app.getSharedPreferences(RuleStore.PREFS, Context.MODE_PRIVATE)

    val rules = mutableStateMapOf<String, PillConfig>().apply { putAll(RuleStore.load(ruleSp)) }

    fun setRule(name: String, cfg: PillConfig) {
        rules[name] = cfg
        RuleStore.save(ruleSp, name, cfg)
        app.contentResolver.notifyChange(ConfigContract.URI, null)
    }

    fun clearRule(name: String) {
        rules.remove(name)
        RuleStore.save(ruleSp, name, null)
        app.contentResolver.notifyChange(ConfigContract.URI, null)
    }

    fun resetGuard() {
        sp.edit().putInt("guard_token", sp.getInt("guard_token", 0) + 1).apply()
        app.contentResolver.notifyChange(ConfigContract.URI, null)
    }

    fun reload() {
        config = PillConfig.load(sp)
    }

    fun styleText(index: Int): String? {
        val p = profiles.getOrNull(index) ?: return null
        return JSONObject().put("name", p.name).put("cfg", JSONObject(p.cfg.toJson())).toString()
    }

    fun importStyleText(text: String): Boolean = try {
        val o = JSONObject(text)
        val cfg = if (o.has("cfg")) PillConfig.fromJson(o.getJSONObject("cfg").toString()) else PillConfig.fromJson(text)
        if (cfg == null) {
            false
        } else {
            profiles.add(Profile(o.optString("name", "Imported").take(24), cfg))
            ProfileStore.save(profileSp, profiles)
            true
        }
    } catch (_: Throwable) {
        false
    }

    val profiles = mutableStateListOf<Profile>().apply { addAll(ProfileStore.load(profileSp)) }

    fun addProfile(name: String) {
        profiles.add(Profile(name, config))
        ProfileStore.save(profileSp, profiles)
    }

    fun deleteProfile(index: Int) {
        if (index in profiles.indices) {
            profiles.removeAt(index)
            ProfileStore.save(profileSp, profiles)
        }
    }

    fun applyProfile(index: Int) {
        val p = profiles.getOrNull(index) ?: return
        val cur = config
        update(p.cfg.copy(hookEnabled = cur.hookEnabled, debug = cur.debug, enabled = true))
        commit()
    }

    fun exportJson(): String = JSONObject()
        .put("version", 2)
        .put("config", JSONObject(config.toJson()))
        .put("profiles", ProfileStore.toJson(profiles))
        .put("rules", JSONObject().also { o -> rules.forEach { (k, v) -> o.put(k, JSONObject(v.toJson())) } })
        .toString(2)

    fun importJson(text: String): Boolean = try {
        val root = JSONObject(text)
        val cfg = if (root.has("config")) PillConfig.fromJson(root.getJSONObject("config").toString())
        else PillConfig.fromJson(text)
        if (cfg == null) {
            false
        } else {
            if (root.has("profiles")) {
                profiles.clear()
                profiles.addAll(ProfileStore.fromJson(root.getJSONArray("profiles")))
                ProfileStore.save(profileSp, profiles)
            }
            if (root.has("rules")) {
                RuleStore.NAMES.forEach {
                    rules.remove(it)
                    RuleStore.save(ruleSp, it, null)
                }
                val ro = root.getJSONObject("rules")
                ro.keys().forEach { k ->
                    if (k in RuleStore.NAMES) {
                        PillConfig.fromJson(ro.getJSONObject(k).toString())?.let { c ->
                            rules[k] = c
                            RuleStore.save(ruleSp, k, c)
                        }
                    }
                }
            }
            import(cfg)
            true
        }
    } catch (_: Throwable) {
        false
    }

    fun update(new: PillConfig) {
        config = new
    }

    fun commit() {
        config.save(sp)
        app.contentResolver.notifyChange(ConfigContract.URI, null)
    }

    fun resetLook() {
        config = PillConfig(hookEnabled = config.hookEnabled, debug = config.debug)
        commit()
    }

    fun resetAll() {
        RuleStore.NAMES.forEach {
            rules.remove(it)
            RuleStore.save(ruleSp, it, null)
        }
        config = PillConfig()
        commit()
    }

    fun import(new: PillConfig) {
        config = new
        commit()
    }
}
