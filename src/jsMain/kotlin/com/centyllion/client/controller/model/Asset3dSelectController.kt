package com.centyllion.client.controller.model

import bulma.Control
import bulma.Dropdown
import bulma.DropdownContentItem
import bulma.DropdownItem
import bulma.DropdownSimpleItem
import bulma.Field
import bulma.Icon
import bulma.Input
import bulma.NoContextController
import bulma.noContextDropdownController
import com.centyllion.client.AppContext
import com.centyllion.client.controller.navigation.ResultPageController
import com.centyllion.model.ResultPage
import kotlin.properties.Delegates.observable

fun shortUrl(url: String) = url.match("/api/asset/([a-z0-9-]+)/(.*)")?.get(2) ?: url

class Asset3dSelectController(
    url: String, appContext: AppContext,
    var onUpdate: (old: String, new: String, controller: Asset3dSelectController) -> Unit = { _, _, _ -> }
) : NoContextController<String, Dropdown>() {


    class AssetItem(url: String, parent: Asset3dSelectController): NoContextController<String, DropdownItem>() {

        override var data: String by observable(url) { _, old, new ->
            if (old != new) refresh()
        }


        override val container = DropdownSimpleItem(shortUrl(data)) { _ ->
            parent.data = data
            parent.dropdown.active = false
        }

        override var readOnly: Boolean = false

        override fun refresh() {
            container.text = shortUrl(data)
        }
    }

    override var data by observable(url) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@Asset3dSelectController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            container.disabled = readOnly
        }
    }

    val search = Input(placeholder = "Search", rounded = true) { _, _ -> refresh() }

    val dropdown: Dropdown = Dropdown(
        DropdownContentItem(Field(Control(search, Icon("search")), grouped = true)),
        text = url, rounded = true, menuWidth = "30rem"
    ) { assetsPageController.refreshFetch() }

    val urlController =
        noContextDropdownController(emptyList<String>(), dropdown)
        { asset, _ -> AssetItem(asset, this) }

    val assetsPageController = ResultPageController(
        appContext.locale, urlController, { DropdownContentItem(it) },
        { offset, limit ->
            appContext.api.fetchAllAssets(offset, limit, "gltf", "glb", "zip").then {
                val all = it.content.flatMap {
                    val base = "/api/asset/${it.id}/${it.name}"
                    (it.entries.map { "$base/$it" } + base).filter { it.endsWith(".gltf") || it.endsWith(".glb")}
                }
                ResultPage(all, offset, it.totalSize)
            }
        }
    )

    override val container: Dropdown = dropdown

    override fun refresh() {
        container.text = shortUrl(data)
    }

}
