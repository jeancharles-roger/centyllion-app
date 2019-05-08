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

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            dropdown.disabled = readOnly
        }
    }

    val icon = Icon(indexIcon(data)).apply {
        root.style.color = indexColor(data)
    }

    val dropdown: Dropdown = Dropdown(indexLabel(data), icon = icon, rounded = true).apply { items = items() }

    override val container: Field = Field(Control(dropdown))

    private fun item(index: Int): DropdownSimpleItem {
        val grainIcon = Icon(indexIcon(index))
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

    private fun indexIcon(index: Int) = when {
        index < 0 -> "times-circle"
        else -> {
            val grainId = if (index == 0)
                context.first.mainReactiveId else
                context.first.reaction[index - 1].reactiveId
            val grain = if (grainId >= 0) context.second.indexedGrains[grainId] else null
            if (grain != null) grain.icon else "times-circle"
        }
    }

    private fun indexColor(index: Int) = when {
        index < 0 -> ""
        else -> {
            val grainId = if (index == 0)
                context.first.mainReactiveId else
                context.first.reaction[index - 1].reactiveId
            context.second.indexedGrains[grainId]?.color ?: ""
        }
    }

    private fun items() = listOf(item(0)) +
            context.first.reaction.mapIndexed { index, _ -> item(index + 1) } +
            emptyItem()


    override fun refresh() {
        dropdown.text = indexLabel(data)
        icon.icon = indexIcon(data)
        icon.root.style.color = indexColor(data)
    }

}
