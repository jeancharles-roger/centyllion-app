package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.GrainModelDescription
import kotlin.properties.Delegates.observable

class GrainModelDisplayController(
    modelDescription: GrainModelDescription, size: ColumnSize = ColumnSize.OneQuarter
) : NoContextController<GrainModelDescription, Column>() {

    override var data by observable(modelDescription) { _, old, new ->
        if (old != new)  refresh()
    }

    override var readOnly = false

    val name = SubTitle(data.model.name)
    val description = Label(data.model.description)

    val body = Card(
        CardContent(name, description)
    ).apply {
        root.classList.add("is-outlined")
    }



    override val container = Column(body, size = size)

    override fun refresh() {
        name.text = data.name
        description.text = data.model.description
    }
}
