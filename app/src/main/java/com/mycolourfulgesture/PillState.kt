package com.mycolourfulgesture

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycolourfulgesture.core.ConfigContract
import androidx.compose.runtime.mutableStateListOf
import com.mycolourfulgesture.core.PillConfig
import com.mycolourfulgesture.core.Profile
import com.mycolourfulgesture.core.ProfileStore
import org.json.JSONObject

class PillState(context: Context) {
    private val app = context.applicationContext
    private val sp = app.getSharedPreferences(ConfigContract.PREFS_CONFIG, Context.MODE_PRIVATE)

    private val profileSp = app.getSharedPreferences("profiles", Context.MODE_PRIVATE)

    var config by mutableStateOf(PillConfig.load(sp))
        private set

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
        config = PillConfig()
        commit()
    }

    fun import(new: PillConfig) {
        config = new
        commit()
    }
}
