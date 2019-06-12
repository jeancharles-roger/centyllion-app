package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.model.Grain
import kotlin.properties.Delegates.observable

class GrainSelectController(
    grain: Grain?, grains: List<Grain>,
    var onUpdate: (old: Grain?, new: Grain?, controller: GrainSelectController) -> Unit = { _, _, _ -> }
) : Controller<Grain?, List<Grain>, Field> {

    override var data by observable(grain) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@GrainSelectController)
            refresh()
        }
    }

    override var context: List<Grain> by observable(grains) { _, old, new ->
        if (old != new) {
            dropdown.items = items()
            this@GrainSelectController.refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            dropdown.disabled = readOnly
        }
    }

    val icon = Icon(data?.icon ?: "circle")

    val dropdown: Dropdown = Dropdown(text = grain?.label() ?: "none", icon = icon, rounded = true).apply { items = items() }

    override val container: Field = Field(Control(dropdown))

    private fun item(grain: Grain): DropdownSimpleItem {
        val grainIcon = Icon(grain.icon)
        grainIcon.root.style.color = grain.color
        return DropdownSimpleItem(grain.label(), grainIcon) {
            this.data = grain
            this.dropdown.toggleDropdown()
        }
    }

    private fun emptyItem() = DropdownSimpleItem("none", Icon("times-circle")) {
        data = null
        dropdown.toggleDropdown()
    }

    private fun items() = context.map { item(it) } + emptyItem()

    override fun refresh() {
        dropdown.text = data?.label() ?: "none"
        icon.icon = data?.icon ?: "times-circle"
        icon.root.style.color = data?.color ?: ""
    }

}
