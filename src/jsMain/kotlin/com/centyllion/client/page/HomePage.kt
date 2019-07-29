package com.centyllion.client.page

import bulma.Button
import bulma.Column
import bulma.ColumnSize
import bulma.Control
import bulma.Div
import bulma.ElementColor
import bulma.Icon
import bulma.Input
import bulma.NoContextController
import bulma.Panel
import bulma.PanelContentBlock
import bulma.PanelSimpleBlock
import bulma.PanelTabs
import bulma.PanelTabsItem
import bulma.Size
import bulma.TileAncestor
import bulma.TileChild
import bulma.TileParent
import bulma.TileSize
import bulma.Title
import bulma.noContextColumnsController
import bulma.noContextPanelController
import bulma.wrap
import com.centyllion.client.AppContext
import com.centyllion.client.controller.navigation.FeaturedController
import com.centyllion.client.controller.navigation.MeController
import com.centyllion.client.showPage
import com.centyllion.common.creatorRole
import com.centyllion.model.Description
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable

class HomePage(override val appContext: AppContext) : BulmaPage {

    class PanelItemController(source: Description) : NoContextController<Description, PanelSimpleBlock>() {

        override var data by observable(source) { _, old, new ->
            if (new != old) refresh()
        }

        override val container = PanelSimpleBlock(data.label, data.icon).apply {
            root.classList.toggle("has-text-weight-bold", data is GrainModelDescription)
            root.style.paddingLeft = if (data is SimulationDescription) "2rem" else ""
        }

        override var readOnly = false

        override fun refresh() {
            root.classList.toggle("has-text-weight-bold", data is GrainModelDescription)
            root.style.paddingLeft = if (data is SimulationDescription) "2rem" else ""
            container.text = data.label
            container.icon = data.icon
        }
    }

    var elements by observable(listOf<Description>()) { _, old, new ->
        if (new != old) {
            updateElements()
        }
    }

    val userController = MeController(appContext)

    val searchInput = Input("", "Search", size = Size.Small) { _, _ ->
        updateElements()
    }

    val newModelButton = Button("Model", Icon("plus"), ElementColor.Link, size = Size.Small) {
        appContext.openPage(showPage)
    }

    val allTabItem = PanelTabsItem("all") { activateFilter(it) }

    val modelsTabItem = PanelTabsItem("models") { activateFilter(it) }

    val simulationsTabItem = PanelTabsItem("simulations") { activateFilter(it) }

    fun visibleElements() = when {
        allTabItem.active -> setOf(GrainModelDescription::class, SimulationDescription::class)
        modelsTabItem.active -> setOf(GrainModelDescription::class)
        simulationsTabItem.active -> setOf(SimulationDescription::class)
        else -> setOf(GrainModelDescription::class, SimulationDescription::class)
    }

    val panelController = noContextPanelController(
        Panel("My models and simulations"), emptyList<Description>(),
        header = listOfNotNull(
            if (appContext.hasRole(creatorRole)) PanelContentBlock(Control(newModelButton)) else null,
            PanelContentBlock(Control(searchInput, leftIcon = Icon("search", Size.Small))),
            PanelTabs(allTabItem, modelsTabItem, simulationsTabItem)
        )
    ) { data, previous -> previous ?: PanelItemController(data) }

    val featuredController = noContextColumnsController(emptyList<FeaturedDescription>())
    { data, previous ->
        previous ?: FeaturedController(data).wrap { ctrl ->
            ctrl.container.root.onclick = {
                appContext.openPage(showPage, mapOf("model" to data.modelId, "simulation" to data.simulationId))
            }
            ctrl.root.style.cursor = "pointer"
            Column(ctrl.container, size = ColumnSize.OneThird)
        }
    }

    val container = TileAncestor(
        TileParent(TileChild(panelController), size = TileSize.S3),
        TileParent(
            TileChild(userController),
            TileChild(Div(Title("Featured models"), featuredController)),
            size = TileSize.S9, vertical = true
        )
    )

    override val root: HTMLElement = container.root

    init {
        // Makes new model button full panel width
        newModelButton.root.classList.add("is-fullwidth")

        allTabItem.active = true

        panelController.onClick = { data, _ ->
            when (data) {
                is GrainModelDescription -> {
                    appContext.openPage(showPage, mapOf("model" to data.id))
                }
                is SimulationDescription -> {
                    appContext.openPage(showPage, mapOf("model" to data.modelId, "simulation" to data.id))
                }
            }
        }

        appContext.api.fetchMyGrainModels().then {
            elements = it

            it.forEach { model ->
                appContext.api.fetchSimulations(model.id, false).then {
                    val index = elements.indexOf(model)
                    val mutable = elements.toMutableList()
                    mutable.addAll(index + 1, it)
                    elements = mutable
                }
            }
        }

        // retrieves featured models
        appContext.api.fetchAllFeatured().then { models -> featuredController.data = models.content }

    }

    private fun activateFilter(selected: PanelTabsItem) {
        allTabItem.active = false
        modelsTabItem.active = false
        simulationsTabItem.active = false
        selected.active = true
        updateElements()
    }

    fun updateElements() {
        val search = searchInput.value
        val visibleElements = visibleElements()
        panelController.data = elements.filter {
            visibleElements.contains(it::class) &&
                    (search.isEmpty() || it.label.contains(search, true))
        }
    }
}
