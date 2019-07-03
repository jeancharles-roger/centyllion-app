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
            grainsController.context = data
            grainsController.data = data.grains
            behavioursController.context = data
            behavioursController.data = data.behaviours
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

    val fieldsController = noContextColumnsController(data.fields) { field, previous ->
            previous ?: FieldEditController(field).wrap { controller ->
                controller.onUpdate = { old, new, _ ->
                    data = data.updateField(old, new)
                }
                controller.onDelete = { _, _ ->
                    data = data.dropField(controller.data)
                }
                Column(controller.container, size = ColumnSize.OneThird)
            }
        }

    val grainsController =
        columnsController(data.grains, data) { grain, previous ->
            previous ?: GrainEditController(grain, data).wrap { controller ->
                controller.onUpdate = { old, new, _ ->
                    data = data.updateGrain(old, new)
                }
                controller.onDelete = { delete, _ ->
                    data = data.dropGrain(data.grainIndex(delete))
                }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val behavioursController =
        columnsController(data.behaviours, data) { behaviour, previous ->
            previous ?: BehaviourEditController(behaviour, data).wrap { controller ->
                controller.onUpdate = { old, new, _ ->
                    data = data.updateBehaviour(old, new)
                }
                controller.onDelete = { delete, _ ->
                    data = data.dropBehaviour(delete)
                }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    override val container = Columns(
        Column(
            Level(
                left = listOf(Title("Fields", TextSize.S4)),
                right = listOf(addFieldButton),
                mobile = true
            ),
            fieldsController,
            size = ColumnSize.Full
        ),
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
