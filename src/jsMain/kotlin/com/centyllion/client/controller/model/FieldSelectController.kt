package com.centyllion.client.controller.model

import bulma.Control
import bulma.Controller
import bulma.Dropdown
import bulma.DropdownSimpleItem
import bulma.Icon
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable
import bulma.Field as BField

class FieldSelectController(
    field: Field, fields: List<Field>,
    var onUpdate: (old: Field?, new: Field?, controller: FieldSelectController) -> Unit = { _, _, _ -> }
) : Controller<Field, List<Field>, BField> {

    override var data by observable(field) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@FieldSelectController)
            refresh()
        }
    }

    override var context: List<Field> by observable(fields) { _, old, new ->
        if (old != new) {
            dropdown.items = items()
            this@FieldSelectController.refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            dropdown.disabled = readOnly
        }
    }

    val icon = Icon("square")

    val dropdown: Dropdown = Dropdown(text = field.label(), icon = icon, rounded = true).apply { items = items() }

    override val container = BField(Control(dropdown))

    private fun item(field: Field): DropdownSimpleItem {
        val grainIcon = Icon("square")
        grainIcon.root.style.color = field.color
        return DropdownSimpleItem(field.label(), grainIcon) {
            this.data = field
            this.dropdown.toggleDropdown()
        }
    }

    private fun items() = context.map { item(it) }

    override fun refresh() {
        dropdown.text = data.label()
        icon.root.style.color = data.color
    }

}
