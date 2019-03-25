package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import kotlin.properties.Delegates.observable

class ReactionEditController(
    reaction: Reaction, model: GrainModel,
    var onUpdate: (old: Reaction, new: Reaction, controller: ReactionEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Reaction, controller: ReactionEditController) -> Unit = { _, _ ->}
): Controller<Reaction, GrainModel, Column> {

    override var data: Reaction by observable(reaction) { _, old, new ->
        if (old != new) {
            reactiveController.data = context.indexedGrains[data.reactiveId]
            directionController.data = data.allowedDirection
            productController.data = context.indexedGrains[data.productId]
            transform.checked = data.transform
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

    val transform = Checkbox("transform", data.transform) { _, value ->
        this.data = this.data.copy(transform = value)
    }

    val delete = Delete { onDelete(this.data, this@ReactionEditController) }

    override val container = Column(Media(
        center = listOf(
            Columns(
                // first line
                Column(HorizontalField(Help("Reactive"), reactiveController.container), size = ColumnSize.S4),
                Column(directionController, size = ColumnSize.S2),
                Column(HorizontalField(Help("Product"), productController.container), size = ColumnSize.S4),
                Column(Field(Control(transform)), size = ColumnSize.S2),
                multiline = true
            )
        ),
        right = listOf(delete)
    ), size = ColumnSize.Full)

    override fun refresh() {
        reactiveController.refresh()
        directionController.refresh()
        productController.refresh()
    }

}
