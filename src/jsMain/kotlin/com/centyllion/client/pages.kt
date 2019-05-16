package com.centyllion.client

import bulma.BulmaElement
import com.centyllion.client.page.AdministrationPage
import com.centyllion.client.page.HomePage
import com.centyllion.client.page.ShowPage
import com.centyllion.common.adminRole
import keycloak.KeycloakInstance

data class Page(
    val title: String, val id: String, val needUser: Boolean,
    val role: String?, val callback: (appContext: AppContext) -> BulmaElement
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = when {
        !needUser -> true
        role == null -> keycloak.authenticated
        else -> keycloak.hasRealmRole(role)
    }
}

const val contentSelector = "section.cent-main"

val homePage = Page("Home", "home", true, null, ::HomePage)
val showPage = Page("Show", "show", false, null, ::ShowPage)
val administrationPage = Page("Administration", "administration", true, adminRole, ::AdministrationPage)

val header = listOf(homePage, administrationPage)


