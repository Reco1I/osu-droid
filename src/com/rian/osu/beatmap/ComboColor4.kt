package com.rian.osu.beatmap

import ru.nsu.ccfit.zuev.osu.data.Color4

/**
 * An extension to [Color4] specifically for combo colors.
 */
class ComboColor4(
    /**
     * The index of this combo color.
     */
    @JvmField val index: Int,

    /**
     * The underlying [Color4].
     */
    color: Color4
) : Color4(color)
