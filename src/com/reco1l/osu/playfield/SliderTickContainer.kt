package com.reco1l.osu.playfield

import com.reco1l.framework.*
import com.reco1l.osu.graphics.*
import com.rian.osu.beatmap.hitobject.*
import com.rian.osu.beatmap.hitobject.sliderobject.*
import ru.nsu.ccfit.zuev.osu.*

class SliderTickContainer : ExtendedEntity() {

    fun init(beatmapSlider: Slider) {

        detachChildren()

        val scale = beatmapSlider.gameplayScale

        for (i in 1 until beatmapSlider.nestedHitObjects.size) {

            val sliderTick = beatmapSlider.nestedHitObjects[i] as? SliderTick ?: break
            val position = sliderTick.gameplayStackedPosition
            val sprite = SliderTickSprite.pool.obtain()

            sprite.setPosition(position.x, position.y)
            sprite.setScale(scale)
            sprite.alpha = 0f

            attachChild(sprite)
        }

    }

    override fun onDetached() {
        detachChildren()
    }

}


class SliderTickSprite : ExtendedSprite() {

    init {
        textureRegion = ResourceManager.getInstance().getTexture("sliderscorepoint")
        originX = 0.5f
        originY = 0.5f
    }

    override fun onDetached() {
        pool.free(this)
    }

    companion object {

        @JvmStatic
        val pool = Pool { SliderTickSprite() }

    }

}
