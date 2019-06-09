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

    // featured controller
    val featuredController =
        noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
        { _, data, previous ->
            previous ?: FeaturedController(data).apply { root.style.cursor = "pointer" }
        }


    val featuredTabItem = TabItem("Featured", "star") {
        context.api.fetchAllFeatured().then { featuredController.data = it.content }
    }

    val recentLimit = 8

    var recentOffset by Delegates.observable(0) { _, old, new ->
        if (old != new) context.api.fetchPublicSimulations(new, recentLimit).then { updateRecent(it) }
    }

    val recentController = noContextColumnsController<SimulationDescription, SimulationDisplayController>(emptyList())
    { _, data, previous -> previous ?: SimulationDisplayController(data).apply { root.style.cursor = "pointer" } }

    private fun updateRecent(result: ResultPage<SimulationDescription>) {
        recentController.data = result.content
        recentPagination.items = (0..result.totalSize / recentLimit).map { page ->
            val pageOffset = page * recentLimit
            PaginationLink("$page", current = (pageOffset == result.offset)) { recentOffset = pageOffset }
        }
        previous.disabled = recentOffset == 0
        next.disabled = recentOffset > result.totalSize - recentLimit
    }

    val next = PaginationAction("Next") {

    }
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
        featuredController.onClick = { featured , _  ->
            context.openPage(showPage, mapOf("simulation" to featured.simulationId))
        }
        searchedSimulationController.onClick = { simulation, _ ->
            context.openPage(showPage, mapOf("simulation" to simulation.id))
        }
        searchedModelController.onClick = { model, _ ->
            context.openPage(showPage, mapOf("model" to model.id))
        }
        recentController.onClick = { simulation, _ ->
            context.openPage(showPage, mapOf("simulation" to simulation.id))
        }

        context.api.fetchAllFeatured().then { featuredController.data = it.content }
        context.api.fetchPublicSimulations(recentOffset, recentLimit).then { updateRecent(it) }
    }

}
