package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.FeaturedDescription
import kotlin.properties.Delegates.observable

class FeaturedController(featured: FeaturedDescription, size: ColumnSize = ColumnSize.Half) : NoContextController<FeaturedDescription, Column>() {

    override var data by observable(featured) { _, old, new ->
        if (old != new) refresh()
    }

    private fun dotColumns() =
        data.dotColors.map {
            Column(Icon("circle").apply { root.style.color = it }, size = ColumnSize.S1)
        }

    val dots = Columns(multiline = true, mobile = true).apply { columns = dotColumns() }

    val name = SubTitle(data.name)
    val description = Label(data.description)

    fun thumbnail() =
        if (data.thumbnailId.isNotEmpty()) Image("/asset/${data.thumbnailId}", ImageSize.S128) else null

    val body = Media(
        left = listOfNotNull(thumbnail()),
        center = listOf(name, description, dots)
    )

    override val container = Column(body, size = size)

    override fun refresh() {
        body.left = listOfNotNull(thumbnail())
        dots.columns = dotColumns()
        name.text = data.name
        description.text = data.description
    }
}
