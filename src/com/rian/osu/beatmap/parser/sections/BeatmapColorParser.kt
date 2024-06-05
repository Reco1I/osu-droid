package com.rian.osu.beatmap.parser.sections

import com.rian.osu.beatmap.Beatmap
import com.rian.osu.beatmap.ComboColor4
import ru.nsu.ccfit.zuev.osu.data.Color4

/**
 * A parser for parsing a beatmap's colors section.
 */
object BeatmapColorParser : BeatmapKeyValueSectionParser() {
    override fun parse(beatmap: Beatmap, line: String) = splitProperty(line).let { p ->
        val s = p[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (s.size != 3 && s.size != 4) {
            throw UnsupportedOperationException("Color specified in incorrect format (should be R,G,B or R,G,B,A)")
        }

        val color = Color4(
            parseInt(s[0]).toFloat(),
            parseInt(s[1]).toFloat(),
            parseInt(s[2]).toFloat()
        )

        if (p[0].startsWith("Combo")) {
            val index = p[0].substring(5).toIntOrNull() ?: (beatmap.colors.comboColors.size + 1)

            beatmap.colors.comboColors.apply {
                add(ComboColor4(index, color))
                
                sortBy { it.index }
            }
        }

        if (p[0].startsWith("SliderBorder")) {
            beatmap.colors.sliderBorderColor = color
        }
    }
}
