package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Button
import bulma.CardFooter
import bulma.CardFooterContentItem
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Delete
import bulma.Div
import bulma.ElementColor
import bulma.FileInput
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.SubTitle
import bulma.TabItem
import bulma.TabPage
import bulma.TabPages
import bulma.Tabs
import bulma.columnsController
import bulma.noContextColumnsController
import bulma.wrap
import com.centyllion.client.AppContext
import com.centyllion.client.controller.admin.AssetAdministrationController
import com.centyllion.client.controller.admin.GrainModelFeaturedController
import com.centyllion.client.controller.admin.UserAdministrationController
import com.centyllion.client.controller.navigation.FeaturedController
import com.centyllion.client.controller.navigation.ResultPageController
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import com.centyllion.model.emptySimulationDescription
import org.w3c.dom.HTMLElement
import org.w3c.files.get

class AdministrationPage(override val appContext: AppContext) : BulmaPage {

    val api = appContext.api

    val featuredController =
        noContextColumnsController(emptyList<FeaturedDescription>())
        { featured, previous ->
            previous ?: FeaturedController(featured).wrap { ctrl ->
                ctrl.container.content += CardFooter(CardFooterContentItem(Delete {
                    // forces delete with this toggle
                    toggleFeatured(emptySimulationDescription, ctrl.data)
                }))
                Column(ctrl.container, size = ColumnSize.Half)
            }
        }

    val publicModelsController =
        columnsController(emptyList<GrainModelDescription>(), emptyList<FeaturedDescription>())
        { model, previous ->
            previous ?: GrainModelFeaturedController(model, featuredController.data, api).wrap {
                it.toggleFeature = ::toggleFeatured
                Column(it.container, size = ColumnSize.Half)
            }
        }

    val featuredColumns = Columns(
        Column(SubTitle(i18n("Featured models")), featuredController, size = ColumnSize.Half),
        Column(SubTitle(i18n("Public models")), publicModelsController, size = ColumnSize.Half)
    )

    val featuredPage = TabPage(TabItem(i18n("Featured"), "star"), featuredColumns)

    val usersPageController = ResultPageController(
        noContextColumnsController(emptyList()) { data, previous ->
            previous ?: UserAdministrationController(data, this).wrap { Column(it.container, size = ColumnSize.S6) }
        },
        { Column(it, size = ColumnSize.Full) },
        { offset, limit -> api.fetchAllUsers(true, offset, limit) }
    )

    val userPage = TabPage(TabItem(i18n("Users"), "user"), usersPageController)

    val assetInput: FileInput = FileInput(i18n("Asset")) { input, files ->
        val file = files?.get(0)
        input.fileName = file?.name ?: ""
        assetSend.disabled = file == null
        assetSendResult.text = ""
    }

    val assetSend = Button(
        i18n("Send"), Icon("upload"), ElementColor.Primary, rounded = true, disabled = true
    ) { button ->
        button.disabled = true
        button.loading = true
        assetInput.files?.get(0)?.let { file ->
            api.createAsset(file.name, file)
                .then {
                    button.loading = false
                    assetsPageController.refreshFetch()
                    assetSendResult.text = i18n("Asset created with id %0.", it)
                }
                .catch {
                    button.loading = false
                    assetSendResult.text = it.message ?: "error"
                    error(it)
                }
        }
    }

    val assetSendResult = Label()

    val assetsPageController = ResultPageController(
        noContextColumnsController(emptyList()) { data, previous ->
            previous ?: AssetAdministrationController(data).wrap { Column(it.container, size = ColumnSize.S3) }
        },
        { Column(it, size = ColumnSize.Full) },
        { offset, limit -> api.fetchAllAssets(offset, limit) }
    )

    val assetPage = TabPage(
        TabItem(i18n("Assets"), "file-code"),
        Div(Level(center = listOf(assetInput, assetSend, assetSendResult)), assetsPageController)
    )

    val onTabChange: (TabPage) -> Unit = {
        when (it) {
            featuredPage -> {
                api.fetchAllFeatured().then { featuredController.data = it.content }.catch { error(it) }
                api.fetchPublicGrainModels().then { publicModelsController.data = it.content }.catch { error(it) }
            }
            userPage -> api.fetchAllUsers(true, 0, usersPageController.limit)
                .then { usersPageController.data = it }.catch { error(it) }
            assetPage -> api.fetchAllAssets(0, assetsPageController.limit)
                .then { assetsPageController.data = it }.catch { error(it) }
        }
    }

    val container: BulmaElement = TabPages(
        featuredPage, userPage, assetPage,
        tabs = Tabs(boxed = true), onTabChange = onTabChange
    )

    override val root: HTMLElement = container.root

    fun toggleFeatured(
        simulation: SimulationDescription,
        featured: FeaturedDescription?
    ) {
        val result = if (featured == null) {
            api.saveFeatured(simulation.id).then {
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
        }.catch { error(it) }
    }

    init {
        onTabChange(featuredPage)
    }
}
