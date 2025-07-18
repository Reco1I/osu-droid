package com.reco1l.andengine.modifier

/**
 * The type of the modifier. This is used to determine what kind of modification
 * the modifier will apply to the entity.
 */
enum class UIComponentModifierType {

    /**
     * Modifies the entity's X scale value.
     */
    ScaleX,

    /**
     * Modifies the entity's Y scale value.
     */
    ScaleY,

    /**
     * Modifies the entity's X and Y scale values.
     */
    ScaleXY,

    /**
     * Modifies the entity's alpha value.
     */
    Alpha,

    /**
     * Modifies the entity's color value.
     */
    Color,

    /**
     * Modifies the entity's X position.
     */
    MoveX,

    /**
     * Modifies the entity's Y position.
     */
    MoveY,

    /**
     * Modifies the entity's X and Y position.
     */
    MoveXY,

    /**
     * Modifies the entity's X translation.
     */
    TranslateX,

    /**
     * Modifies the entity's Y translation.
     */
    TranslateY,

    /**
     * Modifies the entity's X and Y translation.
     */
    TranslateXY,

    /**
     * Modifies the entity's rotation.
     */
    Rotation,

    /**
     * Modifies the entity's X size.
     */
    SizeX,

    /**
     * Modifies the entity's Y size.
     */
    SizeY,

    /**
     * Modifies the entity's X and Y size.
     */
    SizeXY,

    /**
     * Does nothing, used as a delay modifier.
     */
    Delay;

}