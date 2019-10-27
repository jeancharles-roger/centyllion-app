package com.centyllion.client.page

import bulma.Box
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
import bulma.Title
import bulma.Value
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
            previous ?: GrainModelFeaturedController(model, featuredController.data, this).wrap {
                it.toggleFeature = ::toggleFeatured
                Column(it.container, size = ColumnSize.Half)
            }
        }

    val featuredColumns = Columns(
        Column(SubTitle(i18n("Featured")), featuredController, size = ColumnSize.Half),
        Column(SubTitle(i18n("Public models")), publicModelsController, size = ColumnSize.Half)
    )

    val featuredPage = TabPage(TabItem(i18n("Featured"), "star"), featuredColumns)

    val monitoringBox = Box(
        Columns(
            Column(Label(i18n("Models")), Value().apply {
                api.fetchGrainModelsInfo().then {
                    this.text = i18n("Total %0, this week %1, this month %2",it.total, it.lastWeek, it.lastMonth)
                }
            }),
            Column(Label(i18n("Simulations")), Value().apply {
                api.fetchSimulationsInfo().then {
                    this.text = i18n("Total %0, this week %1, this month %2",it.total, it.lastWeek, it.lastMonth)
                }
            }),
            Column(Label(i18n("Users")), Value().apply {
                api.fetchUsersInfo().then {
                    this.text = i18n("Total %0, this week %1, this month %2",it.total, it.lastWeek, it.lastMonth)
                }
            }),
            Column(Label(i18n("Assets")), Value().apply {
                api.fetchAllAssets().then { this.text = "${it.totalSize}" }
            })
        )
    )

    val usersPageController = ResultPageController(
        appContext.locale,
        noContextColumnsController(emptyList()) { data, previous ->
            previous ?: UserAdministrationController(data, this).wrap { Column(it.container, size = ColumnSize.S6) }
        },
        { Column(it, size = ColumnSize.Full) },
        { offset, limit -> api.fetchAllUsers(true, offset, limit) }
    )

    val monitoringPage = TabPage(TabItem(i18n("Monitoring"), "chart-bar"),
        Div(monitoringBox, Title(i18n("Users")), usersPageController)
    )

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
        appContext.locale,
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
                api.fetchGrainModels().then { publicModelsController.data = it.content }.catch { error(it) }
            }
            monitoringPage -> api.fetchAllUsers(true, 0, usersPageController.limit)
                .then { usersPageController.data = it }.catch { error(it) }
            assetPage -> api.fetchAllAssets(0, assetsPageController.limit)
                .then { assetsPageController.data = it }.catch { error(it) }
        }
    }

    val container: BulmaElement = TabPages(
        monitoringPage, featuredPage, assetPage,
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
        onTabChange(monitoringPage)
    }
}
