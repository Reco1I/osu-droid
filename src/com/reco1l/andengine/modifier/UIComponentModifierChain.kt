package com.reco1l.andengine.modifier

import com.edlplan.framework.easing.Easing
import com.reco1l.andengine.modifier.UIComponentModifierType.*
import com.reco1l.framework.*


/**
 * A chain of modifiers that can be applied to an entity.
 *
 * An entity is the first node in the chain, and each modifier is a node that follows it.
 */
interface UIComponentModifierChain {

    /**
     * Returns a new modifier that can be used to modify the target.
     */
    fun obtainModifier(block: UIComponentModifier.() -> Unit): UIComponentModifier


    // Translate

    fun translateTo(valueX: Float, valueY: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = TranslateXY
            duration = durationSec

            var initialX = 0f
            var initialY = 0f
            onStart = {
                initialX = it.translationX
                initialY = it.translationY
            }

            onUpdate = { component, time ->
                component.translationX = Interpolation.floatAt(time, initialX, valueX, startTime, endTime, easing)
                component.translationY = Interpolation.floatAt(time, initialY, valueY, startTime, endTime, easing)
            }
        }
    }

    fun translateToX(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = TranslateX
            duration = durationSec

            var initialX = 0f
            onStart = { initialX = it.translationX }
            onUpdate = { component, time -> component.translationX = Interpolation.floatAt(time, initialX, value, startTime, endTime, easing) }
        }
    }

    fun translateToY(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = TranslateY
            duration = durationSec
            var initialY = 0f
            onStart = { initialY = it.translationY }
            onUpdate = { component, time -> component.translationY = Interpolation.floatAt(time, initialY, value, startTime, endTime, easing) }
        }
    }


    // Move

    fun moveTo(valueX: Float, valueY: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = MoveXY
            duration = durationSec

            var initialX = 0f
            var initialY = 0f
            onStart = {
                initialX = it.x
                initialY = it.y
            }

            onUpdate = { component, time ->
                component.x = Interpolation.floatAt(time, initialX, valueX, startTime, endTime, easing)
                component.y = Interpolation.floatAt(time, initialY, valueY, startTime, endTime, easing)
            }
        }
    }

    fun moveToX(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = MoveX
            duration = durationSec

            var initialX = 0f
            onStart = { initialX = it.x }
            onUpdate = { component, time -> component.x = Interpolation.floatAt(time, initialX, value, startTime, endTime, easing) }
        }
    }

    fun moveToY(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = MoveY
            duration = durationSec

            var initialY = 0f
            onStart = { initialY = it.y }
            onUpdate = { component, time -> component.y = Interpolation.floatAt(time, initialY, value, startTime, endTime, easing) }
        }
    }


    // Scale

    fun scaleTo(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = ScaleXY
            duration = durationSec

            var initialScaleX = 1f
            var initialScaleY = 1f
            onStart = {
                initialScaleX = it.scaleX
                initialScaleY = it.scaleY
            }

            onUpdate = { component, time ->
                component.scaleX = Interpolation.floatAt(time, initialScaleX, value, startTime, endTime, easing)
                component.scaleY = Interpolation.floatAt(time, initialScaleY, value, startTime, endTime, easing)
            }
        }
    }

    fun scaleToX(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = ScaleX
            duration = durationSec

            var initialScaleX = 1f
            onStart = { initialScaleX = it.scaleX }
            onUpdate = { component, time -> component.scaleX = Interpolation.floatAt(time, initialScaleX, value, startTime, endTime, easing) }
        }
    }

    fun scaleToY(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = ScaleY
            duration = durationSec

            var initialScaleY = 1f
            onStart = { initialScaleY = it.scaleY }
            onUpdate = { component, time -> component.scaleY = Interpolation.floatAt(time, initialScaleY, value, startTime, endTime, easing) }
        }
    }


    // Coloring

    fun fadeTo(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = Alpha
            duration = durationSec

            var initialAlpha = 1f
            onStart = { initialAlpha = it.alpha }
            onUpdate = { component, time -> component.alpha = Interpolation.floatAt(time, initialAlpha, value, startTime, endTime, easing) }
        }
    }

    fun fadeIn(durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return fadeTo(1f, durationSec, easing)
    }

    fun fadeInFromZero(durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = Alpha
            duration = durationSec
            onUpdate = { component, time -> component.alpha = Interpolation.floatAt(time, 0f, 1f, startTime, endTime, easing) }
        }
    }

    fun fadeOut(durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return fadeTo(0f, durationSec, easing)
    }


    fun colorTo(color: Long, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return colorTo(Color4(color), durationSec, easing)
    }

    fun colorTo(color: Color4, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return colorTo(color.red, color.green, color.blue, durationSec, easing)
    }

    fun colorTo(red: Float, green: Float, blue: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = Color
            duration = durationSec

            var initialRed = 1f
            var initialGreen = 1f
            var initialBlue = 1f
            onStart = {
                initialRed = it.color.red
                initialGreen = it.color.green
                initialBlue = it.color.blue
            }

            onUpdate = { component, time ->
                component.setColor(
                    Interpolation.floatAt(time, initialRed, red, startTime, endTime, easing),
                    Interpolation.floatAt(time, initialGreen, green, startTime, endTime, easing),
                    Interpolation.floatAt(time, initialBlue, blue, startTime, endTime, easing)
                )
            }
        }
    }


    // Rotation

    fun rotateTo(value: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = Rotation
            duration = durationSec

            var initialRotation = 0f
            onStart = { initialRotation = it.rotation }
            onUpdate = { component, time -> component.rotation = Interpolation.floatAt(time, initialRotation, value, startTime, endTime, easing) }
        }
    }


    // Size
    fun sizeTo(width: Float, height: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = SizeXY
            duration = durationSec

            var initialWidth = 0f
            var initialHeight = 0f
            onStart = {
                initialWidth = it.width
                initialHeight = it.height
            }

            onUpdate = { component, time ->
                component.width = Interpolation.floatAt(time, initialWidth, width, startTime, endTime, easing)
                component.height = Interpolation.floatAt(time, initialHeight, height, startTime, endTime, easing)
            }
        }
    }

    fun sizeToX(width: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = SizeX
            duration = durationSec

            var initialWidth = 0f
            onStart = { initialWidth = it.width }
            onUpdate = { component, time -> component.width = Interpolation.floatAt(time, initialWidth, width, startTime, endTime, easing) }
        }
    }

    fun sizeToY(height: Float, durationSec: Float = 0f, easing: Easing = Easing.None): UIComponentModifier {
        return obtainModifier {
            type = SizeY
            duration = durationSec

            var initialHeight = 0f
            onStart = { initialHeight = it.height }
            onUpdate = { component, time -> component.height = Interpolation.floatAt(time, initialHeight, height, startTime, endTime, easing) }
        }
    }
}