package com.mycolourfulgesture

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.mycolourfulgesture.core.ConfigContract
import com.mycolourfulgesture.core.PillConfig

class PillTileService : TileService() {

    override fun onStartListening() = refresh()

    override fun onClick() {
        val sp = getSharedPreferences(ConfigContract.PREFS_CONFIG, MODE_PRIVATE)
        val cfg = PillConfig.load(sp)
        cfg.copy(enabled = !cfg.enabled).save(sp)
        contentResolver.notifyChange(ConfigContract.URI, null)
        refresh()
    }

    private fun refresh() {
        val tile = qsTile ?: return
        val on = PillConfig.load(getSharedPreferences(ConfigContract.PREFS_CONFIG, MODE_PRIVATE)).enabled
        tile.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.updateTile()
    }
}
