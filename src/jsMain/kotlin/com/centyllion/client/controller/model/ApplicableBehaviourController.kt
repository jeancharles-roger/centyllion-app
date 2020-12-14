package com.centyllion.client.controller.model

import bulma.Box
import bulma.Column
import bulma.Controller
import bulma.Help
import bulma.canvas
import com.centyllion.client.plotter.toRGB
import com.centyllion.model.ApplicableBehavior
import com.centyllion.model.Simulator
import io.data2viz.color.Color
import io.data2viz.color.Colors
import io.data2viz.viz.PathNode
import io.data2viz.viz.Viz
import io.data2viz.viz.bindRendererOn
import io.data2viz.viz.viz
import kotlinx.browser.window
import kotlin.properties.Delegates.observable

class ApplicableBehaviourController(
    initial: ApplicableBehavior, simulator: Simulator
): Controller<ApplicableBehavior, Simulator, Column> {

    private val width: Int = 100
    private val height: Int = 100

    override var data by observable(initial) { _, old, new ->
        if (old != new) {
            title.text = new.behaviour.name
            visual.build()
            invalidate()
        }
    }

    override var context by observable(simulator) { _, old, new ->
        if (old != new) {
            visual.build()
            invalidate()
        }
    }

    override var readOnly: Boolean = true

    val title: Help = Help(data.behaviour.name)

    val canvas = canvas {
        width = "${this@ApplicableBehaviourController.width}px"
        height = "${this@ApplicableBehaviourController.height}px"
    }

    override val container: Column = Column(Box(title, canvas), narrow = true)

    private var redraw = false

    private enum class State {
        Reactive, Product;
        val next get() = if (this == Reactive) Product else Reactive
    }

    private var state = State.Reactive

    private fun Viz.build() {
        clear()

        val stepX = width / 3.0
        val stepY = height / 3.0

        fun PathNode.drawGrid() {
            stroke = Colors.Web.black
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

        fun PathNode.drawGrain(x: Int, y: Int, color: Color) {
            fill = color
            moveTo(x * stepX, y * stepY)
            lineTo(x * stepX + stepX, y * stepY)
            lineTo(x * stepX + stepX, y * stepY + stepY)
            lineTo(x * stepX, y * stepY + stepY)
            lineTo(x * stepX, y * stepY)
        }

        // grid
        group {
            path { drawGrid() }
        }

        // source grains
        group {

            when (state) {
                State.Reactive -> {
                    // presents source of the behaviour
                    val grain = context.grainAtIndex(data.index)
                    if (grain != null) path { drawGrain(1, 1, grain.color.toRGB()) }

                    data.usedNeighbours.forEach { agent ->
                        val agentGrain = context.model.grainForId(agent.id)
                        if (agentGrain != null) {
                            path {
                                drawGrain(
                                    1 + agent.direction.deltaX,
                                    1 + agent.direction.deltaY,
                                    agentGrain.color.toRGB()
                                )
                            }
                        }
                    }
                }
                State.Product -> {
                    // presents target of the behaviour
                    val grain = context.model.grainForId(data.behaviour.mainProductId)
                    if (grain != null) path { drawGrain(1, 1, grain.color.toRGB()) }

                    val reactives = data.usedNeighbours.sortedBy { it.id }
                    val reactions = data.behaviour.reaction.sortedBy { it.reactiveId }
                    reactions.zip(reactives).forEach { (reaction, reactive) ->
                        val agentGrain = context.model.grainForId(reaction.productId)
                        if (agentGrain != null) {
                            path {
                                drawGrain(
                                    1 + reactive.direction.deltaX,
                                    1 + reactive.direction.deltaY,
                                    agentGrain.color.toRGB()
                                )
                            }
                        }
                    }
                }
            }
        }

        invalidate()
    }

    val visual = viz {
        width = this@ApplicableBehaviourController.width.toDouble()
        height = this@ApplicableBehaviourController.height.toDouble()

        build()
        bindRendererOn(canvas.root)
    }

    fun invalidate() {
        redraw = true
        window.setTimeout({ renderRequest() }, 10)
    }

    fun switchState() {
        // change state
        state = state.next
        // rebuild
        visual.build()
        invalidate()
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