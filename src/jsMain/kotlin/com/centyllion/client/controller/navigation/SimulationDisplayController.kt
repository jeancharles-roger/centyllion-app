package com.centyllion.client.controller.navigation

import bulma.Card
import bulma.CardContent
import bulma.CardImage
import bulma.Image
import bulma.ImageSize
import bulma.Label
import bulma.NoContextController
import bulma.SubTitle
import com.centyllion.model.SimulationDescription
import kotlin.properties.Delegates.observable

class SimulationDisplayController(simulationDescription: SimulationDescription) :
    NoContextController<SimulationDescription, Card>() {

    override var data by observable(simulationDescription) { _, old, new ->
        if (old != new)  refresh()
    }

    override var readOnly = false

    val name = SubTitle(data.simulation.name)
    val description = Label(data.simulation.description)

    val thumbnail = Image("/api/simulation/${data.id}/thumbnail", ImageSize.Square)

    override val container = Card(
        CardImage(thumbnail),
        CardContent(name, description)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        name.text = data.name
        description.text = data.simulation.description
        thumbnail.src = "/api/simulation/${data.id}/thumbnail"
    }
}
