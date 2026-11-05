package com.mycolourfulgesture

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycolourfulgesture.core.ConfigContract
import com.mycolourfulgesture.core.PillConfig

class PillState(context: Context) {
    private val app = context.applicationContext
    private val sp = app.getSharedPreferences(ConfigContract.PREFS_CONFIG, Context.MODE_PRIVATE)

    var config by mutableStateOf(PillConfig.load(sp))
        private set

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
