package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.FeaturedController
import com.centyllion.client.controller.GrainModelDisplayController
import com.centyllion.client.controller.ResultPageController
import com.centyllion.client.controller.SimulationDisplayController
import com.centyllion.client.showPage
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription

class ExplorePage(val context: AppContext) : BulmaElement {

    val noSimulationResult = Column(SubTitle("No simulation found"), size = ColumnSize.Full)

    // Searched simulations controller
    val searchedSimulationController =
        noContextColumnsController<SimulationDescription, SimulationDisplayController>(
            initialList = emptyList(),
            header = listOf(noSimulationResult)
        ) { parent, data, previous ->
            previous ?: SimulationDisplayController(data).apply { root.style.cursor = "pointer" }
        }

    // searched simulations tab title
    val searchSimulationTabItem = TabItem("Simulation", "play")

    val noModelResult = Column(SubTitle("No model found"), size = ColumnSize.Full)

    // Searched models controller
    val searchedModelController =
        noContextColumnsController<GrainModelDescription, GrainModelDisplayController>(
            initialList = emptyList(),
            header = listOf(noModelResult)
        ) { parent, data, previous ->
            previous ?: GrainModelDisplayController(data).apply { root.style.cursor = "pointer" }
        }

    // searched modes tab title
    val searchModelTabItem = TabItem("Models", "boxes")

    // search input
    val searchInput = Input("", "Search", rounded = true) { _, value ->
        searchSimulationTabItem.text = "Simulation"
        searchModelTabItem.text = "Model"
        searchedModelController.data = emptyList()
        searchedSimulationController.data = emptyList()

        context.api.searchSimulation(value).then {
            searchSimulationTabItem.text = "Simulation (${it.totalSize})"
            searchedSimulationController.header = if (it.content.isEmpty()) listOf(noSimulationResult) else emptyList()
            searchedSimulationController.data = it.content
        }.catch { context.error(it) }

        context.api.searchModel(value).then {
            searchModelTabItem.text = "Model (${it.totalSize})"
            searchedModelController.header = if (it.content.isEmpty()) listOf(noModelResult) else emptyList()
            searchedModelController.data = it.content
        }.catch { context.error(it) }
    }

    val search = Field(Control(searchInput, Icon("search")))

    // tabs for search results
    val searchTabs = TabPages(
        TabPage(searchSimulationTabItem, searchedSimulationController),
        TabPage(searchModelTabItem, searchedModelController)
    )

    val recentResult = ResultPageController<SimulationDescription, SimulationDisplayController>(
        { _, data, previous -> previous ?: SimulationDisplayController(data).apply { root.style.cursor = "pointer" } },
        { offset, limit ->  context.api.fetchPublicSimulations(offset, limit) },
        { simulation, _ -> context.openPage(showPage, mapOf("simulation" to simulation.id)) },
        { context.error(it)}
    )

    val featuredResult = ResultPageController<FeaturedDescription, FeaturedController>(
        { _, data, previous -> previous ?: FeaturedController(data).apply { root.style.cursor = "pointer" } },
        { offset, limit ->  context.api.fetchAllFeatured(offset, limit) },
        { featured , _  -> context.openPage(showPage, mapOf("simulation" to featured.simulationId)) },
        { context.error(it)}
    )

    val container = div(
        Title("Search"), Box(search, searchTabs),
        Title("Recent simulations"), Box(recentResult),
        Title("Featured"), Box(featuredResult)
    )

    override val root = container.root

    init {
        searchedSimulationController.onClick = { simulation, _ ->
            context.openPage(showPage, mapOf("simulation" to simulation.id))
        }
        searchedModelController.onClick = { model, _ ->
            context.openPage(showPage, mapOf("model" to model.id))
        }

        context.api.fetchAllFeatured(0, featuredResult.limit).then { featuredResult.data = it }
        context.api.fetchPublicSimulations(0, recentResult.limit).then { recentResult.data = it }
    }

}
