package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Grain
import kotlin.properties.Delegates.observable

class GrainEditController(
    initialData: Grain,
    var onUpdate: (old: Grain, new: Grain, controller: GrainEditController) -> Unit = { _, _, _ -> },
    var onDelete: (deleted: Grain, controller: GrainEditController) -> Unit = { _, _ -> }
) : NoContextController<Grain, Column>() {

    override var data: Grain by observable(initialData) { _, old, new ->
        if (old != new) {
            nameController.data = new.name
            descriptionController.data = new.description
            movementProbabilityController.data = "${new.movementProbability}"
            directionController.data = new.allowedDirection
            onUpdate(old, new, this@GrainEditController)
        }
        refresh()
    }

    val dot = span(classes = "dot")

    val nameController = EditableStringController(data.name, "Name") { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description") { _, new, _ ->
        data = data.copy(description = new)
    }

    val movementProbabilityController = editableDoubleController(data.movementProbability, "p") { _, new, _ ->
        this.data = this.data.copy(movementProbability = new)
    }

    val directionController = DirectionSetEditController(data.allowedDirection) { _, new, _ ->
        this.data = this.data.copy(allowedDirection = new)
    }

    val delete = Delete { onDelete(data, this@GrainEditController) }

    override val container = Column(
        Media(
            left = listOf(dot),
            center = listOf(
                nameController,
                descriptionController,
                HorizontalField(Help("Probability"), movementProbabilityController.container),
                HorizontalField(Help("Directions"), directionController.container)
            ),
            right = listOf(delete)
        ), size = ColumnSize.Full
    )

    init {
        refresh()
    }

    override fun refresh() {
        dot.root.style.backgroundColor = data.color
        nameController.refresh()
        descriptionController.refresh()
        movementProbabilityController.refresh()
        directionController.refresh()
    }

}
