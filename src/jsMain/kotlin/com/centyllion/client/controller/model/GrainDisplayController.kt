package com.centyllion.client.controller.model

import bulma.Box
import bulma.Controller
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.Span
import bulma.TextColor
import com.centyllion.client.controller.utils.DeleteCallbackProperty
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

open class GrainDisplayController(grain: Grain, model: GrainModel): Controller<Grain, GrainModel, Box> {

    override var data: Grain by observable(grain) { _, old, new ->
        if (old != new) refresh()
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly by observable(false) { _, old, new ->
        if (old != new) deleteCallbackProperty.readOnly = new
    }

    val icon = Icon(grain.icon).apply {
        root.style.color = data.color
    }

    val titleLabel = Label(grain.name)
    val movingIcon = Icon("walking", color = TextColor.Primary)
    val deathIcon = Icon("dizzy", color = TextColor.Primary)

    val status = Span(movingIcon, deathIcon)

    val body = Level(center = listOf(icon, titleLabel, status), mobile = true)

    val deleteCallbackProperty = DeleteCallbackProperty(this) { old, new ->
        old?.let { body.right -= it }
        new?.let { body.right += it }
    }
    var onDelete by deleteCallbackProperty

    override val container = Box(body)

    override fun refresh() {
        icon.icon = data.icon
        icon.root.style.color = data.color
        titleLabel.text = data.name
        movingIcon.hidden = !data.canMove
        deathIcon.hidden = data.halfLife <= 0
        status.hidden = movingIcon.hidden && deathIcon.hidden
        movingIcon.root.style.opacity = "${data.movementProbability.coerceAtLeast(0.3)}"
        deathIcon.root.style.opacity = "${(101 - data.halfLife.coerceAtMost(71)) / 100.0}"
    }

}
