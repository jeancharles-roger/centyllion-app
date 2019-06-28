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
import bulma.Label
import bulma.NoContextController
import bulma.TextColor
import com.centyllion.model.solidIconNames
import kotlin.properties.Delegates.observable

class IconSelectController(
    sourceIcon: String,
    var onUpdate: (old: String, new: String, controller: IconSelectController) -> Unit = { _, _, _ -> }
) : NoContextController<String, Dropdown>() {

    override var data by observable(sourceIcon) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@IconSelectController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            container.disabled = readOnly
        }
    }

    val icon = Icon(iconName(data))

    val name = Label(data)

    val search = Input(placeholder = "Search", rounded = true) { _, _ -> refresh() }

    val iconColumns = Columns(multiline = true, mobile = true)

    override val container = Dropdown(
        DropdownContentItem(Field(Control(search, Icon("search")), name, grouped = true)),
        DropdownContentItem(iconColumns),
        text = "", icon = icon, rounded = true, menuWidth = "30rem"
    ) {
        // only append icons when clicked
        if (iconColumns.columns.isEmpty()) iconColumns.columns = solidIconNames.keys.map { column(it) }
    }

    private fun column(icon: String): Column {
        val colorIcon = Icon(icon, color = TextColor.Black)
        val item = DropdownSimpleItem("", colorIcon) {
            this.data = icon
            this.container.toggleDropdown()
        }
        val column = Column(item, size = ColumnSize.S1, mobileSize = ColumnSize.S2)
        column.root.style.padding = "0rem"
        return column
    }

    fun iconName(icon: String) = if (icon.startsWith("fa-")) icon.substring(4) else icon

    override fun refresh() {
        name.text = data
        icon.icon = iconName(data)
        icon.root.style.color = data
        iconColumns.columns = solidIconNames.keys.filter { it.contains(search.value, true) }.map { column(it) }
    }

}
