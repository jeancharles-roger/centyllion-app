package com.centyllion.client

import bulma.ElementColor
import com.centyllion.i18n.Locale

data class ClientEvent(
    val date: String,
    val context: String,
    val color: ElementColor
)

interface AppContext {

    val locale: Locale

    val api: Api

    //fun getFont(path: String): Promise<Font>
    fun i18n(key: String, vararg parameters: String) = locale.i18n(key, *parameters)

    val events: List<ClientEvent>

    fun notify(event: ClientEvent)
}
