package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.client.*
import com.centyllion.model.Event
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import org.w3c.dom.HTMLElement

class AdministrationPage(val instance: KeycloakInstance) : BulmaElement {

    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { _, featured, previous ->  previous ?: FeaturedController(featured) }

    val publicModelsController = columnsController<GrainModelDescription, List<FeaturedDescription>, GrainModelFeaturedController>(
        emptyList(), emptyList()
    ) { _, model, previous ->
        val controller = previous ?: GrainModelFeaturedController(model, featuredController.data, instance)
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
               fetchAllFeatured(instance).then { featuredController.data = it}
               fetchPublicGrainModels(instance).then { publicModelsController.data = it}
           }
           eventPage -> {
               fetchEvents(instance).then { eventsController.data = it }
           }
       }
    }

    override val root: HTMLElement = container.root

    fun toggleFeatured(model: GrainModelDescription, simulation: SimulationDescription, featured: FeaturedDescription?) {
        val result = if (featured == null) {
            saveFeatured(model._id, simulation._id, model.info.userId, instance).then {
                featuredController.data + it
            }
        } else {
            deleteFeatured(featured, instance).then {
                featuredController.data - featured
            }
        }
        result.then {
            featuredController.data = it
            publicModelsController.context = it
        }
    }
}
