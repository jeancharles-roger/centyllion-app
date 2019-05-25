package com.centyllion.client

import bulma.*
import com.centyllion.client.controller.FeaturedController
import com.centyllion.client.controller.GrainModelDisplayController
import com.centyllion.client.controller.SimulationDisplayController
import com.centyllion.client.page.AdministrationPage
import com.centyllion.client.page.HomePage
import com.centyllion.client.page.ShowPage
import com.centyllion.common.adminRole
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import keycloak.KeycloakInstance
import kotlin.js.Promise

data class Page<T: BulmaElement>(
    val title: String, val id: String, val needUser: Boolean, val role: String?,
    val header: Boolean, val callback: (appContext: AppContext) -> T,
    val exitCallback: T.(appContext: AppContext) -> Promise<Boolean> = { _ -> Promise.resolve(true) }
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = when {
        !needUser -> true
        role == null -> keycloak.authenticated
        else -> keycloak.hasRealmRole(role)
    }
}

const val contentSelector = "section.cent-main"

val homePage = Page("Home", "home", true, null, true, ::HomePage)
val explorePage = Page("Explore", "explore", false, null, true, ::explore)
val showPage = Page("Show", "show", false, null, false, ::ShowPage, ShowPage::canExit)
val administrationPage = Page("Administration", "administration", true, adminRole, true, ::AdministrationPage)

val pages = listOf(homePage, explorePage, showPage, administrationPage)

fun explore(context: AppContext): BulmaElement {

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
    val tabs = TabPages(
        TabPage(searchSimulationTabItem, searchedSimulationController),
        TabPage(searchModelTabItem, searchedModelController)
    )

    // featured controller
    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { parent, data ->
        val controller = FeaturedController(data)
        controller.body.root.onclick = {
            context.openPage(showPage, mapOf("model" to data.modelId, "simulation" to data.simulationId))
        }
        controller.body.root.style.cursor = "pointer"
        controller
    }
    val page = div(
        Title("Search"), Box(search, tabs),
        Title("Explore featured models"), featuredController
    )

    context.api.fetchAllFeatured().then { models -> featuredController.data = models }
    return page
}
