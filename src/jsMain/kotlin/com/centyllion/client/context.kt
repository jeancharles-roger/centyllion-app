package com.centyllion.client

import KeycloakInstance
import org.w3c.dom.HTMLElement

interface AppContext {

    // TODO Add NavBar
    //val navbar: NavBar

    val root: HTMLElement

    val keycloak: KeycloakInstance?

    val api: Api
}
