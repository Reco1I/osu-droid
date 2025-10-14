package com.reco1l.andengine.ui.form

import com.reco1l.andengine.*
import com.reco1l.andengine.component.backgroundRadius
import com.reco1l.andengine.container.*
import com.reco1l.andengine.shape.*
import com.reco1l.andengine.text.*
import com.reco1l.andengine.theme.FontSize
import com.reco1l.andengine.theme.Radius
import com.reco1l.andengine.theme.srem
import com.reco1l.andengine.ui.*
import com.reco1l.framework.*
import com.reco1l.framework.math.*
import ru.nsu.ccfit.zuev.osu.Config

@Suppress("LeakingThis")
open class FormSlider(initialValue: Float = 0f) : FormControl<Float, UISlider>(initialValue) {

    override val control = UISlider(initialValue).apply {
        width = Full
    }

    override val valueText = UIText().apply {
        anchor = Anchor.CenterRight
        origin = Anchor.CenterRight
        alignment = Anchor.Center
        background = UIBox().apply {
            color = Color4.Black
            alpha = 0.1f
        }

        style = {
            fontSize = FontSize.XS
            padding = Vec4(1f.srem, 0.5f.srem)
            color = it.accentColor
            backgroundRadius = Radius.MD
        }
    }


    init {
        orientation = Orientation.Vertical
        style += {
            spacing = 1f.srem
        }

        linearContainer {
            width = Full
            style = {
                spacing = 2f.srem
            }
            +labelText
            +resetButton

            container {
                width = Full
                +valueText
            }
        }
        +control
    }
}

class FloatPreferenceSlider(private val preferenceKey: String, fallbackValue: Float = 0f) : FormSlider(Config.getFloat(preferenceKey, fallbackValue)) {
    override fun onControlValueChanged() {
        Config.setFloat(preferenceKey, control.value)
        super.onControlValueChanged()
    }
}

class IntPreferenceSlider(private val preferenceKey: String, fallbackValue: Int = 0) : FormSlider(Config.getInt(preferenceKey, fallbackValue).toFloat()) {
    override fun onControlValueChanged() {
        Config.setInt(preferenceKey, control.value.toInt())
        super.onControlValueChanged()
    }
}