package com.centyllion.client.controller.model

import bulma.Control
import bulma.ElementColor
import bulma.Field
import bulma.Icon
import bulma.NoContextController
import bulma.Size
import bulma.iconButton
import com.centyllion.model.Direction
import kotlin.properties.Delegates.observable

class DirectionSetEditController(
    presentedDirection: Set<Direction>, activeDirections: Set<Direction>,
    var onUpdate: (old: Set<Direction>, new: Set<Direction>, controller: DirectionSetEditController) -> Unit = { _, _, _ -> }
) : NoContextController<Set<Direction>, Field>() {

    val icons = mapOf(
        Direction.Up to Icon("arrow-up"),
        Direction.Down to Icon("arrow-down"),
        Direction.Left to Icon("arrow-left"),
        Direction.Right to Icon("arrow-right"),
        Direction.LeftUp to Icon("arrow-left").apply { root.classList.add("fa-rotate-45") },
        Direction.LeftDown to Icon("arrow-down").apply { root.classList.add("fa-rotate-45") },
        Direction.RightUp to Icon("arrow-up").apply { root.classList.add("fa-rotate-45") },
        Direction.RightDown to Icon("arrow-right").apply { root.classList.add("fa-rotate-45") }
    )

    override var data: Set<Direction> by observable(activeDirections) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@DirectionSetEditController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
           buttonsMap.values.forEach { it.disabled = new }
        }
    }

    var error: Boolean by observable(false) { _, old, new ->
        if (old != new) refresh()
    }

    val buttonsMap = presentedDirection.map { it to buttonForDirection(it) }.toMap()

    fun buttonForDirection(direction: Direction) =
        iconButton(icons.getValue(direction), rounded = true, size = Size.Small, color = colorForDirection(direction)) {
            toggleDirection(direction)
        }

    fun colorForDirection(direction: Direction) = when {
        error -> ElementColor.Danger
        data.contains(direction) -> ElementColor.Info
        else -> ElementColor.None
    }

    fun toggleDirection(direction: Direction) {
        data = if (data.contains(direction)) data - direction else data + direction
    }

    override val container = Field(addons = true).apply {
        body = buttonsMap.values.map { Control(it) }
        root.style.transform = "scale(0.8)"
    }

    override fun refresh() {
        buttonsMap.forEach {
            it.value.color = colorForDirection(it.key)
        }
    }

}
