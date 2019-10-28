package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Dropdown
import bulma.DropdownContentItem
import bulma.DropdownSimpleItem
import bulma.Field
import bulma.Icon
import bulma.Input
import bulma.NoContextController
import com.centyllion.model.colorNames
import kotlin.properties.Delegates.observable

class ColorSelectController(
    color: String,
    var onUpdate: (old: String, new: String, controller: ColorSelectController) -> Unit = { _, _, _ -> }
) : NoContextController<String, Dropdown>() {

    override var data by observable(color) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@ColorSelectController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            container.disabled = readOnly
        }
    }

    val icon = Icon("circle").apply {
        root.style.color = color
    }

    val search = Input(placeholder = "Search", rounded = true) { _, filter ->
        colorColumns.columns = colorNames.keys.filter { it.contains(filter, true) }.map { column(it) }
    }

    val colorColumns = Columns(gapless = true, multiline = true, mobile = true)

    override val container = Dropdown(
        DropdownContentItem(Field(Control(search, Icon("search")))),
        DropdownContentItem(colorColumns),
        icon = icon, rounded = true, menuWidth = "30rem"
    ) {
        // only append colors when clicked
        if (colorColumns.columns.isEmpty()) colorColumns.columns = colorNames.keys.map { column(it) }
    }

    private fun column(color: String): Column {
        val colorIcon = Icon("").apply {
            root.style.backgroundColor = color
            root.style.border = "black solid 0.1rem"
            root.style.borderRadius = "50%"
        }
        colorIcon.root.style.color = color
        val item = DropdownSimpleItem("", colorIcon) {
            this.data = color
            this.container.toggleDropdown()
        }
        return Column(item, size = ColumnSize.S1, mobileSize = ColumnSize.S2)
    }

    override fun refresh() {
        icon.root.style.color = data
    }
}
