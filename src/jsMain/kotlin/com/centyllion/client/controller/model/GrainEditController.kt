package com.centyllion.client.controller.model

import bulma.Delete
import bulma.Help
import bulma.HorizontalField
import bulma.Level
import bulma.Media
import bulma.NoContextController
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableDoubleController
import com.centyllion.client.controller.utils.editableIntController
import com.centyllion.model.Grain
import com.centyllion.model.extendedDirections
import com.centyllion.model.firstDirections
import kotlin.properties.Delegates.observable

class GrainEditController(
    initialData: Grain,
    var onUpdate: (old: Grain, new: Grain, controller: GrainEditController) -> Unit = { _, _, _ -> },
    var onDelete: (deleted: Grain, controller: GrainEditController) -> Unit = { _, _ -> }
) : NoContextController<Grain, Media>() {

    override var data: Grain by observable(initialData) { _, old, new ->
        if (old != new) {
            colorController.data = new.color
            iconController.data = new.icon
            nameController.data = new.name
            descriptionController.data = new.description
            movementProbabilityController.data = "${new.movementProbability}"
            firstDirectionController.data = new.allowedDirection
            extendedDirectionController.data = new.allowedDirection
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
            firstDirectionController.readOnly = new
            extendedDirectionController.readOnly = new
            halfLifeController.readOnly = new
            container.right = if (new) emptyList() else listOf(delete)
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

    val firstDirectionController: DirectionSetEditController =
        DirectionSetEditController(firstDirections, data.allowedDirection) { _, new, _ ->
            this.data = this.data.copy(allowedDirection = new)
        }

    val extendedDirectionController: DirectionSetEditController =
        DirectionSetEditController(extendedDirections, data.allowedDirection) { _, new, _ ->
            this.data = this.data.copy(allowedDirection = new)
        }

    val halfLifeController = editableIntController(data.halfLife, "half life") { _, new, _ ->
        this.data = this.data.copy(halfLife = new)
    }

    val delete = Delete { onDelete(data, this@GrainEditController) }

    override val container = Media(
        center = listOf(
            nameController,
            descriptionController,
            Level(center = listOf(colorController, iconController), mobile = true),
            HorizontalField(Help("Half life"), halfLifeController.container),
            HorizontalField(Help("Speed"), movementProbabilityController.container),
            Level(center = listOf(firstDirectionController, extendedDirectionController))
        ),
        right = listOf(delete)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        movementProbabilityController.refresh()
        firstDirectionController.refresh()
        extendedDirectionController.refresh()
        halfLifeController.refresh()
    }

}
