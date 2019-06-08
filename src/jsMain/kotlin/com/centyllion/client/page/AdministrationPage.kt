package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.EventController
import com.centyllion.client.controller.FeaturedController
import com.centyllion.client.controller.GrainModelFeaturedController
import com.centyllion.model.*
import org.w3c.dom.HTMLElement

class AdministrationPage(val context: AppContext) : BulmaElement {

    val api = context.api

    val featuredController =
        noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
        { _, featured ->
            val controller = FeaturedController(featured, ColumnSize.Half)
            val footer = CardFooter(CardFooterContentItem(Delete {
                // forces delete with this toggle
                toggleFeatured(emptyGrainModelDescription, emptySimulationDescription, featured)
            }))
            controller.body.content += footer
            controller
        }

    val publicModelsController =
        columnsController<GrainModelDescription, List<FeaturedDescription>, GrainModelFeaturedController>(
            emptyList(),
            emptyList()
        )
        { _, model ->
            val controller = GrainModelFeaturedController(model, featuredController.data, api)
            controller.toggleFeature = ::toggleFeatured
            controller
        }

    val featuredColumns = Columns(
        Column(SubTitle("Featured models"), featuredController, size = ColumnSize.Half),
        Column(SubTitle("Public models"), publicModelsController, size = ColumnSize.Half)
    )

    val eventsController =
        noContextColumnsController<Event, EventController>(emptyList())
        { _, event -> EventController(event, ColumnSize.Half) }

    val featuredPage = TabPage(TabItem("Featured", "star"), featuredColumns)

    val onTabChange: (TabPage) -> Unit = {
        when (it) {
            featuredPage -> {
                api.fetchAllFeatured().then { featuredController.data = it.content }.catch { context.error(it) }
                api.fetchPublicGrainModels().then { publicModelsController.data = it.content }.catch { context.error(it) }
            }
        }
    }

    val container: BulmaElement = TabPages(featuredPage, tabs = Tabs(boxed = true), onTabChange = onTabChange)

    override val root: HTMLElement = container.root

    fun toggleFeatured(
        model: GrainModelDescription,
        simulation: SimulationDescription,
        featured: FeaturedDescription?
    ) {
        val result = if (featured == null) {
            api.saveFeatured(model.id, simulation.id, model.info.userId).then {
                featuredController.data + it
            }
        } else {
            api.deleteFeatured(featured).then {
                featuredController.data - featured
            }
        }
        result.then {
            featuredController.data = it
            publicModelsController.context = it
        }.catch { context.error(it) }
    }

    init {
        onTabChange(featuredPage)
    }
}
