package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.SimulationDescription
import kotlin.properties.Delegates.observable

class SimulationDisplayController(
    simulationDescription: SimulationDescription, size: ColumnSize = ColumnSize.OneQuarter
) : NoContextController<SimulationDescription, Column>() {

    override var data by observable(simulationDescription) { _, old, new ->
        if (old != new)  refresh()
    }

    override var readOnly = false

    val name = SubTitle(data.simulation.name)
    val description = Label(data.simulation.description)

    fun thumbnail() = Image(
        if (data.thumbnailId != null) "/api/asset/${data.thumbnailId}" else "/images/480x480.png",
        ImageSize.Square
    )

    val body = Card(
        CardImage(thumbnail()),
        CardContent(name, description)
    ).apply {
        root.classList.add("is-outlined")
    }



    override val container = Column(body, size = size)

    override fun refresh() {
        name.text = data.name
        description.text = data.simulation.description
    }
}
