package com.centyllion.client

import bulma.BulmaElement
import com.centyllion.client.page.AdministrationPage
import com.centyllion.client.page.ExplorePage
import com.centyllion.client.page.HomePage
import com.centyllion.client.page.ShowPage
import com.centyllion.client.page.SubscriptionPage
import com.centyllion.client.page.TestPage
import com.centyllion.common.adminRole
import keycloak.KeycloakInstance
import kotlin.js.Promise

data class Page<T: BulmaElement>(
    val title: String, val id: String, val needUser: Boolean, val role: String?,
    val header: Boolean, val callback: (appContext: AppContext) -> T,
    val exitCallback: T.(appContext: AppContext) -> Promise<Boolean> = { _ -> Promise.resolve(true) }
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = when {
        !needUser -> true
        role == null -> keycloak.authenticated
        else -> keycloak.hasRealmRole(role)
    }
}

const val contentSelector = "section.cent-main"

val homePage = Page("Home", "home", true, null, true, ::HomePage)
val explorePage = Page("Explore", "explore", false, null, true, ::ExplorePage)
val showPage = Page("Show", "show", false, null, false, ::ShowPage, ShowPage::canExit)
val subscribePage = Page("Subscribe", "subscribe", true, null, false, ::SubscriptionPage)
val testPage = Page("Test", "test", true, adminRole, true, ::TestPage)
val administrationPage = Page("Administration", "administration", true, adminRole, true, ::AdministrationPage)

val pages = listOf(homePage, explorePage, showPage, subscribePage, testPage, administrationPage)

