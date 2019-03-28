package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.colorNames
import kotlin.properties.Delegates.observable

class ColorSelectController(
    color: String,
    var onUpdate: (old: String, new: String, controller: ColorSelectController) -> Unit = { _, _, _ -> }
) : NoContextController<String, Field>() {

    override var data by observable(color) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@ColorSelectController)
            refresh()
        }
    }

    val icon = Icon("circle")

    val dropdown: Dropdown = Dropdown("", icon = icon, rounded = true).apply { items = colors() }

    override val container: Field = Field(Control(dropdown))

    private fun item(color: String): DropdownSimpleItem {
        val colorIcon = Icon("circle")
        colorIcon.root.style.color = color
        return DropdownSimpleItem(color, colorIcon) {
            this.data = color
            this.dropdown.toggleDropdown()
        }
    }

    private fun colors() = colorNames.map { item(it) }

    override fun refresh() {
        icon.root.style.color = data
    }

}
