package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Simulator
import kotlinx.html.js.onMouseDownFunction
import kotlinx.html.js.onMouseMoveFunction
import kotlinx.html.js.onMouseUpFunction
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import kotlin.browser.window
import kotlin.math.roundToInt
import kotlin.properties.Delegates.observable
import kotlin.random.Random

class SimulatorEditController(
    simulator: Simulator,
    var onUpdate: (ended: Boolean,new: Simulator, SimulatorEditController) -> Unit = { _, _, _ -> }
) : NoContextController<Simulator, BulmaElement>() {

    enum class EditTools(val icon: String) {
        None("ban"), Pen("pen"), Line("pencil-ruler"), Spray("spray-can"), Eraser("eraser")
    }

    override var data: Simulator by observable(simulator) { _, old, new ->
        onUpdate(true, new, this)
        selectedGrainController.context = new.model.grains
        refresh()
    }

    // simulation content edition
    private var selectedTool: EditTools = EditTools.None

    var drawStep = -1
    var sourceX = -1
    var sourceY = -1

    var mouseX = -1
    var mouseY = -1

    fun drawOnSimulation(sourceX: Int, sourceY: Int, x: Int, y: Int, step: Int) {
        when (selectedTool) {
            EditTools.Pen -> {
                val id = selectedGrainController.data?.id
                if (id != null) {
                    data.setIdAtIndex(data.simulation.toIndex(x, y), id)
                }
            }
            EditTools.Line -> {
                // TODO
            }
            EditTools.Spray -> {
                val random = Random.Default
                selectedGrainController.data?.id?.let { idToSet ->
                    val sprayHalfSize = 15
                    val sprayDensity = 0.005

                    for (i in x - sprayHalfSize until x + sprayHalfSize) {
                        for (j in y - sprayHalfSize until y + sprayHalfSize) {
                            if (data.simulation.positionInside(i, j)) {
                                if (random.nextDouble() < sprayDensity) {
                                    data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
                                }
                            }
                        }
                    }
                }
            }
            EditTools.Eraser -> {
                data.resetIdAtIndex(data.simulation.toIndex(x, y))
            }
            EditTools.None -> {
            }
        }

        val scale = 0.1
        val canvasWidth = simulationCanvas.root.width.toDouble()
        val canvasHeight = simulationCanvas.root.height.toDouble()
        val xStep = canvasWidth / data.simulation.width
        val xMax = data.simulation.width * xStep
        val yStep = canvasHeight / data.simulation.height
        val xSize = xStep * (1.0 + scale)
        val xDelta = xStep * (scale / 2.0)
        val ySize = xStep * (1.0 + scale)
        val yDelta = xStep * (scale / 2.0)
        simulationContext.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
        var currentX = 0.0
        var currentY = 0.0
        for (i in 0 until data.currentAgents.size) {
            val grain = data.model.indexedGrains[data.idAtIndex(i)]
            if (grain != null) {
                simulationContext.fillStyle = grain.color
                simulationContext.fillRect(currentX - xDelta, currentY - yDelta, xSize, ySize)
            }

            currentX += xStep
            if (currentX >= xMax) {
                currentX = 0.0
                currentY += yStep
            }
        }

        onUpdate(step == -1, data, this)
        refresh()
    }

    private fun mouseChange(event: MouseEvent) {
        val clicked = event.buttons.toInt() == 1
        val newStep = when {
            clicked && drawStep >= 0 -> drawStep + 1
            clicked -> 0
            drawStep > 0 -> -1
            else -> null
        }

        val rectangle = simulationCanvas.root.getBoundingClientRect()
        val canvasX = event.clientX - rectangle.left
        val canvasY = event.clientY - rectangle.top
        val stepX = data.simulation.width.toDouble() / simulationCanvas.root.width
        val stepY = data.simulation.height.toDouble() / simulationCanvas.root.height
        mouseX = (canvasX * stepX - 2*stepX).roundToInt()
        mouseY = (canvasY * stepY - 2*stepY).roundToInt()

        if (newStep != null) {
            if (newStep == 0) {
                sourceX = mouseX
                sourceY = mouseY
            }
            drawStep = newStep
            drawOnSimulation(sourceX, sourceY, mouseX, mouseY, drawStep)
        }
    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas("cent-simulation") {
        val canvasWidth = (window.innerWidth - 20).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${data.simulation.height * canvasWidth / data.simulation.width}"

        onMouseUpFunction = {
            if (it is MouseEvent) {
                mouseChange(it)
            }
        }
        onMouseDownFunction = {
            if (it is MouseEvent) {
                mouseChange(it)
            }
        }
        onMouseMoveFunction = {
            if (it is MouseEvent) {
                mouseChange(it)
            }
        }
    }

    fun selectTool(tool: EditTools) {
        toolButtons.forEach { it.outlined = false }
        toolButtons[tool.ordinal].outlined = true
        selectedTool = tool
    }

    val selectedGrainController = GrainSelectController(null, data.model.grains)

    val toolButtons = EditTools.values().map { tool ->
        iconButton(Icon(tool.icon), ElementColor.Primary, rounded = true, outlined = selectedTool == tool) { selectTool(tool) }
    }

    val editToolbar = Level(
        center = listOf(Field(addons = true).apply {
            body = toolButtons.map { Control(it) }
        }, selectedGrainController.container)
    )

    override val container = div(
        div(simulationCanvas, classes = "has-text-centered"),
        editToolbar
    )

    val simulationContext = simulationCanvas.root.getContext("2d") as CanvasRenderingContext2D

    init {
        refresh()
    }

    override fun refresh() {
        val scale = 0.1
        val canvasWidth = simulationCanvas.root.width.toDouble()
        val canvasHeight = simulationCanvas.root.height.toDouble()
        val xStep = canvasWidth / data.simulation.width
        val xMax = data.simulation.width * xStep
        val yStep = canvasHeight / data.simulation.height
        val xSize = xStep * (1.0 + scale)
        val xDelta = xStep * (scale / 2.0)
        val ySize = xStep * (1.0 + scale)
        val yDelta = xStep * (scale / 2.0)
        simulationContext.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
        var currentX = 0.0
        var currentY = 0.0
        for (i in 0 until data.currentAgents.size) {
            val grain = data.model.indexedGrains[data.idAtIndex(i)]

            if (grain != null) {
                simulationContext.fillStyle = grain.color
                simulationContext.fillRect(currentX - xDelta, currentY - yDelta, xSize, ySize)
            }

            currentX += xStep
            if (currentX >= xMax) {
                currentX = 0.0
                currentY += yStep
            }
        }
    }
}
