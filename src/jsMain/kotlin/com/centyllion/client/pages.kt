package com.centyllion.client

import bulma.BulmaElement
import com.centyllion.client.page.AdministrationPage
import com.centyllion.client.page.HomePage
import com.centyllion.client.page.ModelPage
import com.centyllion.client.page.ShowPage
import com.centyllion.common.adminRole
import com.centyllion.common.modelRole
import keycloak.KeycloakInstance

data class Page(
    val title: String, val id: String, val needUser: Boolean,
    val role: String?, val header: Boolean, val callback: (appContext: AppContext) -> BulmaElement
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = when {
        !needUser -> true
        role == null -> keycloak.authenticated
        else -> keycloak.hasRealmRole(role)
    }
}

const val contentSelector = "section.cent-main"

val pages = listOf(
    Page("Home", "home", true, null, true, ::HomePage),
    Page("Model", "model", true, modelRole, true, ::ModelPage),
    Page("Administration", "administration", true, adminRole, true, ::AdministrationPage),
    Page("Show", "show", false, null, false, ::ShowPage)
)

val mainPage = pages[0]

val showPage = pages.find { it.id == "show" }!!
