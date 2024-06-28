package com.reco1l.framework.andengine

import org.anddev.andengine.entity.IEntity
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener
import org.anddev.andengine.util.modifier.IModifier

open class EntityModifierListener : IEntityModifierListener {

    override fun onModifierStarted(modifier: IModifier<IEntity>, item: IEntity) = Unit

    override fun onModifierFinished(modifier: IModifier<IEntity>, item: IEntity) = Unit
}
