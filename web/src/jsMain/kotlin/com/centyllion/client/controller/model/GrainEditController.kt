package com.centyllion.client.controller.model

import bulma.*
import bulma.extension.Slider
import bulma.extension.Switch
import com.centyllion.client.controller.utils.*
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.Field
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.grainIcon
import kotlin.properties.Delegates.observable
import bulma.Field as BField

class GrainEditController(
    initialData: Grain, model: GrainModel, val page: BulmaPage,
    var onUpdate: (old: Grain, new: Grain, controller: GrainEditController) -> Unit = { _, _, _ -> }
) : Controller<Grain, GrainModel, Div> {

    override var data: Grain by observable(initialData) { _, old, new ->
        if (old != new) {
            colorController.data = new.color
            iconController.data = new.icon
            nameController.data = new.name
            descriptionController.data = new.description
            speedController.data = "${new.movementProbability}"
            directionController.data = new.allowedDirection
            directionController.context = null to new.color
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
            invisibleCheckbox.disabled = new
            nameController.readOnly = new
            sizeSlider.disabled = new
            descriptionController.readOnly = new
            speedController.readOnly = new
            directionController.readOnly = new
            halfLifeController.readOnly = new
            fieldProductionsController.readOnly = new
            fieldInfluencesController.readOnly = new
            fieldPermeableController.readOnly = new
        }
    }

    val invisibleCheckbox = Switch(
        page.i18n("Invisible"), rounded = true, outlined = true,
        size = Size.Small, color = ElementColor.Info, checked = data.invisible
    ) { _, new -> data = data.copy(invisible = new) }

    val colorController = ColorSelectController(data.color) { _, new, _ ->
        data = data.copy(color = new)
    }

    val iconController = IconSelectController(data.icon) { _, new, _ ->
        data = data.copy(icon = new)
    }

    val nameController = EditableStringController(data.name, page.i18n("Name"), columns = 8) { _, new, _ ->
        data = data.copy(name = new)
    }

    val sizeSlider = Slider("${data.size}", "0", "5", "0.1", size = Size.Small, circle = true) { _, value ->
        data = data.copy(size = value.toDoubleOrNull() ?: 1.0)
    }

    val sizeValue = Value("${data.size}")

    val descriptionController = EditableStringController(data.description, page.i18n("Description"), columns = 60) { _, new, _ ->
        data = data.copy(description = new)
    }

    val halfLifeController = editableIntController(
        page.appContext.locale, data.halfLife, page.i18n("Half-life"), 0
    ) { _, new, _ -> data = data.copy(halfLife = new) }

    val speedController = editableDoubleController(
        page.appContext.locale, data.movementProbability, page.i18n("Speed"), 0.0, 1.0
    ) { _, new, _ -> data = data.copy(movementProbability = new) }

    val directionController = DirectionController(data.allowedDirection, null to data.color)
        { _, new, _ -> this.data = this.data.copy(allowedDirection = new) }

    val fieldProductionsController =
        columnsController(
            context.fields.map { it.id to (data.fieldProductions[it.id] ?: 0f) }, context.fields
        ) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldProduction(new.first, new.second)

                    // revalidate the corresponding permeability
                    fieldPermeableController.dataControllers.find { it.data.first == pair.first }?.validate()
                }
            }
        }

    val fieldInfluencesController =
        columnsController(
            context.fields.map { it.id to (data.fieldInfluences[it.id] ?: 0f) }, context.fields
        ) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields) { old, new, _ ->
                if (old != new) {
                    this.data = data.updateFieldInfluence(new.first, new.second)
                }
            }
        }

    val fieldPermeableController:
            MultipleController<Pair<Int, Float>, List<Field>, Columns, Column, FieldChangeController
    > = columnsController(
            context.fields.map { it.id to (data.fieldPermeable[it.id] ?: 1f) }, context.fields
        ) { pair, previous ->
            previous ?: FieldChangeController(pair, context.fields, 0f, 1f,
            { id, value ->
                if ((data.fieldProductions[id]?: 0f) != 0f && value <= 0f)
                    page.i18n("Field permeability will prevent production for %0", context.fieldForId(id)?.name ?: "")
                else null
            },
            { old, new, _ ->
                if (old != new)  this.data = data.updateFieldPermeable(new.first, new.second)
            })
        }


    val fieldSeparator = HtmlWrapper(createHr()).apply {
        hidden = context.fields.isEmpty()
    }

    val fieldControls = Columns(
        Column(
            Label(page.i18n("Productions")),
            fieldProductionsController,
            size = ColumnSize.Full
        ),
        Column(
            Label(page.i18n("Influences")),
            fieldInfluencesController,
            size = ColumnSize.Full
        ),
        Column(
            Label(page.i18n("Permeability")), fieldPermeableController,
            size = ColumnSize.Full
        ),
        multiline = true
    ).apply { hidden = context.fields.isEmpty() }

    override val container = editorBox(page.i18n("Grain"), grainIcon,
        HorizontalField(
            Label(page.i18n("Name")),
            nameController.container
        ),
        HorizontalField(
            Label(page.i18n("Display")),
            BField(
                Control(iconController.container),
                Control(colorController.container),
                addons = true
            ),
            BField(
                Control(Label(page.i18n("Size"))),
                Control(sizeSlider),
                Control(sizeValue),
                grouped = true
            ),
            BField(invisibleCheckbox)
        ),
        HorizontalField(Label(page.i18n("Description")), descriptionController.container),
        HorizontalField(Label(page.i18n("Half-life")), halfLifeController.container),
        HorizontalField(Label(page.i18n("Movement")),
            BField(Control(Value(page.i18n("Speed"))), speedController.container, grouped = true),
            BField(Control(directionController)),
        ),
        fieldSeparator,
        fieldControls
    )

    override fun refresh() {
        colorController.refresh()
        nameController.refresh()
        descriptionController.refresh()
        halfLifeController.refresh()
        speedController.refresh()
        directionController.refresh()
        fieldProductionsController.refresh()
        fieldInfluencesController.refresh()
        fieldPermeableController.refresh()

        sizeSlider.value = "${data.size}"
        sizeValue.text = "${data.size}"

        fieldSeparator.hidden = context.fields.isEmpty()
        fieldControls.hidden = context.fields.isEmpty()
    }

}
