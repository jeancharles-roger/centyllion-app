package com.centyllion.client.controller.admin

import bulma.Box
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Controller
import bulma.Dropdown
import bulma.DropdownSimpleItem
import bulma.Icon
import bulma.Label
import bulma.Size
import bulma.SubTitle
import bulma.TextColor
import com.centyllion.client.Api
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import kotlin.properties.Delegates.observable

class GrainModelFeaturedController(
    model: GrainModelDescription, allFeatured: List<FeaturedDescription>,
    api: Api,
    var toggleFeature: (
        simulation: SimulationDescription, featured: FeaturedDescription?
    ) -> Unit = { _, _ -> }
) : Controller<GrainModelDescription, List<FeaturedDescription>, Box> {

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
        api.fetchPublicSimulations(model.id, limit = 50).then { simulations ->
            dropdown.items = simulations.content.map { simulation ->
                DropdownSimpleItem(simulation.simulation.name, icon(simulation)) {
                    toggleFeature(simulation, featured(simulation))
                    dropdown.toggleDropdown()
                }
            }
        }
    }

    override val container = Box(name, description, dots, simulationDropDown)

    override fun refresh() {
        dots.columns = dotColumns()
        name.text = data.model.name
        description.text = data.model.description
    }
}
