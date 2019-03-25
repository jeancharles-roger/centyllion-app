package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Direction
import kotlin.properties.Delegates

class DirectionSetEditController(
    directions: Set<Direction> = emptySet(),
    var onUpdate: (old: Set<Direction>, new: Set<Direction>, controller: DirectionSetEditController) -> Unit = { _, _, _ -> }
) : NoContextController<Set<Direction>, Field>() {

    val upIcon = "angle-up"
    val downIcon = "angle-down"
    val leftIcon = "angle-left"
    val rightIcon = "angle-right"

    override var data: Set<Direction> by Delegates.observable(directions) { _, old, new ->
        if (old != new) {
            onUpdate(old, new, this@DirectionSetEditController)
            refresh()
        }
    }

    val left = iconButton(Icon(leftIcon), rounded = true, size = Size.Small) { toggleDirection(Direction.Left) }
    val up = iconButton(Icon(upIcon), rounded = true, size = Size.Small) { toggleDirection(Direction.Up) }
    val down = iconButton(Icon(downIcon), rounded = true, size = Size.Small) { toggleDirection(Direction.Down) }
    val right = iconButton(Icon(rightIcon), rounded = true, size = Size.Small) { toggleDirection(Direction.Right) }

    fun toggleDirection(direction: Direction) {
        data = if (data.contains(direction)) data - direction else data + direction
    }

    override val container = Field(
        Control(left), Control(up), Control(down), Control(right),
        addons = true
    )

    override fun refresh() {
        up.color = if (data.contains(Direction.Up)) ElementColor.Info else ElementColor.None
        down.color = if (data.contains(Direction.Down)) ElementColor.Info else ElementColor.None
        left.color = if (data.contains(Direction.Left)) ElementColor.Info else ElementColor.None
        right.color = if (data.contains(Direction.Right)) ElementColor.Info else ElementColor.None
    }

}
