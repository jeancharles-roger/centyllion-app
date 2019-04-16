package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.GrainModelDescription
import kotlin.properties.Delegates.observable

class GrainModelDisplayController(model: GrainModelDescription) : NoContextController<GrainModelDescription, Column>() {

    override var data by observable(model) { _, old, new ->
        if (old != new) refresh()
    }

    private fun dotColumns() =
        data.model.grains.map {
            Column(Icon("circle").apply { root.style.color = it.color }, size = ColumnSize.S1)
        }

    val dots = Columns(multiline = true, mobile = true).apply { columns = dotColumns() }

    val name = SubTitle(data.model.name)
    val description = Label(data.model.description)

    val body = Media(center = listOf(name, description, dots))

    override val container = Column(body)

    override fun refresh() {
        dots.columns = dotColumns()
        name.text = data.model.name
        description.text = data.model.description
    }
}
