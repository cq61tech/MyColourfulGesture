package com.mycolourfulgesture

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycolourfulgesture.core.ConfigContract
import com.mycolourfulgesture.core.PillConfig

/** update() sadece arayüzü günceller (canlı önizleme), commit() kaydeder ve SystemUI'yi haberdar eder. */
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

    /** Sadece pill görünümünü sıfırlar; ana anahtar ve hata ayıklama ayarı korunur. */
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
