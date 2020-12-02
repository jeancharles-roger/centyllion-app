package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Controller
import bulma.ElementColor
import bulma.Icon
import bulma.Level
import bulma.MultipleController
import bulma.NoContextController
import bulma.Size
import bulma.SubTitle
import bulma.TextSize
import bulma.Title
import bulma.columnsController
import bulma.iconButton
import bulma.noContextColumnsController
import bulma.wrap
import com.centyllion.client.controller.utils.SearchController
import com.centyllion.client.controller.utils.filtered
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.Behaviour
import com.centyllion.model.Field
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.ModelElement
import com.centyllion.model.behaviourIcon
import com.centyllion.model.fieldIcon
import com.centyllion.model.grainIcon
import org.w3c.dom.SMOOTH
import org.w3c.dom.ScrollBehavior
import org.w3c.dom.ScrollOptions
import kotlin.properties.Delegates.observable

class GrainModelEditController(
    val page: BulmaPage, model: GrainModel,
    val onUpdate: (old: GrainModel, new: GrainModel, controller: GrainModelEditController) -> Unit =
        { _, _, _ -> }
) : NoContextController<GrainModel, Columns>() {

    override var data: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            fieldsController.data = data.fields.filtered(searchController.data)
            grainsController.context = data
            grainsController.data = data.grains.filtered(searchController.data)
            behavioursController.context = data
            behavioursController.data = data.behaviours.filtered(searchController.data)

            // if edited isn't in model anymore, stops edition
            if (edited != null && (new.fields + new.grains + new.behaviours).none { it == edited } ) edit(null)

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
            editorController?.readOnly = new
        }
    }

    val searchController: SearchController = SearchController(page) { _, filter ->
        fieldsController.data = data.fields.filtered(filter)
        grainsController.data = data.grains.filtered(filter)
        behavioursController.data = data.behaviours.filtered(filter)
    }

    val addFieldButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val field = data.newField(page.i18n("Field"))
        this.data = data.copy(fields = data.fields + field)
        edit(field)
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val grain = data.newGrain(page.i18n("Grain"))
        this.data = data.copy(grains = data.grains + grain)
        edit(grain)
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val behaviour = data.newBehaviour(page.i18n("Behaviour"))
        this.data = data.copy(behaviours = data.behaviours + behaviour)
        edit(behaviour)
    }

    val fieldsController: MultipleController<Field, Unit, Columns, Column, Controller<Field, Unit, Column>> =
        noContextColumnsController(data.fields, onClick = { field, _ -> edit(field) })
        { field, previous ->
            previous ?: FieldDisplayController(field).wrap { controller ->
                controller.onDelete = {data = data.dropField(controller.data) }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val grainsController: MultipleController<Grain, GrainModel, Columns, Column, Controller<Grain, GrainModel, Column>> =
        columnsController(data.grains, data, onClick = { grain, _ -> edit(grain) })
        { grain, previous ->
            previous ?: GrainDisplayController(page, grain, data).wrap { controller ->
                controller.onDelete = { data = data.dropGrain(controller.data) }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val behavioursController: MultipleController<Behaviour, GrainModel, Columns, Column, Controller<Behaviour, GrainModel, Column>> =
        columnsController(data.behaviours, data, onClick = { behaviour, _ -> edit(behaviour) })
        { behaviour, previous ->
            previous ?: BehaviourDisplayController(page, behaviour, data).wrap { controller ->
                controller.onDelete = { data = data.dropBehaviour(controller.data) }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val emptyEditor = SubTitle(page.i18n("Select a element to edit it"))
        .also { it.root.classList.add("has-text-centered") }

    val editorColumn = Column(emptyEditor, size = ColumnSize.TwoThirds)

    private var editorController: Controller<*, dynamic, dynamic>? = null

    val editor get() = editorController

    private fun MultipleController<*, *, *, *, *>.updateSelection(value: Any?) {
        val found =
            this.dataControllers.find { it.data === value } ?:
            this.dataControllers.find { it.data == value }

        this.dataControllers.forEach {
            it.root.classList.toggle("is-selected", found == it)
        }
    }

    fun edit(element: ModelElement?) {
        editorController = when (element) {
            is Field -> FieldEditController(element, page) { old, new, _ ->
                edited = new
                data = data.updateField(old, new)
            }
            is Grain -> GrainEditController(element, data, page) { old, new, _ ->
                edited = new
                data = data.updateGrain(old, new)
            }
            is Behaviour -> BehaviourEditController(element, data, page) { old, new, _ ->
                edited = new
                data = data.updateBehaviour(old, new)
            }
            else -> null
        }
        editorController?.root?.classList?.add("animated", "fadeIn", "faster")
        editorController?.readOnly = this.readOnly
        editorColumn.body = listOf(editorController ?: emptyEditor)
        fieldsController.updateSelection(element)
        grainsController.updateSelection(element)
        behavioursController.updateSelection(element)
    }

    private var edited: ModelElement? = null

    val selectorColumn = Column(
        searchController,
        Level(
            left = listOf(Icon(fieldIcon), Title(page.i18n("Fields"), TextSize.S4)),
            right = listOf(addFieldButton),
            mobile = true
        ),
        fieldsController,
        Level(
            left = listOf(Icon(grainIcon), Title(page.i18n("Grains"), TextSize.S4)),
            right = listOf(addGrainButton),
            mobile = true
        ),
        grainsController,
        Level(
            left = listOf(Icon(behaviourIcon), Title(page.i18n("Behaviours"), TextSize.S4)),
            right = listOf(addBehaviourButton),
            mobile = true
        ),
        behavioursController,
        size = ColumnSize.OneThird
    ).apply {
        root.style.height = "80vh"
        root.style.overflowY = "auto"
    }

    override val container = Columns(selectorColumn, editorColumn, multiline = true, vcentered = true)

    override fun refresh() {
        grainsController.refresh()
        behavioursController.refresh()
    }

    fun scrollToEdited() {
        root.scrollIntoView(ScrollOptions(ScrollBehavior.SMOOTH))
    }

}
