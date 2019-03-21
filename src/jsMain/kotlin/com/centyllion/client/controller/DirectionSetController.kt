package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Direction

const val upIcon = "fa-angle-up"
const val downIcon = "fa-angle-down"
const val leftIcon = "fa-angle-left"
const val rightIcon = "fa-angle-right"

class DirectionSetController : Controller<Set<Direction>, Columns> {
    override var data: Set<Direction> = emptySet()
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
