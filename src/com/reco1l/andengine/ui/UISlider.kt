package com.reco1l.andengine.ui

import com.edlplan.framework.easing.Easing
import com.reco1l.andengine.*
import com.reco1l.andengine.component.*
import com.reco1l.andengine.container.*
import com.reco1l.andengine.modifier.ModifierType
import com.reco1l.andengine.shape.*
import com.reco1l.andengine.theme.Radius
import com.reco1l.andengine.theme.Size
import com.reco1l.andengine.theme.rem
import com.reco1l.framework.Interpolation
import com.reco1l.framework.math.*
import org.anddev.andengine.input.touch.*
import kotlin.math.*

@Suppress("LeakingThis")
open class UISlider(initialValue: Float = 0f) : UIControl<Float>(initialValue) {

    override var style: UIComponent.(Theme) -> Unit = { theme ->
        height = 2.15f.rem
        backgroundColor = theme.accentColor * 0.25f
        backgroundRadius = Radius.LG
        progressBar.color = theme.accentColor * 0.5f
        thumb.color = theme.accentColor
    }


    /**
     * The minimum allowed value of the slider.
     */
    var min = 0f
        set(min) {
            if (field != min) {
                field = min(min, max)
                value = onProcessValue(value)
            }
        }

    /**
     * The maximum allowed value of the slider.
     */
    var max = 1f
        set(max) {
            if (field != max) {
                field = max(min, max)
                value = onProcessValue(value)
            }
        }

    /**
     * The step size of the slider. If set to 0, the slider will allow any value between min and max.
     */
    var step = 0.0f
        set(step) {
            if (step < 0f) {
                throw IllegalArgumentException("step must be greater than or equal to 0")
            }
            field = step
        }

    /**
     * Called when the user starts dragging the slider.
     */
    var onStartDragging: () -> Unit = {}

    /**
     * Called when the user stops dragging the slider.
     */
    var onStopDragging: () -> Unit = {}


    private val thumb = UIBox().apply {
        height = Size.Full
        anchor = Anchor.CenterLeft
        origin = Anchor.Center
        inheritAncestorsColor = false
        depthInfo = DepthInfo.Less
        clearInfo = ClearInfo.ClearDepthBuffer
        style = {
            width = 1f.rem
            cornerRadius = Radius.LG
        }
    }

    private val progressBar = UIBox().apply {
        height = Size.Full
        anchor = Anchor.CenterLeft
        origin = Anchor.CenterLeft
        depthInfo = DepthInfo.Default
        style = {
            cornerRadius = Radius.LG
        }
    }


    private var isPressed = false
        set(value) {
            if (field != value) {
                field = value
                thumb.clearModifiers(ModifierType.ScaleXY)
                if (value) {
                    thumb.scaleTo(1.25f, 0.1f, Easing.InExpo)
                } else {
                    thumb.scaleTo(1f, 0.3f, Easing.OutBounce)
                }
            }
        }

    private var targetProgressBarWidth = 0f
    private var targetThumbX = 0f


    init {
        width = Size.Full
        background = UIBox()

        attachChild(thumb)
        attachChild(progressBar)

        style(Theme.current)
    }


    override fun onSizeChanged() {
        super.onSizeChanged()
        updateProgress()
    }

    override fun onValueChanged() {
        super.onValueChanged()
        updateProgress()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        updateProgress()
    }

    override fun onProcessValue(value: Float): Float {
        if (step > 0f) {
            return (min + step * round((value - min) / step)).coerceIn(min, max)
        }
        return value.coerceIn(min, max)
    }


    private fun updateProgress() {

        val absoluteProgress = (value - min) / (max - min)

        targetThumbX = (width * absoluteProgress).coerceAtLeast(thumb.width / 2f).coerceAtMost(width - thumb.width / 2f)

        if (min >= 0f) {
            progressBar.anchor = Anchor.CenterLeft
            progressBar.origin = Anchor.CenterLeft
            targetProgressBarWidth = (width * absoluteProgress + thumb.width / 2f).coerceAtLeast(thumb.width).coerceAtMost(width)
            return
        }

        val barCenter = -min / (max - min)

        if (progressBar.anchor.x != barCenter) {
            progressBar.anchor = Vec2(barCenter, 0.5f)
        }

        val partitionWidth = width * if (value < min) barCenter else 1f - barCenter

        targetProgressBarWidth = partitionWidth * abs(value / max)

        val innerCenterX = if (value < 0f) 1f else 0f

        if (progressBar.origin.x != innerCenterX) {
            progressBar.origin = Vec2(innerCenterX, 0.5f)
        }

    }

    override fun onManagedUpdate(deltaTimeSec: Float) {
        progressBar.width = Interpolation.floatAt(deltaTimeSec.coerceIn(0f, 0.3f), progressBar.width, targetProgressBarWidth, 0f, 0.3f, Easing.OutExpo)
        thumb.x = Interpolation.floatAt(deltaTimeSec.coerceIn(0f, 0.3f), thumb.x, targetThumbX, 0f, 0.3f, Easing.OutExpo)
        super.onManagedUpdate(deltaTimeSec)
    }

    override fun onAreaTouched(event: TouchEvent, localX: Float, localY: Float): Boolean {
        if (event.isActionDown || event.isActionMove) {
            if (event.isActionDown) {
                setHierarchyScrollPrevention(true)
                onStartDragging()
            }
            value = (localX / width) * (max - min) + min
            isPressed = true
        } else {
            setHierarchyScrollPrevention(false)
            onStopDragging()
            isPressed = false
        }
        return true
    }

}