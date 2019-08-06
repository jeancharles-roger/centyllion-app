package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Icon
import bulma.Label
import bulma.Media
import bulma.NoContextController
import com.centyllion.model.Grain
import kotlin.properties.Delegates.observable

class GrainDisplayController(grain: Grain): NoContextController<Grain, Column>() {

    override var data: Grain by observable(grain) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    var count: Int by observable(-1) { _, _ , new ->
        countLabel.text = if (new < 0) "" else "$new"
    }

    val icon = Icon(grain.icon).apply {
        root.style.color = data.color
    }

    val titleLabel = Label(data.name)
    val countLabel = Label()

    val body = Media(
        left = listOf(icon),
        center = listOf(titleLabel),
        right = listOf(countLabel)
    ).apply {
        root.classList.add("is-outlined")
    }

    override val container = Column(body, size = ColumnSize.Full)

    override fun refresh() {
        icon.icon = data.icon
        icon.root.style.color = data.color
        titleLabel.text = data.name
    }

}
