package com.centyllion.client

import com.centyllion.client.page.AdministrationPage
import com.centyllion.client.page.BulmaPage
import com.centyllion.client.page.ExplorePage
import com.centyllion.client.page.HomePage
import com.centyllion.client.page.RegisterPage
import com.centyllion.client.page.ShowPage
import com.centyllion.client.page.SubscriptionPage
import com.centyllion.common.adminRole

data class Page(
    val titleKey: String, val id: String, val needUser: Boolean, val role: String?,
    val header: Boolean, val callback: (appContext: AppContext) -> BulmaPage
) {
    fun authorized(context: AppContext): Boolean = when {
        !needUser -> true
        role == null -> context.keycloak.authenticated
        else -> context.hasRole(role)
    }
}

const val contentSelector = "section.cent-main"

val explorePage = Page("Explore", "/", false, null, true, ::ExplorePage)
val homePage = Page("Home", "/home", true, null, true, ::HomePage)
val showPage = Page("Show", "/show", false, null, false, ::ShowPage)
val register = Page("Register", "/register", false, null, false, ::RegisterPage)
val subscribePage = Page("Subscribe", "/subscribe", true, null, false, ::SubscriptionPage)
val administrationPage = Page("Administration", "/administration", true, adminRole, true, ::AdministrationPage)

val pages = listOf(explorePage, homePage, showPage, register, subscribePage, administrationPage)

