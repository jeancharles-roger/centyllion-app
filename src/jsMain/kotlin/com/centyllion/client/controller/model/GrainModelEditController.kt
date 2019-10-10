package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Controller
import bulma.ElementColor
import bulma.Icon
import bulma.Input
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
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.Behaviour
import com.centyllion.model.Field
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.ModelElement
import kotlin.properties.Delegates.observable

class GrainModelEditController(
    model: GrainModel, val page: BulmaPage,
    val onUpdate: (old: GrainModel, new: GrainModel, controller: GrainModelEditController) -> Unit =
        { _, _, _ -> }
) : NoContextController<GrainModel, Columns>() {

    override var data: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            fieldsController.data = data.fields.filtered()
            grainsController.context = data
            grainsController.data = data.grains.filtered()
            behavioursController.context = data
            behavioursController.data = data.behaviours.filtered()
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

    // search input
    val searchInput: Input = Input("", page.i18n("Search"), rounded = true, size = Size.Small) { _, _ ->
        fieldsController.data = data.fields.filtered()
        grainsController.data = data.grains.filtered()
        behavioursController.data = data.behaviours.filtered()
    }

    val clearSearch = iconButton(Icon("times"), rounded = true, size = Size.Small) { searchInput.value = "" }

    val search = bulma.Field(Control(searchInput, Icon("search"), expanded = true), Control(clearSearch), addons = true)

    val addFieldButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        val field = data.newField(page.i18n("Field"))
        this.data = data.copy(fields = data.fields + field)
        edited = field
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        val grain = data.newGrain(page.i18n("Grain"))
        this.data = data.copy(grains = data.grains + grain)
        edited = grain
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        val behaviour = data.newBehaviour(page.i18n("Behaviour"))
        this.data = data.copy(behaviours = data.behaviours + behaviour)
        edited = behaviour
    }

    val fieldsController: MultipleController<Field, Unit, Columns, Column, Controller<Field, Unit, Column>> =
        noContextColumnsController(data.fields, onClick = { field, _ -> edited = field })
        { field, previous ->
            previous ?: FieldDisplayController(field).wrap { controller ->
                controller.onDelete = {
                    if (edited == controller.data) edited = null
                    data = data.dropField(controller.data)
                }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val grainsController: MultipleController<Grain, GrainModel, Columns, Column, Controller<Grain, GrainModel, Column>> =
        columnsController(data.grains, data, onClick = { d, _ -> edited = d })
        { grain, previous ->
            previous ?: GrainDisplayController(grain, data).wrap { controller ->
                controller.onDelete = {
                    if (edited == controller.data) edited = null
                    data = data.dropGrain(controller.data)
                }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val behavioursController: MultipleController<Behaviour, GrainModel, Columns, Column, Controller<Behaviour, GrainModel, Column>> =
        columnsController(data.behaviours, data, onClick = { d, _ -> edited = d })
        { behaviour, previous ->
            previous ?: BehaviourDisplayController(behaviour, data, page).wrap { controller ->
                controller.onDelete = {
                    if (edited == controller.data) edited = null
                    data = data.dropBehaviour(controller.data)
                }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val emptyEditor = SubTitle(page.i18n("Select a element to edit it"))
        .also { it.root.classList.add("has-text-centered") }

    val editorColumn = Column(emptyEditor, size = ColumnSize.TwoThirds)

    private var editorController: Controller<*, dynamic, dynamic>? = null

    private fun MultipleController<*, *, *, *, *>.updateSelection(value: Any?) {
        val found =
            this.dataControllers.find { it.data === value } ?:
            this.dataControllers.find { it.data == value }

        this.dataControllers.forEach {
            it.root.classList.toggle("is-selected", found == it)
        }
    }

    var edited: ModelElement? by observable<ModelElement?>(null) { _, previous, current ->
        if (previous !== current) {
            editorController = when (current) {
                is Field -> FieldEditController(current, page) { old, new, _ ->
                    data = data.updateField(old, new)
                }
                is Grain -> GrainEditController(current, data, page) { old, new, _ ->
                    data = data.updateGrain(old, new)
                }
                is Behaviour -> BehaviourEditController(current, data, page) { old, new, _ ->
                    data = data.updateBehaviour(old, new)
                }
                else -> null
            }
            editorController?.root?.classList?.add("animated", "fadeIn", "faster")
            editorController?.readOnly = this.readOnly
            editorColumn.body = listOf(editorController ?: emptyEditor)
            fieldsController.updateSelection(current)
            grainsController.updateSelection(current)
            behavioursController.updateSelection(current)
        }
    }

    override val container = Columns(
        Column(
            search,
            Level(
                left = listOf(Title(page.i18n("Fields"), TextSize.S4)),
                right = listOf(addFieldButton),
                mobile = true
            ),
            fieldsController,
            Level(
                left = listOf(Title(page.i18n("Grains"), TextSize.S4)),
                right = listOf(addGrainButton),
                mobile = true
            ),
            grainsController,
            Level(
                left = listOf(
                    Title(page.i18n("Behaviours"), TextSize.S4)
                ),
                right = listOf(addBehaviourButton),
                mobile = true
            ),
            behavioursController,
            size = ColumnSize.OneThird
        ).apply {
            root.style.height = "80vh"
            root.style.overflowY = "auto"
        },
        editorColumn,
        multiline = true, vcentered = true
    )

    override fun refresh() {
        grainsController.refresh()
        behavioursController.refresh()
    }

    fun scrollToEdited() {
        (editorController ?: emptyEditor).root.scrollIntoView()
    }

    fun <T: ModelElement> List<T>.filtered() = searchInput.value.let { filter ->
        if (filter.isBlank()) this
        else this.filter { it.name.contains(filter, true) || it.description.contains(filter, true) }
    }
}
