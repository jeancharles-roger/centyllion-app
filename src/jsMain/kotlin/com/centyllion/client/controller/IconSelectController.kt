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

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            container.disabled = readOnly
        }
    }

    val icon = Icon(iconName(data))

    val search = Input(placeholder = "Search", rounded = true) { _, _ -> refresh() }

    val iconColumns = Columns(multiline = true, mobile = true).apply {
        columns = solidIconNames.keys.map { column(it) }
    }

    override val container = Dropdown(data, icon = icon, rounded = true).apply {
        menuSize = "30rem"
        items = listOf(
            DropdownContentItem(Field(
                Control(
                    search,
                    Icon("search")
                )
            )),
            DropdownContentItem(iconColumns)
        )
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
        container.text = data
        icon.icon = iconName(data)
        icon.root.style.color = data
        iconColumns.columns = solidIconNames.keys.filter { it.contains(search.value, true) }.map { column(it) }
    }

}
