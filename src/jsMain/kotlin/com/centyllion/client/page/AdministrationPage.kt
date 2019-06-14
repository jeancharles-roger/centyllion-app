package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.admin.GrainModelFeaturedController
import com.centyllion.client.controller.admin.UserAdministrationController
import com.centyllion.client.controller.navigation.FeaturedController
import com.centyllion.client.controller.navigation.ResultPageController
import com.centyllion.model.*
import org.w3c.dom.HTMLElement

class AdministrationPage(val context: AppContext) : BulmaElement {

    val api = context.api

    val featuredController =
        noContextColumnsController(emptyList<FeaturedDescription>())
        { _, featured, previous ->
            previous ?: FeaturedController(featured).wrap { ctrl ->
                ctrl.container.content += CardFooter(CardFooterContentItem(Delete {
                    // forces delete with this toggle
                    toggleFeatured(emptyGrainModelDescription, emptySimulationDescription, ctrl.data)
                }))
                Column(ctrl.container, size = ColumnSize.Half)
            }
        }

    val publicModelsController =
        columnsController<GrainModelDescription, List<FeaturedDescription>, GrainModelFeaturedController>(
            emptyList(),
            emptyList()
        )
        { _, model, previous ->
            val controller = previous ?: GrainModelFeaturedController(model, featuredController.data, api)
            controller.toggleFeature = ::toggleFeatured
            controller
        }

    val featuredColumns = Columns(
        Column(SubTitle("Featured models"), featuredController, size = ColumnSize.Half),
        Column(SubTitle("Public models"), publicModelsController, size = ColumnSize.Half)
    )

    val featuredPage = TabPage(TabItem("Featured", "star"), featuredColumns)

    val userController = ResultPageController(
        {_, data, previous ->
            previous ?: UserAdministrationController(data, context.api).wrap { Column(it.container, size = ColumnSize.S4) }
        },
        { offset, limit -> api.fetchAllUsers(true, offset, limit) }
    )

    val userPage = TabPage(TabItem("Users", "user"), userController)

    val onTabChange: (TabPage) -> Unit = {
        when (it) {
            featuredPage -> {
                api.fetchAllFeatured().then { featuredController.data = it.content }.catch { context.error(it) }
                api.fetchPublicGrainModels().then { publicModelsController.data = it.content }.catch { context.error(it) }
            }
            userPage -> api.fetchAllUsers(true, 0, userController.limit)
                .then { userController.data = it }.catch { context.error(it) }
        }
    }

    val container: BulmaElement = TabPages(
        featuredPage, userPage,
        tabs = Tabs(boxed = true), onTabChange = onTabChange
    )

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
