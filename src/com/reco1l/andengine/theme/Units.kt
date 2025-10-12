package com.reco1l.andengine.theme

import com.reco1l.andengine.UIEngine


/**
 * Indicates that this float value is in "rem" units (root em).
 * It will be converted to pixels by multiplying it with the root font size.
 */
val Float.rem: Float
    get() = this * UIEngine.Companion.current.rootFontSize

/**
 * Spaced rem units. Based on Tailwind CSS spacing used for margin, padding, gap, etc:
 * * gap-1 = 0.25rem
 * * gap-2 = 0.5rem
 * * gap-3 = 0.75rem
 * * gap-4 = 1rem
 *
 * https://tailwindcss.com/docs/theme#default-theme-variable-reference
 */
val Float.srem: Float
    get() = this * 0.25f.rem

/**
 * Indicates that this float value is in percentage units.
 * It will be converted to a value between -4 and -3.
 */
val Float.per: Float
    get() =  -4f + (this / 100f).coerceAtLeast(-4f).coerceAtMost(-3f)