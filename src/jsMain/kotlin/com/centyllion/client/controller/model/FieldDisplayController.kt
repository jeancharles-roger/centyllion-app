package com.centyllion.client.controller.model

import bulma.Box
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.NoContextController
import bulma.Span
import bulma.TextColor
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable

open class FieldDisplayController(field: Field): NoContextController<Field, Box>() {

    override var data: Field by observable(field) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly = false

    val icon = Icon("square-full").apply {
        root.style.color = data.color
    }

    val titleLabel = Label(field.name)
    val movingIcon = Icon("walking", color = TextColor.Primary)
    val deathIcon = Icon("dizzy", color = TextColor.Primary)

    val status = Span(movingIcon, deathIcon)

    val body = Level(center = listOf(icon, titleLabel, status), mobile = true)

    override val container = Box(body)

    override fun refresh() {
        icon.root.style.color = data.color
        titleLabel.text = data.name
        movingIcon.hidden = data.speed <= 0.0
        deathIcon.hidden = data.halfLife <= 0
        status.hidden = movingIcon.hidden && deathIcon.hidden
        movingIcon.root.style.opacity = "${data.speed.coerceAtLeast(0.3f)}"
        deathIcon.root.style.opacity = "${(101 - data.halfLife.coerceAtMost(71)) / 100.0}"
    }

}
