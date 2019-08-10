package com.centyllion.client.controller.model

import bulma.Box
import bulma.Controller
import bulma.Div
import bulma.Help
import bulma.Level
import bulma.Size
import bulma.Slider
import bulma.TileAncestor
import bulma.TileChild
import bulma.TileParent
import bulma.Value
import bulma.columnsController
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
    var onUpdate: (old: Grain, new: Grain, controller: GrainEditController) -> Unit = { _, _, _ -> }
) : Controller<Grain, GrainModel, Box> {

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
            fieldProductionsController.data = context.fields.map { it.id to (data.fieldProductions[it.id] ?: 0f) }
            fieldInfluencesController.data = context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) }
            fieldPermeableController.data = context.fields.map { it.id to (data.fieldPermeable[it.id] ?: 1f) }
            onUpdate(old, new, this@GrainEditController)
        }
        refresh()
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            fieldProductionsController.data = context.fields.map { it.id to (data.fieldProductions[it.id] ?: 0f) }
            fieldInfluencesController.data = context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) }
            fieldPermeableController.data = context.fields.map { it.id to (data.fieldPermeable[it.id] ?: 1f) }
            fieldProductionsController.context = new.fields
            fieldInfluencesController.context = new.fields
            fieldPermeableController.context = new.fields
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            colorController.readOnly = new
            iconController.readOnly = new
            nameController.readOnly = new
            sizeSlider.disabled = new
            descriptionController.readOnly = new
            movementProbabilityController.readOnly = new
            firstDirectionController.readOnly = new
            extendedDirectionController.readOnly = new
            halfLifeController.readOnly = new
            fieldProductionsController.readOnly = new
        }
    }

    val colorController = ColorSelectController(data.color) { _, new, _ ->
        data = data.copy(color = new)
    }

    val iconController = IconSelectController(data.icon) { _, new, _ ->
        data = data.copy(icon = new)
    }

    val nameController = EditableStringController(data.name, "Name", columns = 8) { _, new, _ ->
        data = data.copy(name = new)
    }

    val sizeSlider = Slider("${data.size}", "0", "5", "0.1", size = Size.Small) { _, value ->
        data = data.copy(size = value.toDoubleOrNull() ?: 1.0)
    }

    val sizeValue = Value("${data.size}")

    val descriptionController = EditableStringController(data.description, "Description") { _, new, _ ->
        data = data.copy(description = new)
    }

    val halfLifeController = editableIntController(data.halfLife, "half life", 0) { _, new, _ ->
        data = data.copy(halfLife = new)
    }

    val movementProbabilityController = editableDoubleController(data.movementProbability, "speed", 0.0, 1.0) { _, new, _ ->
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

    val fieldProductionsController =
        columnsController(context.fields.map { it.id to (data.fieldProductions[it.id] ?: 0f) }, context.fields) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldProduction(new.first, new.second)
                }
            }
        }

    val fieldInfluencesController =
        columnsController(context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) }, context.fields) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldInfluence(new.first, new.second)
                }
            }
        }

    val fieldPermeableController =
        columnsController(context.fields.map { it.id to (data.fieldPermeable[it.id] ?: 1f) }, context.fields) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields, 0f, 1f) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldPermeable(new.first, new.second)
                }
            }
        }

    val fieldsConfiguration = Div(
        Help("Productions"),
        fieldProductionsController,
        Help("Influences"),
        fieldInfluencesController,
        Help("Permeability"),
        fieldPermeableController
    )

    override val container = Box(
        Level(
            left = listOf(colorController, iconController),
            mobile = true
        ),
        Level(
            left = listOf(nameController),
            center = listOf(Help("Size"), sizeSlider, sizeValue),
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
        fieldsConfiguration
    ).apply { root.classList.add("is-outlined") }

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        halfLifeController.refresh()
        movementProbabilityController.refresh()
        firstDirectionController.refresh()
        extendedDirectionController.refresh()
        fieldProductionsController.refresh()
        fieldInfluencesController.refresh()
        fieldPermeableController.refresh()

        sizeSlider.value = "${data.size}"
        sizeValue.text = "${data.size}"

        fieldsConfiguration.hidden = context.fields.isEmpty()
    }

}
