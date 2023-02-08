package com.centyllion.client

import bulma.ElementColor
import bulma.NavBar
import com.centyllion.i18n.Locale
import com.centyllion.model.User

data class ClientEvent(
    val date: String,
    val context: String,
    val color: ElementColor
)

interface AppContext {

    val locale: Locale

    val navBar: NavBar

    val me: User?

    val api: Api

    //fun getFont(path: String): Promise<Font>
    fun i18n(key: String, vararg parameters: String) = locale.i18n(key, *parameters)

    val events: List<ClientEvent>

    fun notify(event: ClientEvent)
}
