package com.centyllion.client.controller.navigation

import bulma.*
import com.centyllion.client.Api
import com.centyllion.model.SimulationDescription
import kotlin.properties.Delegates.observable

class SimulationDisplayController(
    simulationDescription: SimulationDescription, val api: Api
) : NoContextController<SimulationDescription, Card>() {

    override var data by observable(simulationDescription) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    fun getName(): String {
        api.fetchGrainModel(data.modelId).then {
            name.text = "${data.name} / ${it.name}"
        }
        return data.name
    }

    val name = Label(getName())
    val description = Help(data.simulation.description)
    val author = Help(data.info.user?.name?.let {"by $it"} ?: "")

    val thumbnail = Image(
        data.thumbnailId?.let { "/api/simulation/${data.id}/thumbnail" }
            ?: "/images/empty_thumbnail.png",
        ImageSize.S3by2
    )

    override val container = Card(
        CardImage(thumbnail),
        CardContent(name, description, author)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        name.text =getName()
        description.text = data.simulation.description
        author.text = data.info.user?.name?.let {"by $it"} ?: ""
        thumbnail.src =
            data.thumbnailId?.let { "/api/simulation/${data.id}/thumbnail" }
                ?: "/images/empty_thumbnail.png"
    }
}
