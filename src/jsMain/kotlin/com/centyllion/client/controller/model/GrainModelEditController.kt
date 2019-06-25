package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.ElementColor
import bulma.Icon
import bulma.Level
import bulma.NoContextController
import bulma.TextSize
import bulma.Title
import bulma.columnsController
import bulma.iconButton
import bulma.noContextColumnsController
import bulma.wrap
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class GrainModelEditController(
    model: GrainModel,
    val onUpdate: (old: GrainModel, new: GrainModel, controller: GrainModelEditController) -> Unit =
        { _, _, _ -> }
) : NoContextController<GrainModel, Columns>() {

    override var data: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            fieldsController.data = data.fields
            grainsController.data = data.grains
            behavioursController.data = data.behaviours
            behavioursController.context = data
            onUpdate(old, new, this@GrainModelEditController)
            refresh()
        }
    }

    override var readOnly by observable(false) { _, old, new ->
        if (old != new) {
            addFieldButton.invisible = new
            addGrainButton.invisible = new
            addBehaviourButton.invisible = new
            fieldsController.readOnly = new
            grainsController.readOnly = new
            behavioursController.readOnly = new
        }
    }

    val addFieldButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(fields = data.fields + data.newField())
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(grains = data.grains + data.newGrain())
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(behaviours = data.behaviours + Behaviour())
    }

    val fieldsController =
        noContextColumnsController(data.fields) { parent, field, previous ->
            previous ?: FieldEditController(field).wrap { controller ->
                controller.onUpdate = { old, new, _ ->
                    val fields = data.fields.toMutableList()
                    fields[parent.indexOf(controller)] = new
                    data = data.copy(fields = fields)
                }
                controller.onDelete = { field, _ ->
                    data = data.dropField(parent.indexOf(controller))
                }
                Column(controller.container, size = ColumnSize.OneQuarter)
            }
        }

    val grainsController =
        noContextColumnsController(data.grains) { parent, grain, previous ->
            previous ?: GrainEditController(grain).wrap { controller ->
                controller.onUpdate = { _, new, _ ->
                    val newGrains = data.grains.toMutableList()
                    newGrains[parent.indexOf(controller)] = new
                    data = data.copy(grains = newGrains)
                }
                controller.onDelete = { _, _ ->
                    data = data.dropGrain(parent.indexOf(controller))
                }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val behavioursController =
        columnsController(data.behaviours, data) { parent, behaviour, previous ->
            previous ?: BehaviourEditController(behaviour, data).wrap { controller ->
                controller.onUpdate = { _, new, _ ->
                    val behaviours = data.behaviours.toMutableList()
                    behaviours[parent.indexOf(controller)] = new
                    data = data.copy(behaviours = behaviours)
                }
                controller.onDelete = { old, _ ->
                    val behaviours = data.behaviours.toMutableList()
                    behaviours.removeAt(parent.indexOf(controller))
                    data = data.copy(behaviours = behaviours)
                }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    override val container = Columns(
        Column(fieldsController, size = ColumnSize.S11),
        Column(addFieldButton, size = ColumnSize.S1),
        Column(
            Level(
                left = listOf(Title("Grains", TextSize.S4)),
                right = listOf(addGrainButton),
                mobile = true
            ),
            grainsController,
            size = ColumnSize.OneThird
        ),
        Column(
            Level(
                left = listOf(
                    Title("Behaviours", TextSize.S4)
                ),
                right = listOf(addBehaviourButton),
                mobile = true
            ),
            behavioursController,
            size = ColumnSize.TwoThirds
        ),
        multiline = true
    )


    init {
        refresh()
    }

    override fun refresh() {
        grainsController.refresh()
        behavioursController.refresh()
    }


}
