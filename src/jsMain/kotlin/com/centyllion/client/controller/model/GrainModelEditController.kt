package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Controller
import bulma.Delete
import bulma.ElementColor
import bulma.Icon
import bulma.Level
import bulma.MultipleController
import bulma.NoContextController
import bulma.SubTitle
import bulma.TextSize
import bulma.Title
import bulma.columnsController
import bulma.iconButton
import bulma.noContextColumnsController
import bulma.wrap
import com.centyllion.model.Behaviour
import com.centyllion.model.Field
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
            fieldsController.data = data.fields
            grainsController.context = data
            grainsController.data = data.grains
            behavioursController.context = data
            behavioursController.data = data.behaviours
            editorController?.let { if (it.context is GrainModel) it.context = new }
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
        val field = data.newField()
        this.data = data.copy(fields = data.fields + field)
        edited = field
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        val grain = data.newGrain()
        this.data = data.copy(grains = data.grains + grain)
        edited = grain
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        val behaviour = Behaviour()
        this.data = data.copy(behaviours = data.behaviours + behaviour)
        edited = behaviour
    }

    val fieldsController: MultipleController<Field, Unit, Columns, Column, Controller<Field, Unit, Column>> =
        noContextColumnsController(data.fields, onClick = { field, _ -> edited = field })
        { field, previous ->
            previous ?: FieldDisplayController(field).wrap { controller ->
                controller.body.right += Delete { data = data.dropField(controller.data) }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val grainsController: MultipleController<Grain, GrainModel, Columns, Column, Controller<Grain, GrainModel, Column>> =
        columnsController(data.grains, data, onClick = { d, _ -> edited = d })
        { grain, previous ->
            previous ?: GrainDisplayController(grain, data).wrap { controller ->
                controller.body.right += Delete { data = data.dropGrain(controller.data) }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val behavioursController: MultipleController<Behaviour, GrainModel, Columns, Column, Controller<Behaviour, GrainModel, Column>> =
        columnsController(data.behaviours, data, onClick = { d, _ -> edited = d })
        { behaviour, previous ->
            previous ?: BehaviourDisplayController(behaviour, data).wrap { controller ->
                controller.header.right += Delete { data = data.dropBehaviour(controller.data) }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val emptyEditor = SubTitle("Select a element to edit it")
        .also { it.root.classList.add("has-text-centered") }

    val editorColumn = Column(emptyEditor, size = ColumnSize.Full)

    private var editorController: Controller<*, dynamic, dynamic>? = null

    private fun MultipleController<*, *, *, *, *>.updateSelection(value: Any?) {
        this.dataControllers.forEach {
            it.root.classList.toggle("is-selected", it.data == value)
        }
    }

    var edited by observable<Any?>(null) { _, previous, current ->
        if (previous != current) {
            editorController = when (current) {
                is Field -> FieldEditController(current) { old, new, _ ->
                    data = data.updateField(old, new)
                }
                is Grain -> GrainEditController(current, data) { old, new, _ ->
                    data = data.updateGrain(old, new)
                }
                is Behaviour -> BehaviourEditController(current, data) { old, new, _ ->
                    data = data.updateBehaviour(old, new)
                }
                else -> null
            }
            editorController?.root?.classList?.add("is-selected", "animated", "fadeIn", "fast")
            editorColumn.body = listOf(editorController ?: emptyEditor)
            fieldsController.updateSelection(current)
            grainsController.updateSelection(current)
            behavioursController.updateSelection(current)
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
            size = ColumnSize.OneQuarter
        ),
        Column(
            Level(
                left = listOf(Title("Grains", TextSize.S4)),
                right = listOf(addGrainButton),
                mobile = true
            ),
            grainsController,
            size = ColumnSize.OneQuarter
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
            size = ColumnSize.Half
        ),
        editorColumn,
        multiline = true
    )

    override fun refresh() {
        grainsController.refresh()
        behavioursController.refresh()
    }
}
