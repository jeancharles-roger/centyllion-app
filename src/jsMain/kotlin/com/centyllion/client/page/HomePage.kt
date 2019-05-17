package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.FeaturedController
import com.centyllion.client.controller.UserController
import com.centyllion.client.openPage
import com.centyllion.client.showPage
import com.centyllion.model.Description
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import org.w3c.dom.HTMLElement
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class HomePage(val context: AppContext) : BulmaElement {

    class PanelItemController(source: Description) : NoContextController<Description, PanelSimpleBlock>() {

        override var data by observable(source) { _, old, new ->
            if (new != old) refresh()
        }

        override val container = PanelSimpleBlock(data.label, data.icon).apply {
            root.classList.toggle("has-text-weight-bold", data is GrainModelDescription)
        }

        override var readOnly = false

        override fun refresh() {
            root.classList.toggle("has-text-weight-bold", data is GrainModelDescription)
            container.text = data.label
            container.icon = data.icon
        }
    }

    var elements by observable(listOf<Description>()) { _, old, new ->
        if (new != old) {
            updateElements()
        }
    }

    val userController = UserController(context.me)

    val searchInput = Input("", "Search", size = Size.Small) { _, _ ->
        updateElements()
    }

    val newModelButton = Button("Model", Icon("plus"), ElementColor.Link, size = Size.Small) {
        openPage(showPage, context)
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
        header = listOf(
            PanelContentBlock(Control(newModelButton)),
            PanelContentBlock(Control(searchInput, leftIcon = Icon("search", Size.Small))),
            PanelTabs(allTabItem, modelsTabItem, simulationsTabItem)
        )
    ) { _, data -> PanelItemController(data) }

    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { parent, data ->
        val controller = FeaturedController(data)
        controller.body.root.onclick = {
            openPage(showPage, context, mapOf("model" to data.modelId, "simulation" to data.simulationId))
        }
        controller.body.root.style.cursor = "pointer"
        controller
    }

    val container = TileAncestor(
        TileParent(TileChild(panelController), size = TileSize.S3),
        TileParent(
            TileChild(userController),
            TileChild(div(Title("Featured models"),featuredController)),
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
                    openPage(showPage, context, mapOf("model" to data.id))
                }
                is SimulationDescription -> {
                    openPage(showPage, context, mapOf("model" to data.modelId, "simulation" to data.id))
                }
            }
        }

        context.api.fetchMyGrainModels().then {
            //elements = it

            Promise.all(it.map { model ->
                context.api.fetchSimulations(model.id, false).then { listOf(model) + it }
            }.toTypedArray()).then { it -> it.flatMap { it } }.then {
                elements = it
            }

        }

        // sets callbacks for update
        userController.onUpdate = { _, new, _ ->
            if (new != null) context.api.saveUser(new) else null
        }

        // retrieves featured models
        context.api.fetchAllFeatured().then { models -> featuredController.data = models }

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
