package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.GrainModel
import com.centyllion.model.Reaction
import kotlin.properties.Delegates.observable

class ReactionEditController(
    reaction: Reaction, val model: GrainModel,
    var onUpdate: (old: Reaction, new: Reaction, controller: ReactionEditController) -> Unit = { _, _, _ -> },
    var onDelete: (Reaction, controller: ReactionEditController) -> Unit = { _, _ ->}
): Controller<Reaction, Column> {

    override var data: Reaction by observable(reaction) { _, old, new ->
        if (old != new) {
            reactiveController.data = model.indexedGrains[data.reactiveId]
            productController.data = model.indexedGrains[data.productId]
            transform.checked = data.transform
            onUpdate(old, new, this@ReactionEditController)
            refresh()
        }
    }

    val reactiveController = GrainSelectController(model.indexedGrains[data.reactiveId], model.grains) { _, new, _ ->
        this.data = this.data.copy(reactiveId = new?.id ?: -1)
    }

    val productController = GrainSelectController(model.indexedGrains[data.productId], model.grains) { _, new, _ ->
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
                Column(HorizontalField(Help("Reactive"), reactiveController.container), size = ColumnSize.S5),
                Column(HorizontalField(Help("Product"), productController.container), size = ColumnSize.S5),
                Column(Field(Control(transform)), size = ColumnSize.S2),
                multiline = true
            )
        ),
        right = listOf(delete)
    ))

    override fun refresh() {
    }

}
