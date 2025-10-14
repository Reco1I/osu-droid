@file:Suppress("LeakingThis")

package com.reco1l.andengine.ui

import com.reco1l.andengine.*
import com.reco1l.andengine.component.*
import com.reco1l.andengine.container.*
import com.reco1l.andengine.shape.*
import com.reco1l.andengine.text.*
import com.reco1l.andengine.theme.FontSize
import com.reco1l.andengine.theme.Radius
import com.reco1l.andengine.theme.srem
import com.reco1l.andengine.ui.SizeVariant.*
import com.reco1l.framework.*
import com.reco1l.framework.math.*
import org.anddev.andengine.engine.camera.*
import org.anddev.andengine.opengl.font.Font
import ru.nsu.ccfit.zuev.osu.ResourceManager
import javax.microedition.khronos.opengles.*


/**
 * A badge is a small piece of information that can be used to display a value or a status.
 */
open class UIBadge : CompoundText(), ISizeVariable {

    override var style: UIComponent.(Theme) -> Unit = { theme ->
        color = theme.accentColor
        backgroundColor = theme.accentColor * 0.15f
    }

    override var sizeVariant = Medium
        set(value) {
            if (field != value) {
                field = value
                onSizeVariantChanged()
            }
        }

    init {
        background = UIBox()
        onSizeVariantChanged()
    }


    override fun onSizeVariantChanged() {
        when (sizeVariant) {
            Small -> {
                fontSize = FontSize.XS
                padding = Vec4(1.25f.srem, 0.75f.srem)
                spacing = 1.25f.srem
                backgroundRadius = Radius.MD
            }
            Medium -> {
                fontSize = FontSize.SM
                padding = Vec4(2f.srem, 1.25f.srem)
                spacing = 2f.srem
                backgroundRadius = Radius.LG
            }
            Large -> {
                fontSize = FontSize.MD
                padding = Vec4(2f.srem, 1.5f.srem)
                spacing = 2f.srem
                backgroundRadius = Radius.LG
            }
        }
    }
}

/**
 * A statistic badge is a badge that displays a value next to a label.
 */
open class UILabeledBadge : UILinearContainer(), ISizeVariable {

    override var style: UIComponent.(Theme) -> Unit = { theme ->
        color = theme.accentColor
        backgroundColor = theme.accentColor * 0.2f
    }

    override var sizeVariant = Medium
        set(value) {
            if (field != value) {
                field = value
                onSizeVariantChanged()
            }
        }


    /**
     * The entity of the badge's label.
     */
    val labelEntity = text {
        alignment = Anchor.Center
        background = UIBox().apply {
            color = Color4.Black
            alpha = 0.1f
        }
    }

    /**
     * The value of the badge.
     */
    val valueEntity = text {
        fontSize = FontSize.SM
        alignment = Anchor.Center
    }

    //region Shortcuts

    /**
     * The label of the badge.
     */
    var label by labelEntity::text

    /**
     * The value of the badge.
     */
    var value by valueEntity::text

    //endregion


    init {
        orientation = Orientation.Horizontal
        background = UIBox()

        onSizeVariantChanged()
    }


    override fun onSizeVariantChanged() {

        val fontSize: Float
        val padding: Vec4
        val cornerRadius: Float

        when (sizeVariant) {
            Small -> {
                fontSize = FontSize.XS
                padding = Vec4(1.25f.srem, 0.75f.srem)
                cornerRadius = Radius.MD
            }
            Medium -> {
                fontSize = FontSize.SM
                padding = Vec4(2f.srem, 1.25f.srem)
                cornerRadius = Radius.LG
            }
            Large -> {
                fontSize = FontSize.MD
                padding = Vec4(2f.srem, 1.5f.srem)
                cornerRadius = Radius.LG
            }
        }

        labelEntity.fontSize = fontSize
        valueEntity.fontSize = fontSize
        labelEntity.padding = padding
        valueEntity.padding = padding
        (background as? UIBox)?.cornerRadius = cornerRadius
    }

    override fun onManagedDraw(gl: GL10, camera: Camera) {
        (labelEntity.background as? UIBox)?.cornerRadius = (background as? UIBox)?.cornerRadius ?: 0f
        super.onManagedDraw(gl, camera)
    }
}



