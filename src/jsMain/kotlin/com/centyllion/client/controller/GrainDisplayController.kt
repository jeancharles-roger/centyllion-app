package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Grain
import kotlin.properties.Delegates.observable

class GrainDisplayController(grain: Grain): NoContextController<Grain, Column>() {

    override var data: Grain by observable(grain) { _, old, new ->
        if (old != new) refresh()
    }

    var count: Int by observable(-1) { _, _ , new ->
        countLabel.text = if (new < 0) "" else "$new"
    }

    val icon = Icon(grain.icon).apply {
        root.style.color = data.color
    }

    val titleLabel = Label(data.name)
    val descriptionLabel = span(data.description)
    val countLabel = Label()

    override val container = Column(Media(
        left = listOf(icon),
        center = listOf(titleLabel, descriptionLabel),
        right = listOf(countLabel)
    ), size = ColumnSize.Full)

    override fun refresh() {
        icon.root.style.color = data.color
        titleLabel.text = data.name
        descriptionLabel.text = data.description
    }

}
