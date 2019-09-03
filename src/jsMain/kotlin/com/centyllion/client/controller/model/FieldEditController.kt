package com.centyllion.client.controller.model

import bulma.Box
import bulma.Control
import bulma.Div
import bulma.ElementColor
import bulma.Help
import bulma.HorizontalField
import bulma.Label
import bulma.NoContextController
import bulma.Size
import bulma.Tag
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableFloatController
import com.centyllion.client.controller.utils.editableIntController
import com.centyllion.model.Field
import com.centyllion.model.extendedDirections
import com.centyllion.model.firstDirections
import kotlin.properties.Delegates.observable
import bulma.Field as BField

class FieldEditController (
    initialData: Field,
    var onUpdate: (old: Field, new: Field, controller: FieldEditController) -> Unit = { _, _, _ -> }
) : NoContextController<Field, Div>() {

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
        }
    }

    val colorController = ColorSelectController(data.color) { _, new, _ ->
        data = data.copy(color = new)
    }

    val nameController = EditableStringController(data.name, "Name", columns = 8) { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description", columns = 40) { _, new, _ ->
        data = data.copy(description = new)
    }

    val speedController = editableFloatController(data.speed, "speed", 0f, 1f) { _, new, _ ->
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

    val halfLifeController = editableIntController(data.halfLife, "half life", 0) { _, new, _ ->
        this.data = this.data.copy(halfLife = new)
    }

    override val container = Div(
            Tag("Field", ElementColor.Primary, Size.Large),
            Box(
            HorizontalField(
                Label("Display"),
                nameController.container,
                bulma.Field(
                    Control(colorController.container),
                    addons = true
                )
            ),
            HorizontalField(Label("Description"), descriptionController.container),
            HorizontalField(Label("Half-life"), halfLifeController.container),
            HorizontalField(Label("Movement"),
                BField(Control(Help("Speed")), speedController.container, grouped = true),
                firstDirectionController.container,
                extendedDirectionController.container
            )
        )
    )

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
