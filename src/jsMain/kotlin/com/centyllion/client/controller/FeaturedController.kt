package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.FeaturedDescription
import kotlin.properties.Delegates.observable

class FeaturedController(featured: FeaturedDescription) : NoContextController<FeaturedDescription, Column>() {

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

    val body = Media(center = listOf(name, description, dots))

    override val container = Column(body, size = ColumnSize.OneQuarter)

    override fun refresh() {
        dots.columns = dotColumns()
        name.text = data.name
        description.text = data.description
    }
}
