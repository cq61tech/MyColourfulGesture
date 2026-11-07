package com.mycolourfulgesture.ui

import com.mycolourfulgesture.R
import com.mycolourfulgesture.core.PillConfig

internal data class Preset(val nameRes: Int, val cfg: PillConfig)

internal val PRESETS = listOf(
    Preset(R.string.preset_pride, PillConfig(
        gradient = 2, widthPct = 100, heightPct = 200, animMode = PillConfig.ANIM_FLOW, animSpeed = 70)),
    Preset(R.string.preset_spectrum, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, animMode = PillConfig.ANIM_HUE, animSpeed = 90,
        glowPct = 55, heightPct = 160)),
    Preset(R.string.preset_aurora, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFF00E5A8.toInt(), gradient = 1, color2 = 0xFF7C4DFF.toInt(),
        widthPct = 85, heightPct = 150, animMode = PillConfig.ANIM_FLOW, animSpeed = 80)),
    Preset(R.string.preset_lava, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFFFF5722.toInt(), gradient = 1, color2 = 0xFFFFC107.toInt(),
        heightPct = 160, animMode = PillConfig.ANIM_FLOW, animSpeed = 120)),
    Preset(R.string.preset_candy, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFFFF80AB.toInt(), gradient = 1, color2 = 0xFF82B1FF.toInt(),
        widthPct = 70, heightPct = 130)),
    Preset(R.string.preset_ocean, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFF00B0FF.toInt(), gradient = 1, color2 = 0xFF00E5FF.toInt(),
        widthPct = 90, heightPct = 140, animMode = PillConfig.ANIM_BREATHE, animSpeed = 70)),
    Preset(R.string.preset_glass, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = -1, alphaPct = 45, widthPct = 80, heightPct = 150,
        strokeDp10 = 8, strokeColor = -1)),
    Preset(R.string.preset_neon, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFF00E5FF.toInt(), heightPct = 150, glowPct = 60,
        animMode = PillConfig.ANIM_BREATHE)),
    Preset(R.string.preset_sunset, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = 0xFFFF7A59.toInt(), gradient = 1,
        color2 = 0xFFB05CC4.toInt(), widthPct = 70, heightPct = 140)),
    Preset(R.string.preset_soft, PillConfig(
        colorMode = PillConfig.COLOR_MONET, monetSource = 1, monetShade = 4, alphaPct = 90, heightPct = 120)),
    Preset(R.string.preset_minimal, PillConfig(alphaPct = 70, widthPct = 40, heightPct = 60)),
    Preset(R.string.preset_chunky, PillConfig(
        colorMode = PillConfig.COLOR_CUSTOM, color = -1, widthPct = 60, heightPct = 280, cornerPct = 40,
        strokeDp10 = 15, strokeColor = 0xFF000000.toInt())),
    Preset(R.string.preset_dots, PillConfig(
        colorMode = PillConfig.COLOR_MONET, monetSource = 1, monetShade = 5,
        shape = PillConfig.SHAPE_DOTS, heightPct = 130)),
)
