package com.centyllion.client.controller.model

import bulma.Column
import bulma.ColumnSize
import bulma.Controller
import bulma.Icon
import bulma.Label
import bulma.Media
import bulma.Span
import bulma.TextColor
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

open class GrainDisplayController(grain: Grain, model: GrainModel): Controller<Grain, GrainModel, Column> {

    override var data: Grain by observable(grain) { _, old, new ->
        if (old != new) refresh()
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    val icon = Icon(grain.icon).apply {
        root.style.color = data.color
    }

    val titleLabel = Label(grain.name)
    val movingIcon = Icon("walking", color = TextColor.Primary)
    val deathIcon = Icon("dizzy", color = TextColor.Primary)

    val status = Span(movingIcon, deathIcon)

    val body = Media(
        left = listOf(icon),
        center = listOf(titleLabel, status)
    ).apply {
        root.classList.add("is-outlined")
    }

    override val container = Column(body, size = ColumnSize.Full)

    override fun refresh() {
        icon.icon = data.icon
        icon.root.style.color = data.color
        titleLabel.text = data.name
        movingIcon.hidden = !data.canMove
        deathIcon.hidden = data.halfLife <= 0
        status.hidden = movingIcon.hidden && deathIcon.hidden
        deathIcon.root.style.opacity = "${(101 - data.halfLife.coerceAtMost(71)) / 100.0}"
    }

}
