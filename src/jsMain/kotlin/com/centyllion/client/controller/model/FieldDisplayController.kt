package com.centyllion.client.controller.model

import bulma.Box
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.NoContextController
import bulma.Span
import bulma.TextColor
import com.centyllion.client.controller.utils.DeleteCallbackProperty
import com.centyllion.model.Field
import kotlin.properties.Delegates.observable

open class FieldDisplayController(field: Field): NoContextController<Field, Box>() {

    override var data: Field by observable(field) { _, old, new ->
        if (old != new) refresh()
    }

    override var readOnly by observable(false) { _, old, new ->
        if (old != new) deleteCallbackProperty.readOnly = new
    }

    val icon = Icon("square-full").apply {
        root.style.color = data.color
    }

    val titleLabel = Label(field.name)
    val invisibleIcon = Icon("eye-slash", color = TextColor.Primary)
    val movingIcon = Icon("walking", color = TextColor.Primary)
    val deathIcon = Icon("skull-crossbones", color = TextColor.Primary)

    val status = Span(invisibleIcon, movingIcon, deathIcon)

    val body = Level(center = listOf(icon, titleLabel, status), mobile = true)

    val deleteCallbackProperty = DeleteCallbackProperty(null, this) { old, new ->
        old?.let { body.right -= it }
        new?.let { body.right += it }
    }
    var onDelete by deleteCallbackProperty

    override val container = Box(body)

    override fun refresh() {
        icon.root.style.color = data.color
        titleLabel.text = data.name
        invisibleIcon.hidden = !data.invisible
        movingIcon.hidden = data.speed <= 0.0
        deathIcon.hidden = data.halfLife <= 0
        status.hidden = invisibleIcon.hidden && movingIcon.hidden && deathIcon.hidden
        movingIcon.root.style.opacity = "${data.speed.coerceAtLeast(0.3f)}"
        deathIcon.root.style.opacity = "${(101 - data.halfLife.coerceAtMost(71)) / 100.0}"
    }

}
