package com.centyllion.client.controller

import bulma.*
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

    val icon = Icon(iconName(data))

    val dropdown: Dropdown = Dropdown("", icon = icon, rounded = true).apply {
        menuSize = "40rem"
        items = icons()
    }

    override val container = dropdown

    private fun column(icon: String): Column {
        val colorIcon = Icon(icon, color = TextColor.Black)
        val item = DropdownSimpleItem("", colorIcon) {
            this.data = icon
            this.dropdown.toggleDropdown()
        }
        val column = Column(item, size = ColumnSize.S1, mobileSize = ColumnSize.S2)
        column.root.style.padding = "0rem"
        return column
    }

    private fun icons() = listOf(DropdownContentItem(
        Columns(multiline = true, mobile = true).apply { columns = solidIconNames.keys.map { column(it) } }
    ))

    fun iconName(icon: String) = if (icon.startsWith("fa-")) icon.substring(4) else icon

    override fun refresh() {
        icon.icon = iconName(data)
        icon.root.style.color = data
    }

}
