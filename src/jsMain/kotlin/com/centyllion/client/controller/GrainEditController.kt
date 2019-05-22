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
            colorController.data = new.color
            iconController.data = new.icon
            nameController.data = new.name
            descriptionController.data = new.description
            movementProbabilityController.data = "${new.movementProbability}"
            directionController.data = new.allowedDirection
            halfLifeController.data = "${new.halfLife}"
            onUpdate(old, new, this@GrainEditController)
        }
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            colorController.readOnly = new
            iconController.readOnly = new
            nameController.readOnly = new
            descriptionController.readOnly = new
            movementProbabilityController.readOnly = new
            directionController.readOnly = new
            halfLifeController.readOnly = new
            body.right = if (new) emptyList() else listOf(delete)
        }
    }

    val colorController = ColorSelectController(data.color) { _, new, _ ->
        data = data.copy(color = new)
    }

    val iconController = IconSelectController(data.icon) { _, new, _ ->
        data = data.copy(icon = new)
    }

    val nameController = EditableStringController(data.name, "Name") { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description") { _, new, _ ->
        data = data.copy(description = new)
    }

    val movementProbabilityController = editableDoubleController(data.movementProbability, "speed") { _, new, _ ->
        this.data = this.data.copy(movementProbability = new)
    }

    val directionController = DirectionSetEditController(data.allowedDirection) { _, new, _ ->
        this.data = this.data.copy(allowedDirection = new)
    }

    val halfLifeController = editableIntController(data.halfLife, "half life") { _, new, _ ->
        this.data = this.data.copy(halfLife = new)
    }

    val delete = Delete { onDelete(data, this@GrainEditController) }

    val body = Media(
        center = listOf(
            nameController,
            descriptionController,
            Level(center = listOf(colorController, iconController), mobile = true),
            HorizontalField(Help("Half life"), halfLifeController.container),
            HorizontalField(Help("Speed"), movementProbabilityController.container),
            HorizontalField(Help("Directions"), directionController.container)
        ),
        right = listOf(delete)
    ).apply {
        root.classList.add("is-outlined")
    }

    override val container = Column(body, size = ColumnSize.Full)

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        movementProbabilityController.refresh()
        directionController.refresh()
        halfLifeController.refresh()
    }

}
