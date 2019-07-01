package com.centyllion.client

import bulma.BulmaElement
import bulma.Button
import bulma.ElementColor
import bulma.ModalCard
import bulma.NavBar
import com.centyllion.model.User
import keycloak.KeycloakInstance
import org.w3c.dom.HTMLElement
import threejs.extra.core.Font
import kotlin.js.Promise


data class ClientEvent(
    val date: String,
    val context: String,
    val color: ElementColor
)

interface AppContext {

    val navBar: NavBar

    val root: HTMLElement

    val keycloak: KeycloakInstance

    val me: User?

    val api: Api

    val stripeKey: String

    fun getFont(url: String): Promise<Font>

    val events: List<ClientEvent>

    fun error(throwable: Throwable)

    fun error(content: String)

    fun warning(content: String)

    fun message(content: String)

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


    /** Open the given [page] */
    fun openPage(
        page: Page<*>, parameters: Map<String, String> = emptyMap(),
        clearParameters: Boolean = true, register: Boolean = true
    )
}
