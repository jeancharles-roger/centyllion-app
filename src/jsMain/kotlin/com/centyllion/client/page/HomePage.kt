package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.UserController
import com.centyllion.client.openPage
import com.centyllion.client.showPage
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import org.w3c.dom.HTMLElement
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class HomePage(val context: AppContext) : BulmaElement {

    class PanelItemController(source: Any) : NoContextController<Any, PanelSimpleBlock>() {

        override var data by observable(source) { _, old, new ->
            if (new != old) refresh()
        }

        override val container = PanelSimpleBlock(elementText(source), elementIcon(source))

        override var readOnly = false

        override fun refresh() {
            container.text = elementText(data)
            container.icon = elementIcon(data)
        }
    }

    var elements by observable(listOf<Any>()) { _, old, new ->
        if (new != old) {
            updateElements()
        }
    }

    val userController = UserController(context.me)

    val media = Media(
        left = listOf(Image("https://bulma.io/images/placeholders/128x128.png", ImageSize.S128)),
        center = listOf(userController)
    )

    val searchInput = Input("", "Search", size = Size.Small) { _, _ ->
        updateElements()
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
        Panel("My models and simulations"), emptyList<Any>(),
        header = listOf(
            PanelContentBlock(Control(searchInput, leftIcon = Icon("search", Size.Small))),
            PanelTabs(allTabItem, modelsTabItem, simulationsTabItem)
        )
    ) { _, data -> PanelItemController(data) }

    val container = Columns(
        Column(panelController, size = ColumnSize.OneThird),
        Column(media, size = ColumnSize.TwoThirds)
    )

    override val root: HTMLElement = container.root

    init {
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
                    (search.isEmpty() || elementText(it).contains(search, true))
        }
    }
}

private fun elementText(source: Any) = when (source) {
    is GrainModelDescription -> source.model.name
    is SimulationDescription -> source.simulation.name
    else -> source.toString()
}

private fun elementIcon(source: Any) = when (source) {
    is GrainModelDescription -> "boxes"
    is SimulationDescription -> "play"
    else -> "question"
}
