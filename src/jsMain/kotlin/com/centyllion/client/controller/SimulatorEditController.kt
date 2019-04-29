package com.centyllion.client.controller

import bulma.*
import com.centyllion.model.Simulator
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import kotlin.browser.window
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.properties.Delegates.observable
import kotlin.random.Random


interface DisplayElement {
    fun draw(gc: CanvasRenderingContext2D)
}

open class SimulatorViewController(simulator: Simulator) : NoContextController<Simulator, BulmaElement>() {

    override var data: Simulator by observable(simulator) { _, old, new ->
        simulationCanvas.root.height =
            simulator.simulation.height * simulationCanvas.root.width / simulator.simulation.width
        refresh()
    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas("cent-simulation") {
        val canvasWidth = (window.innerWidth - 20).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulator.simulation.height * canvasWidth / simulator.simulation.width}"
    }

    override val container = div(
        div(simulationCanvas, classes = "has-text-centered")
    )

    val simulationContext = simulationCanvas.root.getContext("2d") as CanvasRenderingContext2D

    init {
        //refresh()
    }

    override fun refresh() {
        simulationCanvas.root.classList.toggle("is-danger", data.step > 0)
        simulationCanvas.root.classList.toggle("is-primary", data.step == 0)

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

class SimulatorEditController(
    simulator: Simulator,
    var onUpdate: (ended: Boolean, new: Simulator, SimulatorEditController) -> Unit = { _, _, _ -> }
) : SimulatorViewController(simulator) {

    enum class EditTools(val icon: String) {
        None("ban"), Pen("pen"), Line("pencil-ruler"), Spray("spray-can"), Eraser("eraser")
    }

    enum class ToolSize(val size: Int) {
        Fine(1), Small(5), Medium(10), Large(20)
    }

    override var data: Simulator by observable(simulator) { _, old, new ->
        onUpdate(true, new, this)
        selectedGrainController.context = new.model.grains
        if (old.model != new.model) {
            selectedGrainController.data = data.model.grains.firstOrNull()
        }
        refresh()
    }

    // simulation content edition
    private var selectedTool: EditTools = EditTools.None

    private var toolElement: DisplayElement? = null

    var drawStep = -1
    var canvasSourceX = -1.0
    var canvasSourceY = -1.0

    var mouseX = -1.0
    var mouseY = -1.0

    fun circle(x: Int, y: Int, factor: Int = 1, block: (i: Int, j: Int) -> Unit) {
        val size = ToolSize.valueOf(sizeDropdown.text).size * factor
        val halfSize = (size / 2).coerceAtLeast(1)
        if (halfSize == 1) {
            block(x, y)
        } else {
            for (i in x - halfSize until x + halfSize) {
                for (j in y - halfSize until y + halfSize) {
                    val inCircle = (x - i) * (x - i) + (y - j - 1) * (y - j) + 1 < halfSize * halfSize
                    val inSimulation = data.simulation.positionInside(i, j)
                    if (inCircle && inSimulation) block(i, j)
                }
            }
        }
    }

    fun line(sourceX: Int, sourceY: Int, x: Int, y: Int, block: (i: Int, j: Int) -> Unit) {
        val dx = x - sourceX
        val dy = y - sourceY
        if (dx.absoluteValue > dy.absoluteValue) {
            if (sourceX < x) {
                for (i in sourceX until x) {
                    val j = sourceY + dy * (i - sourceX) / dx
                    block(i, j)
                }
            } else {
                for (i in x until sourceX) {
                    val j = y + dy * (i - x) / dx
                    block(i, j)
                }
            }
        } else {
            if (sourceY < y) {
                for (j in sourceY until y) {
                    val i = sourceX + dx * (j - sourceY) / dy
                    block(i, j)
                }
            } else {
                for (j in y until sourceY) {
                    val i = x + dx * (j - y) / dy
                    block(i, j)
                }
            }
        }
    }

    fun drawOnSimulation(
        canvasSourceX: Double, canvasSourceY: Double, canvasX: Double, canvasY: Double,
        sourceX: Int, sourceY: Int, x: Int, y: Int, step: Int
    ) {
        when (selectedTool) {
            EditTools.Pen -> {
                selectedGrainController.data?.id?.let { idToSet ->
                    circle(x, y) { i, j -> data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet) }
                }
            }
            EditTools.Line -> {
                selectedGrainController.data?.id?.let { idToSet ->

                    // sets guide element
                    toolElement = object : DisplayElement {
                        override fun draw(gc: CanvasRenderingContext2D) {
                            gc.save()

                            gc.beginPath()
                            gc.moveTo(canvasSourceX, canvasSourceY)
                            gc.lineTo(canvasX, canvasY)
                            gc.lineWidth = 4.0
                            gc.strokeStyle = selectedGrainController.data?.color ?: "grey"
                            gc.stroke()

                            gc.lineWidth = 0.75
                            gc.strokeStyle = if (x == sourceX || y == sourceY) "blue" else "black"
                            gc.strokeText("$sourceX, $sourceY", canvasSourceX, canvasSourceY)
                            gc.strokeText("$x, $y", canvasX, canvasY)

                            gc.restore()
                        }
                    }

                    if (step == -1) {
                        // draw the line
                        line(sourceX, sourceY, x, y) { i, j ->
                            data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
                        }
                    }
                }

            }
            EditTools.Spray -> {
                val random = Random.Default
                selectedGrainController.data?.id?.let { idToSet ->
                    val sprayDensity = 0.005
                    circle(x, y, 4) { i, j ->
                        if (random.nextDouble() < sprayDensity) {
                            data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
                        }
                    }
                }
            }
            EditTools.Eraser -> {
                circle(x, y) { i, j ->
                    data.resetIdAtIndex(data.simulation.toIndex(i, j))
                }
            }
            EditTools.None -> {
            }
        }

        if (step == -1) {
            toolElement = null
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

        val stepX = simulationCanvas.root.width / data.simulation.width.toDouble()
        val stepY = simulationCanvas.root.height / data.simulation.height.toDouble()

        val rectangle = simulationCanvas.root.getBoundingClientRect()
        mouseX = event.clientX - rectangle.left - 4
        mouseY = event.clientY - rectangle.top - 4

        if (newStep != null) {
            if (newStep == 0) {
                canvasSourceX = mouseX
                canvasSourceY = mouseY
            }
            drawStep = newStep

            val sourceX = (canvasSourceX / stepX).roundToInt()
            val sourceY = (canvasSourceY / stepY).roundToInt()
            val x = ((mouseX - 4) / stepX).roundToInt()
            val y = ((mouseY - 4) / stepY).roundToInt()
            drawOnSimulation(canvasSourceX, canvasSourceY, mouseX, mouseY, sourceX, sourceY, x, y, drawStep)
        } else {
            val paintElement = object : DisplayElement {
                override fun draw(gc: CanvasRenderingContext2D) {
                    gc.save()

                    gc.beginPath()


                    val factor = if (selectedTool == EditTools.Spray) 4 else 1
                    val brushSize = ToolSize.valueOf(sizeDropdown.text).size
                    val size = brushSize * factor
                    val radiusX = size * stepX / 2.0
                    val radiusY = size * stepY / 2.0
                    gc.ellipse(mouseX, mouseY, radiusX, radiusY, (mouseX + mouseY) / 20, 0.0, 6.30 /* 2pi */)

                    gc.strokeStyle = "black"
                    gc.lineWidth = 1.0

                    when (selectedTool) {
                        EditTools.Eraser -> {
                            gc.globalAlpha = 0.7
                            gc.fillStyle = "white"
                        }
                        EditTools.Spray -> {
                            gc.globalAlpha = 0.3
                            gc.fillStyle = selectedGrainController.data?.color ?: "grey"
                        }
                        else -> {
                            gc.globalAlpha = 0.7
                            gc.fillStyle = selectedGrainController.data?.color ?: "grey"
                        }
                    }

                    gc.fill()
                    gc.globalAlpha = 1.0
                    if (selectedTool == EditTools.Eraser || size > 1) gc.stroke()

                    gc.restore()
                }
            }


            toolElement = when {
                selectedTool == EditTools.Pen && selectedGrainController.data != null -> paintElement
                selectedTool == EditTools.Spray && selectedGrainController.data != null -> paintElement
                selectedTool == EditTools.Eraser -> paintElement
                else -> null
            }
            refresh()
        }
    }

    fun selectTool(tool: EditTools) {
        toolButtons.forEach { it.outlined = false }
        toolButtons[tool.ordinal].outlined = true
        selectedTool = tool
    }

    val selectedGrainController = GrainSelectController(data.model.grains.firstOrNull(), data.model.grains)

    val sizeDropdown = Dropdown(ToolSize.Fine.name, rounded = true).apply {
        items = ToolSize.values().map { size ->
            DropdownSimpleItem(size.name) {
                this.text = size.name
                this.toggleDropdown()
            }
        }
    }

    val toolButtons = EditTools.values().map { tool ->
        iconButton(Icon(tool.icon), ElementColor.Primary, rounded = true, outlined = selectedTool == tool) {
            selectTool(tool)
        }
    }

    val clearAllButton = iconButton(Icon("trash"), ElementColor.Danger, true) {
        (0 until data.simulation.dataSize).forEach { data.resetIdAtIndex(it) }
        onUpdate(true, data, this)
        refresh()
    }

    val editToolbar = Level(
        center = listOf(Field(addons = true).apply {
            body = toolButtons.map { Control(it) }
        }, sizeDropdown, selectedGrainController.container, clearAllButton)
    )

    override val container = div(
        div(simulationCanvas, classes = "has-text-centered"),
        editToolbar
    )

    init {
        simulationCanvas.root.apply {
            onmouseup = { mouseChange(it) }
            onmousedown = { mouseChange(it) }
            onmousemove = { mouseChange(it) }
            onmouseout = {
                toolElement = null
                refresh()
            }
        }
    }

    override fun refresh() {

        toolButtons.forEach {
            it.color = if (data.step > 0) ElementColor.Danger else ElementColor.Primary
        }

        super.refresh()
        toolElement?.draw(simulationContext)

    }
}
