package com.reco1l.andengine.modifier

import android.util.*
import androidx.core.util.Pools.SimplePool
import com.reco1l.andengine.component.*
import org.anddev.andengine.entity.IEntity
import org.anddev.andengine.entity.modifier.IEntityModifier
import org.anddev.andengine.util.modifier.*
import kotlin.math.*

class UIComponentModifier(private val pool: SimplePool<UIComponentModifier>? = null) : IEntityModifier, UIComponentModifierChain {

    /**
     * Whether the modifier has started or not.
     */
    var isStarted = false
        private set

    /**
     * The time when the modifier starts.
     */
    var startTime: Float = 0f
        private set

    /**
     * The time when the modifier ends.
     */
    var endTime = 0f
        private set

    /**
     * The type of the modifier.
     */
    var type = UIComponentModifierType.Delay

    /**
     * The target item that this modifier is applied to.
     */
    var target: UIComponent? = null

    /**
     * A callback that is invoked when the modifier starts.
     */
    var onStart: ((component: UIComponent) -> Unit)? = null

    /**
     * A callback that is invoked on each frame update while the modifier is active.
     */
    var onUpdate: ((component: UIComponent, elapsedSec: Float) -> Unit)? = null

    /**
     * A callback that is invoked when the modifier finishes.
     */
    var onFinished: ((component: UIComponent) -> Unit)? = null


    private var elapsedSec = 0f


    override fun onUpdate(deltaSec: Float, component: IEntity): Float {
        component as UIComponent

        if (elapsedSec < startTime || elapsedSec >= endTime || deltaSec == 0f) {
            isStarted = false
            return 0f
        }

        if (!isStarted) {
            isStarted = true
            onStart?.invoke(component)
        }

        val consumedDeltaSec = min(endTime - elapsedSec, deltaSec)
        elapsedSec += consumedDeltaSec

        onUpdate?.invoke(component, elapsedSec)

        if (elapsedSec >= endTime) {
            elapsedSec = endTime
            onFinished?.invoke(component)
        }

        return max(0f, consumedDeltaSec)
    }

    override fun onUnregister() {
        if (pool != null) {
            clear()
            pool.release(this)
        }
    }



    override fun reset() {
        elapsedSec = 0f
        isStarted = false
    }


    /**
     * Clears the modifier, resetting all its properties.
     */
    fun clear() {
        target = null
        isStarted = false
        elapsedSec = 0f
        startTime = 0f
        endTime = 0f
        onStart = null
        onUpdate = null
        onFinished = null
    }

    /**
     * Sets the duration of the modifier.
     */
    fun setDuration(durationSec: Float) {
        endTime = startTime + durationSec
    }

    fun after(block: (component: UIComponent) -> Unit): UIComponentModifier {
        onFinished = block
        return this
    }


    fun then(): UIComponentModifier {
        return obtainModifier {
            startTime = this@UIComponentModifier.endTime
        }
    }

    override fun obtainModifier(block: UIComponentModifier.() -> Unit): UIComponentModifier {
        // If we're calling this on a modifier that has no duration or callbacks, we reuse it.
        return if (onUpdate == null && onStart == null && duration == 0f)
            this
        else
            target?.obtainModifier(block)
                // This will make the next modifier share the same start time as this one.
                // If `then()` was used then the start time will be the end time of this modifier.
                ?.also { it.startTime = startTime }
                ?: throw IllegalStateException("Cannot obtain modifier without a target component.")
    }


    //region IModifier

    override fun getDuration(): Float {
        return endTime - startTime
    }

    override fun getSecondsElapsed(): Float {
        return elapsedSec
    }

    override fun isFinished(): Boolean {
        return elapsedSec >= endTime
    }

    override fun isRemoveWhenFinished(): Boolean {
        return true
    }

    override fun setRemoveWhenFinished(value: Boolean) {
        Log.w("GenericModifier", "GenericModifier always removes itself when finished.")
    }

    override fun addModifierListener(pModifierListener: IModifier.IModifierListener<IEntity>?) {
        throw UnsupportedOperationException("Modifier does not support modifier listeners.")
    }

    override fun removeModifierListener(pModifierListener: IModifier.IModifierListener<IEntity>?): Boolean {
        throw UnsupportedOperationException("Modifier does not support modifier listeners.")
    }

    override fun deepCopy(): IEntityModifier {
        return UIComponentModifier(pool).also {
            it.target = target
            it.startTime = startTime
            it.endTime = endTime
            it.onStart = onStart
            it.onUpdate = onUpdate
            it.onFinished = onFinished
            it.elapsedSec = elapsedSec
        }
    }

    //endregion


}
