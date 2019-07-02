package com.centyllion.client.page

import bulma.Box
import bulma.BulmaElement
import bulma.Column
import bulma.ColumnSize
import bulma.Control
import bulma.Div
import bulma.Field
import bulma.Icon
import bulma.Input
import bulma.SubTitle
import bulma.TabItem
import bulma.TabPage
import bulma.TabPages
import bulma.Title
import bulma.iconButton
import bulma.noContextColumnsController
import bulma.wrap
import com.centyllion.client.AppContext
import com.centyllion.client.controller.navigation.FeaturedController
import com.centyllion.client.controller.navigation.GrainModelDisplayController
import com.centyllion.client.controller.navigation.ResultPageController
import com.centyllion.client.controller.navigation.SimulationDisplayController
import com.centyllion.client.showPage
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription

class ExplorePage(val context: AppContext) : BulmaElement {

    val noSimulationResult = Column(SubTitle("No simulation found"), size = ColumnSize.Full)

    // Searched simulations controller
    val searchedSimulationController =
        noContextColumnsController(
            initialList = emptyList<SimulationDescription>(),
            header = listOf(noSimulationResult)
        ) { data, previous ->
            previous ?: SimulationDisplayController(data).wrap {
                it.root.style.cursor = "pointer"
                Column(it.container, size = ColumnSize.OneQuarter)
            }
        }

    // searched simulations tab title
    val searchSimulationTabItem = TabItem("Simulation", "play")

    val noModelResult = Column(SubTitle("No model found"), size = ColumnSize.Full)

    // Searched models controller
    val searchedModelController =
        noContextColumnsController(
            initialList = emptyList<GrainModelDescription>(),
            header = listOf(noModelResult)
        ) { data, previous ->
            previous ?: GrainModelDisplayController(data).wrap {
                it.root.style.cursor = "pointer"
                Column(it.container, size = ColumnSize.OneQuarter)
            }
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

    val clearSearch = iconButton(Icon("times"), rounded = true) { searchInput.value = "" }

    val search = Field(Control(searchInput, Icon("search"), expanded = true), Control(clearSearch), addons = true)

    // tabs for search results
    val searchTabs = TabPages(
        TabPage(searchSimulationTabItem, searchedSimulationController),
        TabPage(searchModelTabItem, searchedModelController)
    )

    val recentResult = ResultPageController(
        { data, previous ->
            previous ?: SimulationDisplayController(data).wrap {
                it.root.style.cursor = "pointer"
                Column(it.container, size = ColumnSize.OneQuarter)
            }
        },
        { offset, limit ->  context.api.fetchPublicSimulations(offset, limit) },
        { simulation, _ -> context.openPage(showPage, mapOf("simulation" to simulation.id)) },
        { context.error(it)}
    )

    val featuredResult = ResultPageController(
        { data, previous ->
            previous ?: FeaturedController(data).wrap {
                it.root.style.cursor = "pointer"
                Column(it.container, size = ColumnSize.OneQuarter)
            }
        },
        { offset, limit ->  context.api.fetchAllFeatured(offset, limit) },
        { featured , _  -> context.openPage(showPage, mapOf("simulation" to featured.simulationId)) },
        { context.error(it)}
    )

    val container = Div(
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
