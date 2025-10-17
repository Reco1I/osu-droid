package com.reco1l.andengine.component

import androidx.annotation.*

@Suppress("ConstPropertyName")
@IntDef(value = [
    InvalidationFlag.Size,
    InvalidationFlag.Position,
    InvalidationFlag.Content,
    InvalidationFlag.Transformations,
    InvalidationFlag.InputBindings
])
annotation class InvalidationFlag {
    companion object {

        /**
         * The size of the entity has changed. Calls [UIComponent.onSizeChanged].
         */
        const val Size = 1

        /**
         * The content of the entity has changed. Calls [UIComponent.onContentChanged].
         */
        const val Content = 1 shl 1

        /**
         * The position of the entity has changed. Calls [UIComponent.onPositionChanged].
         */
        const val Position = 1 shl 2

        /**
         * The transformations of the entity have changed. Calls [UIComponent.onInvalidateTransformations].
         */
        const val Transformations = 1 shl 3

        /**
         * The input bindings were removed. Calls [UIComponent.onInvalidateInputBindings].
         */
        const val InputBindings = 1 shl 4

        /**
         * All invalidation flags.
         */
        const val All = Size or Position or Content or Transformations or InputBindings


        fun toString(flag: Int): String {
            val names = mutableListOf<String>()
            if (flag and Size != 0) {
                names.add("Size")
            }
            if (flag and Position != 0) {
                names.add("Position")
            }
            if (flag and Content != 0) {
                names.add("Content")
            }
            if (flag and Transformations != 0) {
                names.add("Transformations")
            }
            if (flag and InputBindings != 0) {
                names.add("InputBindings")
            }
            if (names.isEmpty()) {
                return "None"
            }
            return names.joinToString(separator = " | ")
        }
    }
}