package com.centyllion.client.controller.model

import bulma.Div
import bulma.HtmlWrapper
import bulma.canvas
import com.centyllion.model.ApplicableBehavior
import com.centyllion.model.Simulator
import com.centyllion.model.colorNames
import com.centyllion.model.minFieldLevel
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.window
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.properties.Delegates

open class Simulator2dViewController(simulator: Simulator) : SimulatorViewController(simulator) {

    override var data: Simulator by Delegates.observable(simulator) { _, _, _ ->
        refresh()
    }

    override var readOnly: Boolean by Delegates.observable(false) { _, old, new ->
        if (old != new) {
            // TODO
        }
    }

    override fun animate() {
        // nothing to do
    }

    override fun oneStep(applied: Collection<ApplicableBehavior>, dead: Collection<Int>) {
        refresh()
    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas("cent-simulation") {
        val canvasWidth = (window.innerWidth - 40).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulator.simulation.height * canvasWidth / simulator.simulation.width}"
    }

    override val container = Div(
        Div(simulationCanvas, classes = "has-text-centered")
    )

    val simulationContext = simulationCanvas.root.getContext("2d") as CanvasRenderingContext2D

    override fun refresh() {
        simulationCanvas.root.classList.toggle("is-danger", data.step > 0)
        simulationCanvas.root.classList.toggle("is-primary", data.step == 0)

        val scale = 0.1
        val canvasWidth = simulationCanvas.root.width.toDouble()
        val canvasHeight = simulationCanvas.root.height.toDouble()
        val xStep = canvasWidth / data.simulation.width
        val yStep = canvasHeight / data.simulation.height

        val xSize = xStep * (1.0 + scale)
        val ySize = xStep * (1.0 + scale)

        simulationContext.save()
        // sets font awesome
        simulationContext.font = "${xSize.roundToInt()}px 'Font Awesome 5 Free'"
        simulationContext.clearRect(0.0, 0.0, canvasWidth, canvasHeight)

        var currentX = 0.0
        var currentY = 0.0
        for (i in 0 until data.currentAgents.size) {
            val grain = data.model.indexedGrains[data.idAtIndex(i)]
            data.model.fields.forEach { field ->
                val level = data.field(field.id)[i]
                if (level > minFieldLevel) {
                    val color = colorNames[field.color] ?: Triple(255, 50, 50)
                    val alpha = if (level >= 1f) 1f else  1f / ( -log10(level)) / 1.6f
                    simulationContext.save()
                    simulationContext.fillStyle = "rgba(${color.first}, ${color.second}, ${color.third}, $alpha)"
                    simulationContext.fillRect(currentX, currentY, xSize, ySize)
                    simulationContext.restore()
                }
            }

            simulationContext.save()
            if (grain != null) {
                simulationContext.fillStyle = grain.color
                if (grain.iconString != null) {
                    simulationContext.fillText(grain.iconString, currentX, currentY+yStep)
                } else {
                    simulationContext.fillRect(currentX, currentY, xSize, ySize)
                }
            }
            simulationContext.restore()

            currentX += xStep
            if (currentX >= canvasWidth) {
                currentX = 0.0
                currentY += yStep
            }
        }

        simulationContext.restore()
    }

    override fun dispose() {
        // nothing to do
    }
}
