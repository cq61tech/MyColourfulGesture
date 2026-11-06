package com.mycolourfulgesture.core

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class Profile(val name: String, val cfg: PillConfig)

object ProfileStore {
    private const val KEY = "list"

    fun toJson(list: List<Profile>): JSONArray {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("name", it.name).put("cfg", JSONObject(it.cfg.toJson()))) }
        return a
    }

    fun fromJson(a: JSONArray): List<Profile> = (0 until a.length()).mapNotNull { i ->
        try {
            val o = a.getJSONObject(i)
            PillConfig.fromJson(o.getJSONObject("cfg").toString())?.let { Profile(o.getString("name"), it) }
        } catch (_: Throwable) {
            null
        }
    }

    fun load(sp: SharedPreferences): List<Profile> = try {
        fromJson(JSONArray(sp.getString(KEY, "[]")))
    } catch (_: Throwable) {
        emptyList()
    }

    fun save(sp: SharedPreferences, list: List<Profile>) {
        sp.edit().putString(KEY, toJson(list).toString()).apply()
    }
}
