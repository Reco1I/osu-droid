package com.rian.osu.beatmap.sections

import com.rian.osu.beatmap.ComboColor4
import ru.nsu.ccfit.zuev.osu.data.Color4

/**
 * Contains information about combo and skin colors of a beatmap.
 */
class BeatmapColor {
    /**
     * The combo colors of this beatmap.
     */
    @JvmField
    val comboColors = mutableListOf<ComboColor4>()

    /**
     * The color of the slider border.
     */
    @JvmField
    var sliderBorderColor: Color4? = null
}
