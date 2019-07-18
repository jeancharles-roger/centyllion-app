package com.centyllion.client

import bulma.ElementColor
import bulma.NavBar
import com.centyllion.model.User
import keycloak.KeycloakInstance
import threejs.extra.core.Font
import kotlin.js.Promise


data class ClientEvent(
    val date: String,
    val context: String,
    val color: ElementColor
)

interface AppContext {

    val navBar: NavBar

    val keycloak: KeycloakInstance

    fun hasRole(role: String) = keycloak.hasRealmRole(role)

    val me: User?

    val api: Api

    val stripeKey: String

    fun getFont(path: String): Promise<Font>

    val events: List<ClientEvent>

    fun notify(event: ClientEvent)

    /** Open the given [page] */
    fun openPage(
        page: Page<*>, parameters: Map<String, String> = emptyMap(),
        clearParameters: Boolean = true, register: Boolean = true
    )
}
