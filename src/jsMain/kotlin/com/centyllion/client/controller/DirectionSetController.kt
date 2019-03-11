package com.centyllion.client.controller

import com.centyllion.model.Direction
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.i
import org.w3c.dom.HTMLElement
import kotlin.browser.document

const val upIcon = "fa-angle-up"
const val downIcon = "fa-angle-down"
const val leftIcon = "fa-angle-left"
const val rightIcon = "fa-angle-right"

class DirectionSetController : Controller<Set<Direction>> {
    override var data: Set<Direction> = emptySet()
        set(value) {
            if (field != value) {
                field = value
                refresh()
            }
        }

    override val container: HTMLElement = document.create.div {
        columns("is-mobile is-vcentered") {
            column(size(4)) {
                i("fas $leftIcon")
            }
            column(size(4)){
                i("fas $upIcon")
                i("fas $downIcon")
            }
            column(size(4)) {
                i("fas $rightIcon")
            }
        }
    }

    val up = container.querySelector(".$upIcon") as HTMLElement
    val down = container.querySelector(".$downIcon") as HTMLElement
    val left = container.querySelector(".$leftIcon") as HTMLElement
    val right = container.querySelector(".$rightIcon") as HTMLElement

    override fun refresh() {
        up.classList.toggle("has-text-grey-lighter", !data.contains(Direction.Up))
        down.classList.toggle("has-text-grey-lighter", !data.contains(Direction.Down))
        left.classList.toggle("has-text-grey-lighter", !data.contains(Direction.Left))
        right.classList.toggle("has-text-grey-lighter", !data.contains(Direction.Right))
    }

}
