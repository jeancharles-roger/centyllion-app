package com.centyllion.client.controller.navigation

import bulma.Card
import bulma.CardContent
import bulma.CardImage
import bulma.Help
import bulma.Image
import bulma.ImageSize
import bulma.Label
import bulma.NoContextController
import com.centyllion.model.SimulationDescription
import kotlin.properties.Delegates.observable

class SimulationDisplayController(simulationDescription: SimulationDescription) :
    NoContextController<SimulationDescription, Card>() {

    override var data by observable(simulationDescription) { _, old, new ->
        if (old != new)  refresh()
    }

    override var readOnly = false

    val name = Label(data.simulation.name)
    val description = Help(data.simulation.description)
    val author = Help(data.info.user?.name?.let {"by $it"} ?: "")

    val thumbnail = Image("/api/simulation/${data.id}/thumbnail", ImageSize.Square)

    override val container = Card(
        CardImage(thumbnail),
        CardContent(name, description, author)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        name.text = data.name
        description.text = data.simulation.description
        author.text = data.info.user?.name?.let {"by $it"} ?: ""
        thumbnail.src = "/api/simulation/${data.id}/thumbnail"
    }
}
