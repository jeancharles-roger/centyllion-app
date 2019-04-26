package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.FeaturedDescription
import kotlin.properties.Delegates.observable

class FeaturedController(
    featured: FeaturedDescription, size: ColumnSize = ColumnSize.Half
) : NoContextController<FeaturedDescription, Column>() {

    override var data by observable(featured) { _, old, new ->
        if (old != new)  refresh()
    }

    val name = SubTitle(data.name)
    val description = Label(data.description)

    val author = Label(data.authorName)

    fun thumbnail() =
        if (data.thumbnailId.isNotEmpty()) Image("/api/asset/${data.thumbnailId}", ImageSize.S128) else null

    val body = Media(
        left = listOfNotNull(thumbnail()),
        center = listOf(name, description),
        right = listOf(author)
    )

    override val container = Column(body, size = size)

    override fun refresh() {
        body.left = listOfNotNull(thumbnail())
        name.text = data.name
        description.text = data.description
        author.text = data.authorName
    }
}
