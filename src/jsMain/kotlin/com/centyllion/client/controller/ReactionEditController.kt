package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import kotlin.properties.Delegates.observable

class ReactionEditController(
    reaction: Reaction, model: GrainModel,
    var onUpdate: (old: Reaction, new: Reaction, controller: ReactionEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Reaction, controller: ReactionEditController) -> Unit = { _, _ -> }
) : Controller<Reaction, GrainModel, Column> {

    override var data: Reaction by observable(reaction) { _, old, new ->
        if (old != new) {
            reactiveController.data = context.indexedGrains[data.reactiveId]
            directionController.data = data.allowedDirection
            productController.data = context.indexedGrains[data.productId]
            // TODO adds source reaction controller
            onUpdate(old, new, this@ReactionEditController)
        }
        refresh()
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            reactiveController.data = context.indexedGrains[data.reactiveId]
            reactiveController.context = new.grains
            productController.data = context.indexedGrains[data.productId]
            productController.context = new.grains
            refresh()
        }
    }

    val reactiveController = GrainSelectController(context.indexedGrains[data.reactiveId], context.grains) { _, new, _ ->
        this.data = this.data.copy(reactiveId = new?.id ?: -1)
    }

    val directionController = DirectionSetEditController(data.allowedDirection) { _, new, _ ->
        this.data = this.data.copy(allowedDirection = new)
    }

    val productController = GrainSelectController(context.indexedGrains[data.productId], context.grains) { _, new, _ ->
        this.data = this.data.copy(productId = new?.id ?: -1)
    }

    val transform = Checkbox("Transform", this.data.sourceReactive == 0) { _, value ->
        this.data = this.data.copy(sourceReactive = if (value) 0 else -1)
    }

    val delete = iconButton(Icon("times", Size.Small), ElementColor.Danger, true, size = Size.Small) {
        onDelete(this.data, this@ReactionEditController)
    }

    override val container = Column(
        Level(
            left = listOf(
                HorizontalField(Help("Reactive"), reactiveController.container)
            ),
            center = listOf(
                directionController
            ),
            right = listOf(
                HorizontalField(Help("Product"), productController.container),
                Control(transform),
                delete
            )
        ),
        size = ColumnSize.Full
    )

    override fun refresh() {
        reactiveController.refresh()
        directionController.refresh()
        productController.refresh()
    }

}
