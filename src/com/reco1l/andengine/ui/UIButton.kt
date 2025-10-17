package com.reco1l.andengine.ui

import com.edlplan.framework.easing.*
import com.reco1l.andengine.*
import com.reco1l.andengine.component.*
import com.reco1l.andengine.container.*
import com.reco1l.andengine.modifier.*
import com.reco1l.andengine.shape.*
import com.reco1l.andengine.sprite.*
import com.reco1l.andengine.text.*
import com.reco1l.andengine.theme.FontSize
import com.reco1l.andengine.theme.Radius
import com.reco1l.andengine.theme.Size
import com.reco1l.andengine.theme.rem
import com.reco1l.andengine.theme.srem
import com.reco1l.framework.math.*
import org.anddev.andengine.engine.camera.Camera
import org.anddev.andengine.input.touch.TouchEvent
import org.anddev.andengine.opengl.texture.region.TextureRegion
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min

@Suppress("LeakingThis")
open class UIButton : UIContainer() {

    override var style: UIComponent.(Theme) -> Unit = { theme ->
        height = 2.5f.rem
        backgroundColor = if (isSelected) theme.accentColor else theme.accentColor * 0.175f
        backgroundRadius = Radius.LG
        padding = Vec4(2.5f.srem, 0f)
        color = if (isSelected) theme.accentColor * 0.1f else theme.accentColor
        alpha = if (isEnabled) 1f else 0.5f
    }


    //region State

    /**
     * Whether the button is being pressed or not.
     */
    var isPressed = false
        private set

    /**
     * Whether the button is enabled or not. If disabled, the button will not process any
     * touch events.
     */
    open var isEnabled = true
        set(value) {
            if (field != value) {
                field = value
                onEnableStateChange()
            }
        }

    /**
     * Whether the button is selected or not.
     */
    open var isSelected = false
        set(value) {
            if (field != value) {
                field = value
                onSelectionChange()
            }
        }


    private var pressStartTime = 0L

    //endregion

    //region Actions

    /**
     * The action to perform when the button is pressed.
     */
    var onActionDown: (() -> Unit)? = null

    /**
     * The action to perform when the button is released.
     */
    var onActionUp: (() -> Unit)? = null

    /**
     * The action to perform when the button is cancelled.
     */
    var onActionCancel: (() -> Unit)? = null

    /**
     * The action to perform when the button is long pressed.
     */
    var onActionLongPress: (() -> Unit)? = null

    //endregion


    init {
        scaleCenter = Anchor.Center
        preventShrink = true
    }


    //region Callbacks

    /**
     * Called when the enable state of the button changes.
     */
    open fun onEnableStateChange() {
        clearModifiers(ModifierType.Alpha)
        fadeTo(if (isEnabled) 1f else 0.5f, 0.2f)
    }

    /**
     * Called when the selection state of the button changes.
     */
    open fun onSelectionChange() {
        clearModifiers(ModifierType.Color)
        colorTo(if (isSelected) Theme.current.accentColor * 0.1f else Theme.current.accentColor, 0.2f)

        background?.apply {
            clearModifiers(ModifierType.Color)
            colorTo(if (isSelected) Theme.current.accentColor else Theme.current.accentColor * 0.175f)
        }
    }

    //endregion

    //region Touch

    open fun processTouchFeedback(event: TouchEvent) {
        if (event.isActionDown) {
            clearModifiers(ModifierType.ScaleXY)
            scaleTo(0.9f, 0.3f).eased(Easing.Out)
        }

        if ((event.isActionUp || event.isActionCancel) && scaleX != 1f) {
            clearModifiers(ModifierType.ScaleXY)
            scaleTo(1f, 0.4f).eased(Easing.OutElastic)
        }
    }

    override fun onAreaTouched(event: TouchEvent, localX: Float, localY: Float): Boolean {

        if (!isEnabled) {
            return true
        }
        processTouchFeedback(event)

        when {
            event.isActionDown -> {
                isPressed = true
                onActionDown?.invoke()
                pressStartTime = System.currentTimeMillis()
            }

            event.isActionUp -> {
                if (localX <= width && localY <= height && isPressed) {
                    onActionUp?.invoke()
                } else {
                    onActionCancel?.invoke()
                }
                isPressed = false
            }

            event.isActionOutside || event.isActionCancel -> {
                onActionCancel?.invoke()
                isPressed = false
            }

            !event.isActionMove -> isPressed = false
        }

        return true
    }

    //endregion

    override fun onManagedUpdate(deltaTimeSec: Float) {

        if (onActionLongPress != null) {
            if (isPressed && System.currentTimeMillis() - pressStartTime >= 500L) {
                onActionLongPress?.invoke()
                propagateTouchEvent(TouchEvent.ACTION_CANCEL)
            }
        }

        super.onManagedUpdate(deltaTimeSec)
    }

}


/**
 * A button that displays a text.
 */
open class UITextButton : UIButton() {

    private val textComponent = CompoundText().apply {
        width = Size.Full
        anchor = Anchor.CenterLeft
        origin = Anchor.CenterLeft
        style = {
            iconSize = FontSize.MD
            spacing = 1f.srem
        }
        alignment = Anchor.Center
        preventShrink = true
    }


    //region Shortcuts

    var text by textComponent::text
    var fontFamily by textComponent::fontFamily
    var fontSize by textComponent::fontSize
    var leadingIcon by textComponent::leadingIcon
    var trailingIcon by textComponent::trailingIcon
    var alignment by textComponent::alignment

    //endregion

    init {
        +textComponent
    }

}

open class UIIconButton : UIButton() {

    /**
     * The icon of the button.
     */
    var icon: UIComponent? = null
        set(value) {
            if (field != value) {
                field = value

                detachChildren()
                if (value != null) {
                    value.anchor = Anchor.Center
                    value.origin = Anchor.Center
                    attachChild(value)
                }
            }
        }

    init {
        style += {
            icon?.width = FontSize.MD
            icon?.height = FontSize.MD
        }
    }

}

