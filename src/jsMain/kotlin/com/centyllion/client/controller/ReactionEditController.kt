package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import kotlin.properties.Delegates.observable

class ReactionEditController(
    reaction: Reaction, behaviour: Behaviour, model: GrainModel,
    var onUpdate: (old: Reaction, new: Reaction, controller: ReactionEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Reaction, controller: ReactionEditController) -> Unit = { _, _ -> }
) : Controller<Reaction, Pair<Behaviour, GrainModel>, Column> {

    override var data: Reaction by observable(reaction) { _, old, new ->
        if (old != new) {
            reactiveController.data = context.second.indexedGrains[data.reactiveId]
            directionController.data = data.allowedDirection
            productController.data = context.second.indexedGrains[data.productId]
            sourceReactiveController.data = data.sourceReactive
            onUpdate(old, new, this@ReactionEditController)
        }
        refresh()
    }

    override var context: Pair<Behaviour, GrainModel> by observable(behaviour to model)
    { _, old, new ->
        if (old.second != new.second) {
            reactiveController.data = context.second.indexedGrains[data.reactiveId]
            reactiveController.context = new.second.grains
            productController.data = context.second.indexedGrains[data.productId]
            productController.context = new.second.grains
            refresh()
        }
        if (old != new) {
            sourceReactiveController.context = context
        }
    }

    val reactiveController = GrainSelectController(context.second.indexedGrains[data.reactiveId], context.second.grains)
    { _, new, _ ->
        this.data = this.data.copy(reactiveId = new?.id ?: -1)
    }

    val directionController = DirectionSetEditController(data.allowedDirection)
    { _, new, _ ->
        this.data = this.data.copy(allowedDirection = new)
    }

    val productController = GrainSelectController(context.second.indexedGrains[data.productId], context.second.grains)
    { _, new, _ ->
        this.data = this.data.copy(productId = new?.id ?: -1)
    }

    val sourceReactiveController = SourceReactiveSelectController(data.sourceReactive, context.first, context.second)
    { _, new, _ ->
        this.data = this.data.copy(sourceReactive = new)
    }

    val delete = iconButton(Icon("times", Size.Small), ElementColor.Danger, true, size = Size.Small)
    {
        onDelete(this.data, this@ReactionEditController)
    }

    override val container = Column(
        Level(
            left = listOf(reactiveController),
            center = listOf(directionController, productController, sourceReactiveController),
            right = listOf(delete),
            mobile = true
        ),
        size = ColumnSize.Full
    )

    override fun refresh() {
        reactiveController.refresh()
        directionController.refresh()
        productController.refresh()
    }

}
