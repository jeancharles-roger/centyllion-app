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
import com.centyllion.client.Api
import com.centyllion.client.controller.navigation.ResultPageController
import com.centyllion.model.Asset
import kotlin.properties.Delegates.observable

class Asset3dSelectController(
    url: String, api: Api,
    var onUpdate: (old: String, new: String, controller: Asset3dSelectController) -> Unit = { _, _, _ -> }
) : NoContextController<String, Dropdown>() {

    class AssetItem(asset: Asset, parent: Asset3dSelectController): NoContextController<Asset, DropdownItem>() {

        override var data: Asset by observable(asset) { _, old, new ->
            if (old != new) refresh()
        }

        override val container = DropdownSimpleItem(data.name) { _ ->
            parent.data = "/api/asset/${data.id}"
            parent.dropdown.active = false
        }

        override var readOnly: Boolean = false

        override fun refresh() {
            container.text = data.name
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

    val assetsController =
        noContextDropdownController(emptyList<Asset>(), dropdown)
        { asset, _ -> AssetItem(asset, this) }

    val assetsPageController = ResultPageController(
        assetsController, { DropdownContentItem(it) },
        { offset, limit -> api.fetchAllAssets(offset, limit, "gltf", "glb") }
    )

    override val container: Dropdown = dropdown

    override fun refresh() {
        container.text = data
    }

}
