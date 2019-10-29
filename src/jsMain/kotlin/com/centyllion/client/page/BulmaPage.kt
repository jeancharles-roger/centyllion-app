package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Button
import bulma.Dropdown
import bulma.DropdownDivider
import bulma.DropdownSimpleItem
import bulma.ElementColor
import bulma.Icon
import bulma.ModalCard
import bulma.NavBarItem
import bulma.TextColor
import bulma.extension.ToastAnimation
import bulma.extension.ToastOptions
import bulma.extension.bulmaToast
import com.centyllion.client.AppContext
import com.centyllion.client.ClientEvent
import kotlin.js.Date
import kotlin.js.Promise

interface BulmaPage : BulmaElement {
    val appContext: AppContext

    fun onExit() = Promise.resolve(true)

    fun navBarItem(): List<NavBarItem> = emptyList()

    fun i18n(key: String, vararg parameters: Any) = appContext.locale.i18n(key, *parameters)

    fun error(throwable: Throwable) {
        fun findCause(throwable: Throwable): Throwable = throwable.cause?.let { findCause(it) } ?: throwable
        findCause(throwable).let {
            error(it.message.toString())
            console.error(it)
        }
    }

    fun error(content: String, vararg parameters: Any) = event(appContext.locale.i18n(content, *parameters), ElementColor.Danger)

    fun warning(content: String, vararg parameters: Any) = event(appContext.locale.i18n(content, *parameters), ElementColor.Warning)

    fun message(content: String, vararg parameters: Any) = event(appContext.locale.i18n(content, *parameters), ElementColor.Info)

    private fun event(content: String, color: ElementColor) {
        val date = Date().toISOString()
        val event = ClientEvent(date, content, color)
        appContext.notify(event)
        notification(event.context, event.color)
    }

    fun modalDialog(title: String, body: List<BulmaElement>, vararg buttons: Button, active: Boolean = true): ModalCard {
        val modal = ModalCard(title, body) { root.removeChild(it.root) }
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
        modal.active = active
        return modal
    }

    fun createMenuDivider() = DropdownDivider()

    fun createMenuItem(
        parent: Dropdown, text: String, icon: String, color: TextColor = TextColor.None,
        disabled: Boolean = false, onClick: () -> Unit = {}
    ) = DropdownSimpleItem(text, Icon(icon, color = color), disabled) {
        onClick()
        parent.active = false
    }
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
