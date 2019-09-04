package com.centyllion.client.controller.model

import bulma.Box
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Controller
import bulma.Div
import bulma.ElementColor
import bulma.Help
import bulma.HorizontalField
import bulma.Label
import bulma.Size
import bulma.Slider
import bulma.Tag
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
import bulma.Field as BField

class GrainEditController(
    initialData: Grain, model: GrainModel,
    var onUpdate: (old: Grain, new: Grain, controller: GrainEditController) -> Unit = { _, _, _ -> }
) : Controller<Grain, GrainModel, Div> {

    override var data: Grain by observable(initialData) { _, old, new ->
        if (old != new) {
            colorController.data = new.color
            iconController.data = new.icon
            nameController.data = new.name
            descriptionController.data = new.description
            speedController.data = "${new.movementProbability}"
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
            speedController.readOnly = new
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

    val descriptionController = EditableStringController(data.description, "Description", columns = 60) { _, new, _ ->
        data = data.copy(description = new)
    }

    val halfLifeController = editableIntController(data.halfLife, "half life", 0) { _, new, _ ->
        data = data.copy(halfLife = new)
    }

    val speedController = editableDoubleController(data.movementProbability, "speed", 0.0, 1.0) { _, new, _ ->
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
        columnsController(
            context.fields.map { it.id to (data.fieldProductions[it.id] ?: 0f) },
            context.fields
        ) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldProduction(new.first, new.second)
                }
            }
        }

    val fieldInfluencesController =
        columnsController(
            context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) },
            context.fields
        ) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldInfluence(new.first, new.second)
                }
            }
        }

    val fieldPermeableController =
        columnsController(
            context.fields.map { it.id to (data.fieldPermeable[it.id] ?: 1f) },
            context.fields
        ) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields, 0f, 1f) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldPermeable(new.first, new.second)
                }
            }
        }

    val fieldControls = Columns(
        Column(
            Label("Productions"), fieldProductionsController,
            desktopSize = ColumnSize.OneThird, tabletSize = ColumnSize.Half
        ),
        Column(
            Label("Influences"), fieldInfluencesController,
            desktopSize = ColumnSize.OneThird, tabletSize = ColumnSize.Half
        ),
        Column(
            Label("Permeability"), fieldPermeableController,
            desktopSize = ColumnSize.OneThird, tabletSize = ColumnSize.Half
        ),
        multiline = true
    ).apply { hidden = context.fields.isEmpty() }

    override val container = Div(
        Tag("Grain", ElementColor.Primary, Size.Large),
        Box(
            HorizontalField(
                Label("Name"),
                nameController.container,
                BField(
                    Control(iconController.container),
                    Control(colorController.container),
                    addons = true
                ),
                BField(
                    Control(Label("Size")),
                    Control(sizeSlider),
                    Control(sizeValue),
                    grouped = true
                )
            ),
            HorizontalField(Label("Description"), descriptionController.container),
            HorizontalField(Label("Half-life"), halfLifeController.container),
            HorizontalField(Label("Movement"),
                BField(Control(Help("Speed")), speedController.container, grouped = true),
                firstDirectionController.container,
                extendedDirectionController.container
            ),
            fieldControls
        )
    )

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        halfLifeController.refresh()
        speedController.refresh()
        firstDirectionController.refresh()
        extendedDirectionController.refresh()
        fieldProductionsController.refresh()
        fieldInfluencesController.refresh()
        fieldPermeableController.refresh()

        sizeSlider.value = "${data.size}"
        sizeValue.text = "${data.size}"

        fieldControls.hidden = context.fields.isEmpty()
    }

}
