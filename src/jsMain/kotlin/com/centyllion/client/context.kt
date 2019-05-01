package com.centyllion.client

import KeycloakInstance
import bulma.ElementColor
import bulma.NavBar
import org.w3c.dom.HTMLElement


data class ClientEvent(
    val date: String,
    val context: String,
    val color: ElementColor
)

interface AppContext {

    val navBar: NavBar

    val root: HTMLElement

    val keycloak: KeycloakInstance

    val api: Api

    val events: List<ClientEvent>

    fun error(throwable: Throwable)

    fun error(content: String)

    fun warning(content: String)

    fun message(content: String)
}
