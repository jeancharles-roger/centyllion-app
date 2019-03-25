package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Direction
import kotlin.properties.Delegates

class DirectionSetEditController(
    directions: Set<Direction> = emptySet(),
    var onUpdate: (old: Set<Direction>, new: Set<Direction>, controller: DirectionSetEditController) -> Unit = { _, _, _ -> }
) : NoContextController<Set<Direction>, Field>() {

    val icons = mapOf(
        Direction.Up to "angle-up",
        Direction.Down to "angle-down",
        Direction.Left to "angle-left",
        Direction.Right to "angle-right"
    )

    override var data: Set<Direction> by Delegates.observable(directions) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@DirectionSetEditController)
            refresh()
        }
    }

    val left = buttonForDirection(Direction.Left)
    val up = buttonForDirection(Direction.Up)
    val down = buttonForDirection(Direction.Down)
    val right = buttonForDirection(Direction.Right)

    fun buttonForDirection(direction: Direction) =
        iconButton(Icon(icons.getValue(direction)), rounded = true, size = Size.Small, color = colorForDirection(direction)) {
            toggleDirection(direction)
        }

    fun colorForDirection(direction: Direction) =
        if (data.contains(direction)) ElementColor.Info else ElementColor.None

    fun toggleDirection(direction: Direction) {
        data = if (data.contains(direction)) data - direction else data + direction
    }

    override val container = Field(
        Control(left), Control(up), Control(down), Control(right),
        addons = true
    )

    override fun refresh() {
        up.color = colorForDirection(Direction.Up)
        down.color = colorForDirection(Direction.Down)
        left.color = colorForDirection(Direction.Left)
        right.color = colorForDirection(Direction.Right)
    }

}
