package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.FeaturedDescription
import kotlin.properties.Delegates.observable

class FeaturedController(
    featured: FeaturedDescription, size: ColumnSize = ColumnSize.OneQuarter
) : NoContextController<FeaturedDescription, Column>() {

    override var data by observable(featured) { _, old, new ->
        if (old != new)  refresh()
    }

    override var readOnly = false

    val name = SubTitle(data.name)
    val description = Label(data.description)

    val author = Label(data.authorName)

    fun thumbnail() = Image(
        if (data.thumbnailId != null) "/api/asset/${data.thumbnailId}" else "/images/480x480.png",
        ImageSize.Square
    )

    val body = Card(
        CardImage(thumbnail()),
        CardContent(name, author, description)
    )

    override val container = Column(body, size = size)

    override fun refresh() {
        name.text = data.name
        description.text = data.description
        author.text = data.authorName
    }
}
