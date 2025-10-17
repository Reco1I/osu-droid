@file:Suppress("MemberVisibilityCanBePrivate")

package com.reco1l.andengine.container

import com.reco1l.andengine.component.*
import com.reco1l.andengine.theme.Size
import kotlin.math.max

open class UIFillContainer : UILinearContainer() {

    override fun onContentChanged() {

        // This will not have any free space to distribute.
        if (orientation == Orientation.Horizontal && rawWidth == Size.Auto
            || orientation == Orientation.Vertical && rawHeight == Size.Auto) {
            super.onContentChanged()
            return
        }

        var totalWidth = 0f
        var totalHeight = 0f
        var totalWeight = 0f

        var visibleCount = 0

        // First pass - calculate total weight and natural sizes (content + padding)
        forEach { child ->

            if (child !is UIComponent || !child.isVisible) {
                return@forEach
            }
            visibleCount++

            totalWidth += child.intrinsicWidth
            totalHeight += child.intrinsicHeight

            totalWeight += child.weight
        }

        // Nothing to distribute.
        if (totalWeight == 0f) {
            super.onContentChanged()
            return
        }

        val totalSpacing = spacing * (visibleCount - 1)
        val freeSpace = width - totalWidth - totalSpacing

        // Second pass - place items, distribute remaining space according to weights
        var contentWidth = 0f
        var contentHeight = 0f

        mChildren?.forEachIndexed { index, child ->

            if (child !is UIComponent || !child.isVisible) {
                return@forEachIndexed
            }

            val weightPortion = child.weight / totalWeight
            val extraSpace = freeSpace * weightPortion
            val spacing = (if (index < visibleCount - 1) spacing else 0f)

            when (orientation) {

                Orientation.Horizontal -> {
                    child.width = child.intrinsicWidth + extraSpace
                    child.x = contentWidth

                    contentWidth += child.width + spacing
                    contentHeight = max(contentHeight, child.height)
                }

                Orientation.Vertical -> {
                    child.height = child.intrinsicHeight + extraSpace
                    child.y = contentWidth

                    contentWidth += child.height + spacing
                    contentHeight = max(contentHeight, child.width)
                }
            }
        }

        this.contentWidth = contentWidth
        this.contentHeight = contentHeight
    }

}