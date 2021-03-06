package com.centyllion.client.page

import bulma.Button
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.ElementColor
import bulma.Icon
import bulma.Input
import bulma.NoContextController
import bulma.Panel
import bulma.PanelContentBlock
import bulma.PanelItem
import bulma.PanelSimpleBlock
import bulma.PanelTabs
import bulma.PanelTabsItem
import bulma.Size
import bulma.Tag
import bulma.Tags
import bulma.Title
import bulma.noContextColumnsController
import bulma.noContextPanelController
import bulma.wrap
import com.centyllion.client.AppContext
import com.centyllion.client.controller.navigation.FeaturedController
import com.centyllion.client.controller.navigation.MeController
import com.centyllion.client.controller.navigation.ResultPageController
import com.centyllion.client.controller.navigation.SimulationDisplayController
import com.centyllion.client.showPage
import com.centyllion.model.Description
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import kotlinx.browser.document
import kotlinx.html.div
import kotlinx.html.dom.create
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

    val searchInput = Input("", i18n("Search"), size = Size.Small) { _, _ ->
        updateElements()
    }

    val newModelButton = Button(i18n("Model"), Icon("plus"), ElementColor.Link, size = Size.Small) {
        appContext.openPage(showPage)
    }

    val allTabItem = PanelTabsItem(i18n("all")) { activateFilter(it) }

    val modelsTabItem = PanelTabsItem(i18n("models")) { activateFilter(it) }

    val simulationsTabItem = PanelTabsItem(i18n("simulations")) { activateFilter(it) }

    fun visibleElements() = when {
        allTabItem.active -> setOf(GrainModelDescription::class, SimulationDescription::class)
        modelsTabItem.active -> setOf(GrainModelDescription::class)
        simulationsTabItem.active -> setOf(SimulationDescription::class)
        else -> setOf(GrainModelDescription::class, SimulationDescription::class)
    }

    val myTags = Tags()
    val myTagsItem = object : PanelItem {

        override val root: HTMLElement = document.create.div(classes = "panel-block")

        init {
            hidden = true
            appContext.api.fetchMyTags(0, 10).then {
                hidden = it.content.isEmpty()
                myTags.tags = it.content.map { tag ->
                    Tag(tag, rounded = true).apply {
                        root.style.cursor = "pointer"
                        root.onclick = {
                            color = if (color == ElementColor.None) ElementColor.Primary else ElementColor.None
                            updateElements()
                        }
                    }
                }
                root.appendChild(myTags.root)
            }
        }
    }

    val panelController = noContextPanelController(
        Panel(i18n("My models and simulations")), emptyList<Description>(),
        header = listOfNotNull(
            PanelContentBlock(Control(newModelButton)),
            PanelContentBlock(Control(searchInput, leftIcon = Icon("search", Size.Small))),
            myTagsItem,
            PanelTabs(allTabItem, modelsTabItem, simulationsTabItem)
        )
    ) { data, previous -> previous ?: PanelItemController(data) }

    val recentSimulationListController =
        noContextColumnsController(emptyList<SimulationDescription>())
        { data, previous ->
            previous ?: SimulationDisplayController(data, appContext.api).wrap { ctrl ->
                ctrl.root.style.cursor = "pointer"
                ctrl.root.onclick = { appContext.openPage(showPage, mapOf("simulation" to ctrl.data.id)) }
                Column(ctrl.container, size = ColumnSize.OneThird)
            }
        }

    val recentSimulationResult =
        ResultPageController(
            appContext.locale, recentSimulationListController,
            { Column(it, size = ColumnSize.Full) },
            { offset, limit ->  appContext.api.fetchSimulations(appContext.me?.id, null, offset, limit) },
            { error(it) }
        ).apply { limit = 6 }

    val featuredListController =
        noContextColumnsController(emptyList<FeaturedDescription>())
        { data, previous ->
            previous ?: FeaturedController(data).wrap { ctrl ->
                ctrl.root.style.cursor = "pointer"
                ctrl.root.onclick = { appContext.openPage(showPage, mapOf("simulation" to ctrl.data.simulationId)) }
                Column(ctrl.container, size = ColumnSize.OneThird)
            }
        }

    val featuredResult = ResultPageController(
        appContext.locale, featuredListController,
        { Column(it, size = ColumnSize.Full) },
        { offset, limit -> appContext.api.fetchAllFeatured(offset, limit) },
        { error(it) }
    ).apply { limit = 6 }

    val container = Columns(
        Column(panelController, size = ColumnSize.OneThird),
        Column(
            userController,
            Title(i18n("My Recent simulation")), recentSimulationResult,
            Title(i18n("Featured")), featuredResult,
            size = ColumnSize.TwoThirds
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

        appContext.me?.let { me ->
            appContext.api.fetchGrainModels(userId = me.id, limit = 50).then {
                elements = it.content

                it.content.forEach { model ->
                    appContext.api.fetchSimulations(appContext.me?.id, model.id, limit = 50).then {
                        val index = elements.indexOf(model)
                        val mutable = elements.toMutableList()
                        mutable.addAll(index + 1, it.content)
                        elements = mutable
                    }
                }
            }
        }

        // retrieves featured models
        recentSimulationResult.refreshFetch()
        featuredResult.refreshFetch()
    }

    private fun activateFilter(selected: PanelTabsItem) {
        allTabItem.active = false
        modelsTabItem.active = false
        simulationsTabItem.active = false
        selected.active = true
        updateElements()
    }

    private fun tagsForDescription(description: Description) = when (description) {
        is GrainModelDescription ->
            description.tags
        is SimulationDescription ->
            elements.filterIsInstance<GrainModelDescription>().find { it.id == description.modelId }?.tags ?: ""
        else -> ""
    }

    fun updateElements() {
        val search = searchInput.value
        val activesTags = myTags.tags.filter { it.color == ElementColor.Primary }.map(Tag::text)
        val visibleElements = visibleElements()
        panelController.data = elements.filter {
            visibleElements.contains(it::class) &&
            (search.isEmpty() || it.label.contains(search, true)) &&
            tagsForDescription(it).let { tags -> activesTags.all { tags.contains(it) } }
        }
    }
}
