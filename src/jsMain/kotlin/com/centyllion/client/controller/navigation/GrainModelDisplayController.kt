package com.centyllion.client.controller.navigation

import bulma.*
import com.centyllion.model.GrainModelDescription
import kotlin.properties.Delegates.observable

class GrainModelDisplayController(modelDescription: GrainModelDescription) :
    NoContextController<GrainModelDescription, Card>() {

    override var data by observable(modelDescription) { _, old, new ->
        if (old != new)  refresh()
    }

    override var readOnly = false

    val name = SubTitle(data.model.name)
    val description = Label(data.model.description)

    override val container = Card(
        CardContent(name, description)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        name.text = data.name
        description.text = data.model.description
    }
}
