package com.centyllion.client.page

import bulma.Div
import com.centyllion.client.AppContext
import kotlin.browser.window

class SignInPage(override val appContext: AppContext) : BulmaPage {

    val container = Div()

    override val root = container.root

    init {
        window.location.href = appContext.keycloak.createRegisterUrl()
    }

}
