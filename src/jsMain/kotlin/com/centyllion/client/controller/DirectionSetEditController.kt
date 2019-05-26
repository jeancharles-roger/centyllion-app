package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Direction
import kotlin.properties.Delegates

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

    override var data: Set<Direction> by Delegates.observable(activeDirections) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@DirectionSetEditController)
            refresh()
        }
    }

    override var readOnly: Boolean by Delegates.observable(false) { _, old, new ->
        if (old != new) {
           buttonsMap.values.forEach { it.disabled = new }
        }
    }

    val buttonsMap = presentedDirection.map { it to buttonForDirection(it) }.toMap()

    fun buttonForDirection(direction: Direction) =
        iconButton(icons.getValue(direction), rounded = true, size = Size.Small, color = colorForDirection(direction)) {
            toggleDirection(direction)
        }

    fun colorForDirection(direction: Direction) =
        if (data.contains(direction)) ElementColor.Info else ElementColor.None

    fun toggleDirection(direction: Direction) {
        data = if (data.contains(direction)) data - direction else data + direction
    }

    override val container = Field(addons = true).apply { body = buttonsMap.values.map { Control(it) } }

    override fun refresh() {
        buttonsMap.forEach {
            it.value.color = colorForDirection(it.key)
        }
    }

}
