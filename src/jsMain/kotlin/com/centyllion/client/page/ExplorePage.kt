package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.FeaturedController
import com.centyllion.client.controller.GrainModelDisplayController
import com.centyllion.client.controller.SimulationDisplayController
import com.centyllion.client.showPage
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription

class ExplorePage(val context: AppContext) : BulmaElement {

    val noSimulationResult = Column(SubTitle("No simulation found"), size = ColumnSize.Full)

    // Searched simulations controller
    val searchedSimulationController =
        noContextColumnsController<SimulationDescription, SimulationDisplayController>(emptyList(), header = listOf(noSimulationResult))
        { parent, data ->
            val controller = SimulationDisplayController(data)
            controller.body.root.onclick = {
                context.openPage(showPage, mapOf("model" to data.modelId, "simulation" to data.id))
            }
            controller.body.root.style.cursor = "pointer"
            controller
        }
    // searched simulations tab title
    val searchSimulationTabItem = TabItem("Simulation", "play")

    val noModelResult = Column(SubTitle("No model found"), size = ColumnSize.Full)

    // Searched models controller
    val searchedModelController =
        noContextColumnsController<GrainModelDescription, GrainModelDisplayController>(emptyList(), header = listOf(noModelResult))
        { parent, data ->
            val controller = GrainModelDisplayController(data)
            controller.body.root.onclick = { context.openPage(showPage, mapOf("model" to data.id)) }
            controller.body.root.style.cursor = "pointer"
            controller
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
            searchSimulationTabItem.text = "Simulation (${it.size})"
            searchedSimulationController.header = if (it.isEmpty()) listOf(noSimulationResult) else emptyList()
            searchedSimulationController.data = it
        }.catch { context.error(it) }
        context.api.searchModel(value).then {
            searchModelTabItem.text = "Model (${it.size})"
            searchedModelController.header = if (it.isEmpty()) listOf(noModelResult) else emptyList()
            searchedModelController.data = it
        }.catch { context.error(it) }
    }

    val search = Field(Control(searchInput, Icon("search")))

    // tabs for search results
    val searchTabs = TabPages(
        TabPage(searchSimulationTabItem, searchedSimulationController),
        TabPage(searchModelTabItem, searchedModelController)
    )

    // featured controller
    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { _, data ->
        val controller = FeaturedController(data)
        controller.body.root.onclick = {
            context.openPage(showPage, mapOf("model" to data.modelId, "simulation" to data.simulationId))
        }
        controller.body.root.style.cursor = "pointer"
        controller
    }


    val featuredTabItem = TabItem("Featured", "star") {
        context.api.fetchAllFeatured().then { featuredController.data = it.content }
    }

    val recentController =
        noContextColumnsController<SimulationDescription, SimulationDisplayController>(emptyList())
        { _, data ->
            val controller = SimulationDisplayController(data)
            controller.body.root.onclick = {
                context.openPage(showPage, mapOf("model" to data.modelId, "simulation" to data.id))
            }
            controller.body.root.style.cursor = "pointer"
            controller
        }

    val recentTabItem = TabItem("Recent", "play") {
        println("Fetch recent simulations")
        context.api.fetchPublicSimulations().then { recentController.data = it.content }
    }

    val exploreTabs = TabPages(
        TabPage(featuredTabItem, featuredController),
        TabPage(recentTabItem, recentController)
    )

    val container = div(
        Title("Search"), Box(search, searchTabs),
        Title("Explore"), exploreTabs
    )

    override val root = container.root

    init {
        context.api.fetchAllFeatured().then { featuredController.data = it.content }
        context.api.fetchPublicSimulations().then { recentController.data = it.content }
    }

}
