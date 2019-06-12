package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class GrainModelEditController(
    model: GrainModel,
    val onUpdate: (old: GrainModel, new: GrainModel, controller: GrainModelEditController) -> Unit =
        { _, _, _ -> }
) : NoContextController<GrainModel, Columns>() {

    override var data: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            grainsController.data = data.grains
            behavioursController.data = data.behaviours
            behavioursController.context = data
            onUpdate(old, new, this@GrainModelEditController)
            refresh()
        }
    }

    override var readOnly by observable(false) { _, old, new ->
        if (old != new) {
            addGrainButton.invisible = new
            addBehaviourButton.invisible = new
            grainsController.readOnly = new
            behavioursController.readOnly = new
        }
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(grains = data.grains + data.newGrain())
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(behaviours = data.behaviours + Behaviour())
    }

    val grainsController =
        noContextColumnsController<Grain, GrainEditController>(data.grains) { parent, grain, previous ->
            val controller = previous ?: GrainEditController(grain)
            controller.onUpdate = { _, new, _ ->
                val newGrains = data.grains.toMutableList()
                newGrains[parent.indexOf(controller)] = new
                data = data.copy(grains = newGrains)
            }
            controller.onDelete = { _, _ ->
                data = data.dropGrain(parent.indexOf(controller))
            }
            controller
        }

    val behavioursController =
        columnsController<Behaviour, GrainModel, BehaviourEditController>(data.behaviours, data)
        { parent, behaviour, previous ->
            val controller = previous ?: BehaviourEditController(behaviour, data)
            controller.onUpdate = { _, new, _ ->
                val behaviours = data.behaviours.toMutableList()
                behaviours[parent.indexOf(controller)] = new
                data = data.copy(behaviours = behaviours)
            }
            controller.onDelete = { _, _ ->
                val behaviours = data.behaviours.toMutableList()
                behaviours.removeAt(parent.indexOf(controller))
                data = data.copy(behaviours = behaviours)
            }
            controller
        }

    override val container = Columns(
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