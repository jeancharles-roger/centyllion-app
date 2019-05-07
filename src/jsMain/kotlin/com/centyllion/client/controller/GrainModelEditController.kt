package com.centyllion.client.controller

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
            nameController.data = data.name
            descriptionController.data = data.description
            grainsController.data = data.grains
            behavioursController.data = data.behaviours
            behavioursController.context = data
            onUpdate(old, new, this@GrainModelEditController)
            refresh()
        }
    }

    val nameController = EditableStringController(data.name, "Name") { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description") { _, new, _ ->
        data = data.copy(description = new)
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(grains = data.grains + data.newGrain())
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(behaviours = data.behaviours + Behaviour())
    }

    val grainsController =
        noContextColumnsController<Grain, GrainEditController>(data.grains) { parent, grain ->
            val controller = GrainEditController(grain)
            controller.onUpdate = { _, new, _ ->
                val newGrains = data.grains.toMutableList()
                newGrains[parent.indexOf(controller)] = new
                data = data.copy(grains = newGrains)
            }
            controller.onDelete = { _, _ ->
                val newGrains = data.grains.toMutableList()
                newGrains.removeAt(parent.indexOf(controller))
                data = data.copy(grains = newGrains)
            }
            controller
        }

    val behavioursController =
        columnsController<Behaviour, GrainModel, BehaviourEditController>(data.behaviours, data)
        { parent, behaviour ->
            val controller = BehaviourEditController(behaviour, data)
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
        Column(nameController, size = ColumnSize.OneThird),
        Column(descriptionController, size = ColumnSize.TwoThirds),
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
        nameController.refresh()
        descriptionController.refresh()
        grainsController.refresh()
        behavioursController.refresh()
    }


}
