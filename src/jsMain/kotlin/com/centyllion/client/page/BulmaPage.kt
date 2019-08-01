package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Button
import bulma.DropdownItem
import bulma.ElementColor
import bulma.Icon
import bulma.ModalCard
import bulma.Size
import bulma.TextColor
import bulma.bulmaList
import bulma.className
import bulma.span
import bulmatoast.ToastAnimation
import bulmatoast.ToastOptions
import bulmatoast.bulmaToast
import com.centyllion.client.AppContext
import com.centyllion.client.ClientEvent
import com.centyllion.common.SubscriptionType
import com.centyllion.common.roleIcons
import kotlinx.html.a
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.js.Date
import kotlin.js.Promise
import kotlin.properties.Delegates

interface BulmaPage : BulmaElement {
    val appContext: AppContext

    fun onExit(): Promise<Boolean> = Promise.resolve(true)

    fun error(throwable: Throwable) {
        fun findCause(throwable: Throwable): Throwable = throwable.cause?.let { findCause(it) } ?: throwable
        findCause(throwable).let {
            error(it.message.toString())
            console.error(it)
        }
    }

    fun error(content: String) = event(content, ElementColor.Danger)

    fun warning(content: String) = event(content, ElementColor.Warning)

    fun message(content: String) = event(content, ElementColor.Info)

    private fun event(content: String, color: ElementColor) {
        val date = Date().toISOString()
        val event = ClientEvent(date, content, color)
        appContext.notify(event)
        notification(event.context, event.color)
    }

    fun modalDialog(title: String, body: BulmaElement, vararg buttons: Button): ModalCard {
        val modal = ModalCard(title, listOf(body)) { root.removeChild(it.root) }
        // wraps button actions with the closing of the modal dialog
        modal.buttons = buttons.map {
            val action = it.onClick
            it.onClick = {
                action(it)
                modal.active = false
            }
            it
        }
        root.appendChild(modal.root)
        return modal
    }

    fun createMenuItem(
        text: String, icon: String, color: TextColor = TextColor.None,
        requiredRole: String? = null, disabled: Boolean = false, onClick: () -> Unit = {}
    ) =
        MenuItem(
            Icon(icon, color = color), span(text), requiredRole,
            requiredRole == null || appContext.hasRole(requiredRole), disabled
        ) { onClick() }

}

private fun notification(content: String, color: ElementColor = ElementColor.None) {
    when (color) {
        ElementColor.Danger -> console.error(content)
        ElementColor.Warning -> console.warn(content)
        else -> console.log(content)
    }

    val animation = ToastAnimation("fadeIn", "fadeOut")
    val options = ToastOptions(
        content, color.className, 2000, "bottom-center",
        false, true, true, 0.8, animation
    )
    bulmaToast.toast(options)
}

class MenuItem(
    val itemIcon: Icon, val itemText: BulmaElement, val role: String? = null,
    hasRole: Boolean = role == null, disabled: Boolean = false, onSelect: (MenuItem) -> Unit = {}
) : DropdownItem {

    val roleIcon = Icon(roleIcons[role] ?: "", Size.Small, TextColor.GreyLight)

    override val root: HTMLElement = document.create.a(classes = "dropdown-item") {
        onClickFunction = { if (!this@MenuItem.internalDisabled) onSelect(this@MenuItem) }
    }

    override var text: String
        get() = itemText.text
        set(value) {
            itemText.text = value
        }

    val body by bulmaList(listOfNotNull(itemIcon, itemText, span(" "), roleIcon), root)

    var disabled by Delegates.observable(disabled) { _, _, new ->
        internalDisabled = new || !hasRole
    }

    private var internalDisabled by className(disabled || !hasRole, "is-disabled", root)

    init {
        // If the role isn't included in the biggest subscription it must be hidden when not matched
        hidden = role != null && !SubscriptionType.Master.roles.contains(role) && !hasRole
    }
}
