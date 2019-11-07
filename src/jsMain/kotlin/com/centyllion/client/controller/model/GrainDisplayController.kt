package com.centyllion.client.controller.model

import bulma.Box
import bulma.Controller
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.Span
import bulma.TextColor
import com.centyllion.client.controller.utils.DeleteCallbackProperty
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class GrainDisplayController(val page: BulmaPage, grain: Grain, model: GrainModel): Controller<Grain, GrainModel, Box> {

    override var data: Grain by observable(grain) { _, old, new ->
        if (old != new) {
            errorIcon.hidden = data.diagnose(context, page.appContext.locale).isEmpty()
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            errorIcon.hidden = data.diagnose(context, page.appContext.locale).isEmpty()
            refresh()
        }
    }

    override var readOnly by observable(false) { _, old, new ->
        if (old != new) deleteCallbackProperty.readOnly = new
    }

    val errorIcon = Icon("exclamation-triangle", color = TextColor.Danger).apply {
        hidden = data.diagnose(context, page.appContext.locale).isEmpty()
    }

    val icon = Icon(grain.icon).apply {
        root.style.color = data.color
    }

    val titleLabel = Label(grain.name)
    val invisibleIcon = Icon("eye-slash", color = TextColor.Primary)
    val movingIcon = Icon("walking", color = TextColor.Primary)
    val deathIcon = Icon("skull-crossbones", color = TextColor.Primary)

    val status = Span(invisibleIcon, movingIcon, deathIcon)

    val body = Level(left=listOf(errorIcon), center = listOf(icon, titleLabel, status), mobile = true)

    val deleteCallbackProperty = DeleteCallbackProperty(null,this) { old, new ->
        old?.let { body.right -= it }
        new?.let { body.right += it }
    }
    var onDelete by deleteCallbackProperty

    override val container = Box(body)

    override fun refresh() {
        icon.icon = data.icon
        icon.root.style.color = data.color
        titleLabel.text = data.name
        invisibleIcon.hidden = !data.invisible
        movingIcon.hidden = !data.canMove
        deathIcon.hidden = data.halfLife <= 0
        status.hidden = invisibleIcon.hidden && movingIcon.hidden && deathIcon.hidden
        movingIcon.root.style.opacity = "${data.movementProbability.coerceAtLeast(0.3)}"
        deathIcon.root.style.opacity = "${(101 - data.halfLife.coerceAtMost(71)) / 100.0}"
    }

}
