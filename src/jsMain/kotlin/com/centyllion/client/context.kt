package com.centyllion.client

import KeycloakInstance
import bulma.NavBar
import org.w3c.dom.HTMLElement

interface AppContext {

    val navBar: NavBar

    val root: HTMLElement

    val keycloak: KeycloakInstance

    val api: Api
}
