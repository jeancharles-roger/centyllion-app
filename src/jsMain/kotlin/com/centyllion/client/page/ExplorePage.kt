package com.centyllion.client.page

import bulma.Box
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
import com.centyllion.client.controller.model.TagsController
import com.centyllion.client.controller.navigation.FeaturedController
import com.centyllion.client.controller.navigation.GrainModelDisplayController
import com.centyllion.client.controller.navigation.ResultPageController
import com.centyllion.client.controller.navigation.SimulationDisplayController
import com.centyllion.client.showPage
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription

class ExplorePage(override val appContext: AppContext) : BulmaPage {

    val noSimulationResult = Column(SubTitle(i18n("No simulation found")), size = ColumnSize.Full)

    // Searched simulations controller
    val searchedSimulationController =
        noContextColumnsController(
            initialList = emptyList<SimulationDescription>(),
            header = listOf(noSimulationResult)
        ) { data, previous ->
            previous ?: SimulationDisplayController(data, appContext.api).wrap {
                it.root.style.cursor = "pointer"
                Column(it.container, size = ColumnSize.OneQuarter)
            }
        }

    val searchedSimulationResult = ResultPageController(
        appContext.locale, searchedSimulationController,
        { Column(it, size = ColumnSize.Full) },
        { offset, limit ->
            appContext.api.searchSimulation(searchInput.value, offset, limit).then {
                searchSimulationTabItem.text = "${i18n("Simulation")} (${it.totalSize})"
                noSimulationResult.hidden = it.totalSize > 0
                it
            }
        },
        { error(it) },
        initialLimit = 8
    )

    // searched simulations tab title
    val searchSimulationTabItem = TabItem(i18n("Simulations"), "play")

    val noModelResult = Column(SubTitle(i18n("No model found")), size = ColumnSize.Full)

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

    val searchedModelResult = ResultPageController(
        appContext.locale, searchedModelController,
        { Column(it, size = ColumnSize.Full) },
        { offset, limit ->
            appContext.api.searchModel(searchInput.value, tagsController.tags, offset, limit).then {
                searchModelTabItem.text = "${i18n("Model")} (${it.totalSize})"
                noModelResult.hidden = it.totalSize > 0
                it
            }
        },
        { error(it) },
        initialLimit = 8
    )
    // searched modes tab title
    val searchModelTabItem = TabItem(i18n("Models"), "boxes")

    // search input
    val searchInput: Input = Input("", i18n("Search"), rounded = true) { _, value ->
        searchSimulationTabItem.text = i18n("Simulation")
        searchModelTabItem.text = i18n("Model")
        searchedSimulationResult.refreshFetch()
        searchedModelResult.refreshFetch()
    }

    val clearSearch = iconButton(Icon("times"), rounded = true) { searchInput.value = "" }

    val search = Field(Control(searchInput, Icon("search"), expanded = true), Control(clearSearch), addons = true)

    val tagsController: TagsController = TagsController("", appContext) { old, new, _ ->
        if (old != new) searchedModelResult.refreshFetch()
    }

    // tabs for search results
    val searchTabs = TabPages(
        TabPage(searchSimulationTabItem, searchedSimulationResult),
        TabPage(searchModelTabItem, searchedModelController)
    )

    val recentListController =
        noContextColumnsController(emptyList<SimulationDescription>())
        { data, previous ->
            previous ?: SimulationDisplayController(data, appContext.api).wrap { ctrl ->
                ctrl.root.style.cursor = "pointer"
                ctrl.root.onclick = { appContext.openPage(showPage, mapOf("simulation" to ctrl.data.id)) }
                Column(ctrl.container, size = ColumnSize.OneQuarter)
            }
        }

    val recentResult =
        ResultPageController(
            appContext.locale, recentListController,
            { Column(it, size = ColumnSize.Full) },
            { offset, limit ->  appContext.api.fetchSimulationsSelection(offset, limit) },
            { error(it) }
        )

    val featuredListController =
        noContextColumnsController(emptyList<FeaturedDescription>())
        { data, previous ->
            previous ?: FeaturedController(data).wrap { ctrl ->
                ctrl.root.style.cursor = "pointer"
                ctrl.root.onclick = { appContext.openPage(showPage, mapOf("simulation" to ctrl.data.simulationId)) }
                Column(ctrl.container, size = ColumnSize.OneQuarter)
            }
        }

    val featuredResult = ResultPageController(
        appContext.locale, featuredListController,
        { Column(it, size = ColumnSize.Full) },
        { offset, limit -> appContext.api.fetchAllFeatured(offset, limit) },
        { error(it) }
    )

    val container = Div(
        Title(i18n("Search")), Box(search, tagsController, searchTabs),
        Title(i18n("Recent simulations")), Box(recentResult),
        Title(i18n("Featured")), Box(featuredResult)
    )

    override val root = container.root

    init {
        searchedSimulationController.onClick = { simulation, _ ->
            appContext.openPage(showPage, mapOf("simulation" to simulation.id))
        }
        searchedModelController.onClick = { model, _ ->
            appContext.openPage(showPage, mapOf("model" to model.id))
        }

        recentResult.refreshFetch()
        featuredResult.refreshFetch()
    }

}
