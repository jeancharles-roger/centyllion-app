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

    val dropdown: Dropdown = Dropdown("", icon = icon, rounded = true).apply {
        menuSize = "30rem"
        items = colors()
    }

    override val container: Field = Field(Control(dropdown))


    private fun column(color: String): Column {
        val colorIcon = Icon("circle")
        colorIcon.root.style.color = color
        val item = DropdownSimpleItem("", colorIcon) {
            this.data = color
            this.dropdown.toggleDropdown()
        }
        val column = Column(item, size = ColumnSize.S1)
        column.root.style.padding = "0rem"
        return column
    }

    private fun colors() = listOf(DropdownContentItem(
        Columns(multiline = true).apply { columns = colorNames.map { column(it) } }
    ))

    override fun refresh() {
        icon.root.style.color = data
    }

}
