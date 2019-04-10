package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Behaviour
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class SourceReactiveSelectController(
    index: Int, behaviour: Behaviour, model: GrainModel,
    var onUpdate: (old: Int, new: Int, controller: SourceReactiveSelectController) -> Unit = { _, _, _ -> }
) : Controller<Int, Pair<Behaviour, GrainModel>, Field> {

    override var data: Int by observable(index) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@SourceReactiveSelectController)
            refresh()
        }
    }

    override var context: Pair<Behaviour, GrainModel> by observable(behaviour to model) { _, old, new ->
        if (old != new) {
            dropdown.items = items()
            this@SourceReactiveSelectController.refresh()
        }
    }

    val icon = Icon("circle")

    val dropdown: Dropdown = Dropdown(indexLabel(data), icon = icon, rounded = true).apply { items = items() }

    override val container: Field = Field(Control(dropdown))

    private fun item(index: Int): DropdownSimpleItem {
        val grainIcon = Icon("circle")
        grainIcon.root.style.color = indexColor(index)
        return DropdownSimpleItem(indexLabel(index), grainIcon) {
            this.data = index
            this.dropdown.toggleDropdown()
        }
    }

    private fun emptyItem() = DropdownSimpleItem("none", Icon("times-circle")) {
        data = -1
        dropdown.toggleDropdown()
    }

    private fun indexLabel(index: Int) = if (index >= 0) "reactive ${index + 1}" else "none"

    private fun indexColor(index: Int) = when {
        index < 0 -> ""
        else -> {
            val graindId = if (index == 0)
                context.first.mainReactiveId else
                context.first.reaction[index - 1].reactiveId
            context.second.indexedGrains[graindId]?.color ?: ""
        }
    }

    private fun items() = listOf(item(0)) +
            context.first.reaction.mapIndexed { index, _ -> item(index + 1) } +
            emptyItem()


    override fun refresh() {
        dropdown.text = indexLabel(data)
        icon.icon = if (data >= 0) "circle" else "times-circle"
        icon.root.style.color = indexColor(data)
    }

}
