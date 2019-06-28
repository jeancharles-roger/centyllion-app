package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Controller
import bulma.Delete
import bulma.ElementColor
import bulma.Help
import bulma.Icon
import bulma.Level
import bulma.Media
import bulma.Size
import bulma.TileAncestor
import bulma.TileChild
import bulma.TileParent
import bulma.columnsController
import bulma.iconButton
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.editableDoubleController
import com.centyllion.client.controller.utils.editableIntController
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.extendedDirections
import com.centyllion.model.firstDirections
import kotlin.properties.Delegates.observable

class GrainEditController(
    initialData: Grain, model: GrainModel,
    var onUpdate: (old: Grain, new: Grain, controller: GrainEditController) -> Unit = { _, _, _ -> },
    var onDelete: (deleted: Grain, controller: GrainEditController) -> Unit = { _, _ -> }
) : Controller<Grain, GrainModel, Media> {

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
            fieldsController.data = new.fields.toList()
            onUpdate(old, new, this@GrainEditController)
        }
        refresh()
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            // TODO
            fieldsController.context = new.fields
            refresh()
        }
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
            fieldsController.readOnly = new
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

    val halfLifeController = editableIntController(data.halfLife, "half life") { _, new, _ ->
        data = data.copy(halfLife = new)
    }

    val movementProbabilityController = editableDoubleController(data.movementProbability, "speed") { _, new, _ ->
        data = data.copy(movementProbability = new)
    }

    val firstDirectionController: DirectionSetEditController =
        DirectionSetEditController(firstDirections, data.allowedDirection) { _, new, _ ->
            this.data = this.data.copy(allowedDirection = new)
        }

    val extendedDirectionController: DirectionSetEditController =
        DirectionSetEditController(extendedDirections, data.allowedDirection) { _, new, _ ->
            this.data = this.data.copy(allowedDirection = new)
        }

    val addFieldButton = iconButton(
        Icon("plus", Size.Small), rounded = true, color = ElementColor.Info,
        size = Size.Small, disabled = data.fields.size >= context.fields.size
    ) {
        this.data = data.copy(fields = data.fields + (context.fields.first().id to 50f) )
    }

    val fieldsController =
        columnsController(data.fields.toList(), context.fields) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateField(new.first, new.second)
                }
            }
        }

    val delete = Delete { onDelete(data, this@GrainEditController) }

    override val container = Media(
        center = listOf(
            Columns(
                Column(colorController, size = ColumnSize.S3),
                Column(iconController, size = ColumnSize.S3),
                Column(nameController, size = ColumnSize.S6),
                mobile = true
            ),
            descriptionController,
            TileAncestor(
                TileParent(
                    TileChild(Help("Half life")),
                    TileChild(halfLifeController),
                    vertical = true
                ),
                TileParent(
                    TileChild(Help("Speed")),
                    TileChild(movementProbabilityController),
                    vertical = true
                )
            ),
            Level(center = listOf(firstDirectionController, extendedDirectionController)),
            TileAncestor(
                TileParent(
                    TileChild(
                        Level(
                            left = listOf(Help("Fields")),
                            right = listOf(addFieldButton)
                        )
                    ),
                    TileChild(fieldsController),
                    vertical = true
                )
            )

        ),
        right = listOf(delete)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        halfLifeController.refresh()
        movementProbabilityController.refresh()
        firstDirectionController.refresh()
        extendedDirectionController.refresh()

        addFieldButton.disabled = data.fields.size >= context.fields.size
    }

}
