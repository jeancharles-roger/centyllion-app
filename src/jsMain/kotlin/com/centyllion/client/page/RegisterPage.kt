package com.centyllion.client.page

import bulma.Div
import com.centyllion.client.AppContext
import com.centyllion.client.homePage
import kotlinx.browser.window

class RegisterPage(override val appContext: AppContext) : BulmaPage {

    val container = Div()

    override val root = container.root

    init {
        if (appContext.me == null) {
            window.location.href = appContext.keycloak.createRegisterUrl()
        } else {
            appContext.openPage(homePage)
        }
    }

}
