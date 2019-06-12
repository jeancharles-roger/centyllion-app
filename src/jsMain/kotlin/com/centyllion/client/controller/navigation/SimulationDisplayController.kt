package com.centyllion.client.controller.navigation

import bulma.*
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

    val thumbnail = Image(
        if (data.thumbnailId != null) "/api/asset/${data.thumbnailId}" else "/images/480x480.png",
        ImageSize.Square
    )

    override val container = Card(
        CardImage(thumbnail),
        CardContent(name, description)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        name.text = data.name
        description.text = data.simulation.description
        thumbnail.src = if (data.thumbnailId != null) "/api/asset/${data.thumbnailId}" else "/images/480x480.png"
    }
}
