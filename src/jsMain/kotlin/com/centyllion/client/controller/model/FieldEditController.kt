package com.centyllion.client.controller.model

import bulma.Delete
import bulma.Help
import bulma.HorizontalField
import bulma.Level
import bulma.Media
import bulma.NoContextController
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableFloatController
import com.centyllion.client.controller.utils.editableIntController
import com.centyllion.model.Field
import com.centyllion.model.extendedDirections
import com.centyllion.model.firstDirections
import kotlin.properties.Delegates.observable

class FieldEditController(
    initialData: Field,
    var onUpdate: (old: Field, new: Field, controller: FieldEditController) -> Unit = { _, _, _ -> },
    var onDelete: (deleted: Field, controller: FieldEditController) -> Unit = { _, _ -> }
) : NoContextController<Field, Media>() {

    override var data: Field by observable(initialData) { _, old, new ->
        if (old != new) {
            colorController.data = new.color
            nameController.data = new.name
            descriptionController.data = new.description
            speedController.data = "${new.speed}"
            firstDirectionController.data = new.allowedDirection
            extendedDirectionController.data = new.allowedDirection
            halfLifeController.data = "${new.halfLife}"
            onUpdate(old, new, this@FieldEditController)
        }
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            colorController.readOnly = new
            nameController.readOnly = new
            descriptionController.readOnly = new
            speedController.readOnly = new
            firstDirectionController.readOnly = new
            extendedDirectionController.readOnly = new
            halfLifeController.readOnly = new
            container.right = if (new) emptyList() else listOf(delete)
        }
    }

    val colorController = ColorSelectController(data.color) { _, new, _ ->
        data = data.copy(color = new)
    }

    val nameController = EditableStringController(data.name, "Name") { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description") { _, new, _ ->
        data = data.copy(description = new)
    }

    val speedController = editableFloatController(data.speed, "speed") { _, new, _ ->
        this.data = this.data.copy(speed = new)
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

    val delete = Delete { onDelete(data, this@FieldEditController) }

    override val container = Media(
        center = listOf(
            nameController,
            descriptionController,
            Level(center = listOf(colorController), mobile = true),
            HorizontalField(Help("Half life"), halfLifeController.container),
            HorizontalField(Help("Speed"), speedController.container),
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
        speedController.refresh()
        firstDirectionController.refresh()
        extendedDirectionController.refresh()
        halfLifeController.refresh()
    }

}
