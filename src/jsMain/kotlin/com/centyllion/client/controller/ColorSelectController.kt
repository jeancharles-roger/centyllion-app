package com.centyllion.client.controller

import bulma.*
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

    val icon = Icon("circle")

    val search = Input(placeholder = "Search", rounded = true) { _, _ -> refresh() }

    val colorColumns = Columns(multiline = true, mobile = true).apply {
        columns = colorNames.keys.map { column(it) }
    }

    override val container = Dropdown(icon = icon, rounded = true).apply {
        menuSize = "30rem"
        items = listOf(
            DropdownContentItem(Field(
                Control(
                    search,
                    Icon("search")
                )
            )),
            DropdownContentItem(colorColumns)
        )
    }

    private fun column(color: String): Column {
        val colorIcon = Icon("circle")
        colorIcon.root.style.color = color
        val item = DropdownSimpleItem("", colorIcon) {
            this.data = color
            this.container.toggleDropdown()
        }
        val column = Column(item, size = ColumnSize.S1, mobileSize = ColumnSize.S2)
        column.root.style.padding = "0rem"
        return column
    }

    override fun refresh() {
        icon.root.style.color = data
        colorColumns.columns = colorNames.keys.filter { it.contains(search.value, true) }.map { column(it) }
    }

}
