package com.centyllion.client.page

import bulma.BulmaElement
import bulma.DropdownItem
import bulma.Icon
import bulma.Size
import bulma.TextColor
import bulma.bulmaList
import bulma.className
import bulma.span
import com.centyllion.client.AppContext
import com.centyllion.common.roleIcons
import kotlinx.html.a
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import kotlin.browser.document

interface BulmaPage : BulmaElement {
    val appContext: AppContext

    fun createMenuItem(
        text: String, icon: String, color: TextColor = TextColor.None,
        requiredRole: String? = null, disabled: Boolean = false, onClick: () -> Unit = {}
    ) =
        MenuItem(
            Icon(icon, color = color), span(text),
            Icon(roleIcons[requiredRole] ?: "", Size.Small, TextColor.GreyLight),
            disabled = disabled || requiredRole != null && !appContext.hasRole(requiredRole)
        ) { onClick() }

}


class MenuItem(
    val itemIcon: Icon, val itemText: BulmaElement, roleIcon: Icon?,
    disabled: Boolean = false, onSelect: (MenuItem) -> Unit = {}
) : DropdownItem {

    override val root: HTMLElement = document.create.a(classes = "dropdown-item") {
        onClickFunction = { if (!this@MenuItem.disabled) onSelect(this@MenuItem) }
    }

    override var text: String
        get() = itemText.text
        set(value) {
            itemText.text = value
        }

    val body by bulmaList(listOfNotNull(itemIcon, itemText, span(" "), roleIcon), root)

    override var disabled by className(disabled, "is-disabled", root)

}
