package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.FeaturedController
import com.centyllion.client.controller.GrainModelDisplayController
import com.centyllion.client.controller.SimulationDisplayController
import com.centyllion.client.showPage
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ResultPage
import com.centyllion.model.SimulationDescription
import kotlin.properties.Delegates

class ExplorePage(val context: AppContext) : BulmaElement {

    val noSimulationResult = Column(SubTitle("No simulation found"), size = ColumnSize.Full)

    // Searched simulations controller
    val searchedSimulationController =
        noContextColumnsController<SimulationDescription, SimulationDisplayController>(
            emptyList(),
            header = listOf(noSimulationResult)
        )
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
        noContextColumnsController<GrainModelDescription, GrainModelDisplayController>(
            emptyList(),
            header = listOf(noModelResult)
        )
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

    val recentLimit = 8

    var recentOffset by Delegates.observable(0) { _, old, new ->
        if (old != new) context.api.fetchPublicSimulations(new, recentLimit).then { updateRecent(it) }
    }

    val recentController =
        noContextColumnsController<SimulationDescription, SimulationDisplayController>(emptyList())
        { _, data ->
            val controller = SimulationDisplayController(data)
            controller.body.root.style.cursor = "pointer"
            controller
        }

    private fun updateRecent(result: ResultPage<SimulationDescription>) {
        recentController.data = result.content
        recentPagination.items = (0..result.totalSize / recentLimit).map {page ->
            val pageOffset = page * recentLimit
            PaginationLink("$page", current = (pageOffset == result.offset) ) { recentOffset = pageOffset }
        }
    }

    val next = PaginationAction("Next")
    val previous = PaginationAction("Previous")

    val recentPagination = Pagination(previous = previous, next = next, rounded = true)

    val recentTabItem = TabItem("Recent", "play")

    val exploreTabs = TabPages(
        TabPage(featuredTabItem, featuredController),
        TabPage(recentTabItem, div(recentPagination, recentController))
    )

    val container = div(
        Title("Search"), Box(search, searchTabs),
        Title("Explore"), exploreTabs
    )

    override val root = container.root

    init {

        recentController.onClick = { simulation, _ ->
            context.openPage(showPage, mapOf("model" to simulation.modelId, "simulation" to simulation.id))
        }

        context.api.fetchAllFeatured().then { featuredController.data = it.content }
        context.api.fetchPublicSimulations(recentOffset, recentLimit).then { updateRecent(it) }
    }

}
