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

    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { _, featured, previous ->
        val controller = previous ?: FeaturedController(featured, ColumnSize.Half)
        val footer = CardFooter(CardFooterContentItem(Delete {
            // forces delete with this toggle
            toggleFeatured(emptyGrainModelDescription, emptySimulationDescription, featured)
        }))
        controller.body.content = controller.body.content.let { if (previous == null) it else it.dropLast(1)} + footer
        controller
    }

    val publicModelsController = columnsController<GrainModelDescription, List<FeaturedDescription>, GrainModelFeaturedController>(
        emptyList(), emptyList()
    ) { _, model, previous ->
        val controller = previous ?: GrainModelFeaturedController(
            model,
            featuredController.data,
            api
        )
        controller.toggleFeature = ::toggleFeatured
        controller
    }

    val featuredColumns = Columns(
        Column(SubTitle("Featured models"), featuredController, size = ColumnSize.Half),
        Column(SubTitle("Public models"), publicModelsController, size = ColumnSize.Half)
    )

    val eventsController = noContextColumnsController<Event, EventController>(emptyList())
    { _, event, previous -> previous ?: EventController(event, ColumnSize.Half) }

    val featuredPage = TabPage(TabItem("Featured", "star"), featuredColumns )
    val eventPage = TabPage(TabItem("Events", "bolt"), eventsController)

    val container: BulmaElement = TabPages(featuredPage, eventPage, tabs = Tabs(boxed = true)) {
       when (it) {
           featuredPage -> {
               api.fetchAllFeatured().then { featuredController.data = it}.catch { context.error(it) }
               api.fetchPublicGrainModels().then { publicModelsController.data = it}.catch { context.error(it) }
           }
           eventPage -> {
               api.fetchEvents().then { eventsController.data = it }.catch { context.error(it) }
           }
       }
    }

    override val root: HTMLElement = container.root

    fun toggleFeatured(model: GrainModelDescription, simulation: SimulationDescription, featured: FeaturedDescription?) {
        val result = if (featured == null) {
            api.saveFeatured(model._id, simulation._id, model.info.userId).then {
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
}
