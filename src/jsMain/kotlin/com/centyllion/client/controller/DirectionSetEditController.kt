package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Direction

class DirectionSetEditController(directions: Set<Direction> = emptySet()) : NoContextController<Set<Direction>, Columns>() {

    val upIcon = "angle-up"
    val downIcon = "angle-down"
    val leftIcon = "angle-left"
    val rightIcon = "angle-right"

    override var data: Set<Direction> = directions
        set(value) {
            if (field != value) {
                field = value
                refresh()
            }
        }

    val left = Icon(leftIcon)
    val up = Icon(upIcon)
    val down = Icon(downIcon)
    val right = Icon(rightIcon)

    override val container = Columns(
        Column(left, size = ColumnSize.OneThird),
        Column(up, down, size = ColumnSize.OneThird),
        Column(right, size = ColumnSize.OneThird)
    )

    override fun refresh() {
        up.color = if (data.contains(Direction.Up)) TextColor.GreyLighter else TextColor.None
        down.color = if (data.contains(Direction.Down)) TextColor.GreyLighter else TextColor.None
        left.color = if (data.contains(Direction.Left)) TextColor.GreyLighter else TextColor.None
        right.color = if (data.contains(Direction.Right)) TextColor.GreyLighter else TextColor.None
    }

}
