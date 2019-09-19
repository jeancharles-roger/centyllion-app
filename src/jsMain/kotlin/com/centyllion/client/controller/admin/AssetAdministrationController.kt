package com.centyllion.client.controller.admin

import bulma.BulmaElement
import bulma.Card
import bulma.CardContent
import bulma.CardImage
import bulma.Help
import bulma.Image
import bulma.ImageSize
import bulma.NoContextController
import com.centyllion.model.Asset
import kotlin.properties.Delegates.observable

class AssetAdministrationController(asset: Asset) : NoContextController<Asset, BulmaElement>() {

    override var data: Asset by observable(asset) { _, _, _ ->
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, _, _ -> }

    val url get() = "/api/asset/${data.id}/${data.name}"

    val thumbnail = Image(if (data.name.endsWith(".png")) url else "/images/480x480.png", ImageSize.Square)
    val name = Help(data.name)

    override val container = Card(
        CardImage(thumbnail),
        CardContent(name)
    )

    override fun refresh() {
        thumbnail.src = url
        name.text = data.name
    }
}
