package com.centyllion.client

import bulma.BulmaElement
import bulma.Title
import bulma.div
import bulma.noContextColumnsController
import com.centyllion.client.controller.FeaturedController
import com.centyllion.client.page.AdministrationPage
import com.centyllion.client.page.HomePage
import com.centyllion.client.page.ShowPage
import com.centyllion.common.adminRole
import com.centyllion.model.FeaturedDescription
import keycloak.KeycloakInstance

data class Page(
    val title: String, val id: String, val needUser: Boolean, val role: String?,
    val header: Boolean, val callback: (appContext: AppContext) -> BulmaElement
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = when {
        !needUser -> true
        role == null -> keycloak.authenticated
        else -> keycloak.hasRealmRole(role)
    }
}

const val contentSelector = "section.cent-main"

val homePage = Page("Home", "home", true, null, true, ::HomePage)
val explorePage = Page("Explore", "explore", false, null, true, ::explore)
val showPage = Page("Show", "show", false, null, false, ::ShowPage)
val administrationPage = Page("Administration", "administration", true, adminRole, true, ::AdministrationPage)

val pages = listOf(homePage, explorePage, showPage, administrationPage)

fun explore(appContext: AppContext): BulmaElement {
    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { parent, data ->
        val controller = FeaturedController(data)
        controller.body.root.onclick = {
            openPage(showPage, appContext, mapOf("model" to data.modelId, "simulation" to data.simulationId))
        }
        controller.body.root.style.cursor = "pointer"
        controller
    }
    val page = div(
        Title("Explore featured models"), featuredController
    )

    appContext.api.fetchAllFeatured().then { models -> featuredController.data = models }
    return page
}
