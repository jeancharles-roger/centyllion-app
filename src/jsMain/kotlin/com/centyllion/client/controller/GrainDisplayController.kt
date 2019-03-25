package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Grain
import kotlin.properties.Delegates.observable

class GrainDisplayController: NoContextController<Grain, Column>() {

    override var data: Grain by observable(Grain()) { _, old, new ->
        if (old != new) refresh()
    }

    var count: Int by observable(-1) { _, _ , new ->
        countLabel.text = if (new < 0) "" else "$new"
    }

    val dot = span(classes = "dot")
    val titleLabel = Label()
    val descriptionLabel = p()
    val countLabel = Label()

    override val container = Column(Media(
        left = listOf(dot),
        center = listOf(titleLabel, descriptionLabel),
        right = listOf(countLabel)
    ), size = ColumnSize.Full)

    override fun refresh() {
        dot.root.style.backgroundColor = data.color
        titleLabel.text = data.name
        descriptionLabel.text = data.description
    }

}
