package com.centyllion.client.controller.admin

import bulma.*
import com.centyllion.client.Api
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import kotlin.properties.Delegates.observable

class GrainModelFeaturedController(
    model: GrainModelDescription, allFeatured: List<FeaturedDescription>,
    api: Api, size: ColumnSize = ColumnSize.Half,
    var toggleFeature: (
        model: GrainModelDescription, simulation: SimulationDescription, featured: FeaturedDescription?
    ) -> Unit = { _, _, _ -> }
) : Controller<GrainModelDescription, List<FeaturedDescription>, Column> {

    override var data by observable(model)
    { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    override var context: List<FeaturedDescription> = allFeatured

    private fun dotColumns() =
        data.model.grains.map {
            Column(Icon("circle").apply { root.style.color = it.color }, size = ColumnSize.S1)
        }

    val dots = Columns(multiline = true, mobile = true).apply { columns = dotColumns() }

    val name = SubTitle(data.model.name)
    val description = Label(data.model.description)

    fun featured(simulation: SimulationDescription) = context.find {
        it.modelId == data.id && it.simulationId == simulation.id
    }

    fun icon(simulation: SimulationDescription): Icon {
        return (featured(simulation) != null).let {
            Icon(
                "star", color = if (it) TextColor.Primary else TextColor.GreyLight,
                size = if (it) Size.None else Size.Small
            )
        }
    }

    val simulationDropDown = Dropdown(text = "Simulations", rounded = true, icon = Icon("play")) { dropdown ->
        dropdown.items = listOf(DropdownSimpleItem("Loading", Icon("sync", spin = true)))
        api.fetchSimulations(model.id, true).then { simulations ->
            dropdown.items = simulations.map { simulation ->
                DropdownSimpleItem(simulation.simulation.name, icon(simulation)) {
                    toggleFeature(model, simulation, featured(simulation))
                    dropdown.toggleDropdown()
                }
            }
        }
    }

    val body = Media(center = listOf(name, description, dots, simulationDropDown)).apply {
        root.classList.add("is-outlined")
    }

    override val container = Column(body, size = size)

    override fun refresh() {
        dots.columns = dotColumns()
        name.text = data.model.name
        description.text = data.model.description
    }
}
