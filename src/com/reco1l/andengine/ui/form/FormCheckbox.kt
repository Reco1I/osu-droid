package com.reco1l.andengine.ui.form

import com.reco1l.andengine.*
import com.reco1l.andengine.theme.srem
import com.reco1l.andengine.ui.*
import org.anddev.andengine.input.touch.*
import ru.nsu.ccfit.zuev.osu.*

@Suppress("LeakingThis")
open class FormCheckbox(initialValue: Boolean = false) : FormControl<Boolean, UICheckbox>(initialValue) {

    override val control = UICheckbox(initialValue).apply {
        anchor = Anchor.CenterRight
        origin = Anchor.CenterRight
    }


    init {
        +labelText
        +resetButton

        container {
            width = Full
            +control
        }

        style += {
            spacing = 2f.srem
        }
    }


    override fun onAreaTouched(event: TouchEvent, localX: Float, localY: Float): Boolean {
        if (!super.onAreaTouched(event, localX, localY) && event.isActionUp) {
            control.value = !control.value
        }
        return true
    }

}

class PreferenceCheckbox(private val preferenceKey: String, fallback: Boolean = false) : FormCheckbox(Config.getBoolean(preferenceKey, fallback)) {
    override fun onControlValueChanged() {
        Config.setBoolean(preferenceKey, value)
        super.onControlValueChanged()
    }
}