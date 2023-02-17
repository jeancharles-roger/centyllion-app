package com.centyllion.client.controller.model

import bulma.ControlElement
import bulma.Controller
import bulma.HtmlWrapper
import bulma.canvas
import com.centyllion.client.plotter.toRGB
import com.centyllion.model.Direction
import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.geom.Point
import io.data2viz.viz.*
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import kotlin.properties.Delegates.observable

class DirectionController(
    initial: Set<Direction>, initialContext: Pair<String?,String?>,
    onUpdate: (old: Set<Direction>, new: Set<Direction>, DirectionController) -> Unit = { _, _, _ -> }
): ControlElement, Controller<Set<Direction>, Pair<String?,String?>, HtmlWrapper<HTMLCanvasElement>> {

    private val width: Int = 50
    private val height: Int = 50

    val stepX = width / 3.0
    val stepY = height / 3.0

    override var data: Set<Direction> by observable(initial) { _, old, new ->
        if (old != new) {
            visual.build()
            onUpdate(old, new, this@DirectionController)
        }
    }

    override var context by observable(initialContext) { _, old, new ->
        if (old != new) visual.build()
    }

    override var readOnly: Boolean = true

    val canvas = canvas {
        width = "${this@DirectionController.width}px"
        height = "${this@DirectionController.height}px"
    }

    private var redraw = false

    override val container: HtmlWrapper<HTMLCanvasElement> = canvas

    private fun Viz.build() {
        clear()

        fun PathNode.drawGrid() {
            strokeColor = Colors.Web.black
            strokeWidth = 1.0

            // 3 slots, 4 lines
            for (i in 0 until 4) {
                moveTo(i*stepX, 0.0)
                lineTo(i*stepX, height)
            }

            // 3 slots, 4 lines
            for (i in 0 until 4) {
                moveTo(0.0, i*stepY)
                lineTo(width, i*stepY)
            }
        }

        fun PathNode.drawOuterGrain(x: Int, y: Int, color: Color) {
            val delta = 2
            strokeColor = color
            fill = Colors.Web.darkgray
            moveTo(x * stepX + delta, y * stepY + delta)
            lineTo(x * stepX + stepX - delta, y * stepY + delta)
            lineTo(x * stepX + stepX - delta, y * stepY + stepY - delta)
            lineTo(x * stepX + delta, y * stepY + stepY - delta)
            lineTo(x * stepX + delta, y * stepY + delta)
        }

        fun PathNode.drawInnerGrain(x: Int, y: Int, color: Color) {
            val delta = 2
            fill = color
            moveTo(x * stepX + delta, y * stepY + delta)
            lineTo(x * stepX + stepX - delta, y * stepY + delta)
            lineTo(x * stepX + stepX - delta, y * stepY + stepY - delta)
            lineTo(x * stepX + delta, y * stepY + stepY - delta)
            lineTo(x * stepX + delta, y * stepY + delta)
        }


        // grid
        group {
            path { drawGrid() }
        }

        // grains
        group {
            val outerColor = context.first?.toRGB() ?: Colors.Web.darkgray
            val innerColor = context.second?.toRGB()
            // presents source of the behaviour
            innerColor?.let { path { drawInnerGrain(1, 1, it) } }

            // presents all grains from simulation for context
            Direction.values().forEach { direction ->
                if (data.contains(direction)) {
                    path {
                        drawOuterGrain(1 + direction.deltaX, 1 + direction.deltaY, outerColor)
                    }
                }
            }
        }

        invalidate()
    }

    val visual = viz {
        width = this@DirectionController.width.toDouble()
        height = this@DirectionController.height.toDouble()

        build()
        bindRendererOn(canvas.root)

        on(KPointerEvents.up) {
            // updates directions upon click
            val direction = pointToDirection(it.pos)
            when {
                direction == null -> { /* nothing to do */ }
                data.contains(direction) -> data = data - direction
                !data.contains(direction) -> data = data + direction
            }
            EventPropagation.Stop
        }
    }

    private fun pointToDirection(point: Point): Direction? {
        // computes x and y index (0 to 2)
        val x = point.x / stepX
        val y = point.y / stepY
        return when {
            y < 0 -> null
            y <= 1 -> when {
                x <= 1 -> Direction.LeftUp
                x <= 2 -> Direction.Up
                x <= 3 -> Direction.RightUp
                else -> null
            }
            y <= 2 -> when {
                x <= 1 -> Direction.Left
                x <= 2 -> null
                x <= 3 -> Direction.Right
                else -> null
            }
            y <= 3 -> when {
                x <= 1 -> Direction.LeftDown
                x <= 2 -> Direction.Down
                x <= 3 -> Direction.RightDown
                else -> null
            }
            else -> null
        }
    }

    fun invalidate() {
        redraw = true
        window.setTimeout({ renderRequest() }, 10)
    }

    fun renderRequest() {
        if (redraw) {
            visual.render()
            redraw = false
        }
    }

    override fun refresh() {
    }
}