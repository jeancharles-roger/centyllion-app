package com.centyllion.client.controller.navigation

import bulma.Card
import bulma.CardContent
import bulma.CardImage
import bulma.Div
import bulma.Image
import bulma.ImageSize
import bulma.Label
import bulma.NoContextController
import bulma.SubTitle
import com.centyllion.client.markdownToHtml
import com.centyllion.model.FeaturedDescription
import kotlin.properties.Delegates.observable

class FeaturedController(featured: FeaturedDescription) : NoContextController<FeaturedDescription, Card>() {

    override var data by observable(featured) { _, old, new ->
        if (old != new)  refresh()
    }

    override var readOnly = false

    val name = SubTitle(data.name)
    val description = Div().apply { root.innerHTML = markdownToHtml(data.description) }

    val author = Label(data.authorName)

    val thumbnail = Image(
        if (data.thumbnailId != null) "/api/asset/${data.thumbnailId}" else "/images/480x480.png",
        ImageSize.Square
    )

    override val container = Card(
        CardImage(thumbnail),
        CardContent(name, author, description)
    ).apply {
        root.classList.add("is-outlined")
    }

    override fun refresh() {
        name.text = data.name
        description.root.innerHTML = markdownToHtml(data.description)
        author.text = data.authorName
        thumbnail.src = if (data.thumbnailId != null) "/api/asset/${data.thumbnailId}" else "/images/480x480.png"
    }
}
