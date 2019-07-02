package com.centyllion.client.controller.model

import bulma.Control
import bulma.Div
import bulma.Dropdown
import bulma.DropdownSimpleItem
import bulma.ElementColor
import bulma.Field
import bulma.Icon
import bulma.Level
import bulma.iconButton
import com.centyllion.model.Grain
import com.centyllion.model.Simulator
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.pointerevents.PointerEvent
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import kotlin.random.Random

class SimulatorEditController(
    simulator: Simulator,
    var onUpdate: (ended: Boolean, new: Simulator, SimulatorEditController) -> Unit = { _, _, _ -> }
) : Simulator2dViewController(simulator) {

    interface DisplayElement {
        fun draw(gc: CanvasRenderingContext2D)
    }

    enum class EditTools(val icon: String) {
        None("ban"), Pen("pen"), Line("pencil-ruler"), Spray("spray-can"), Eraser("eraser")
    }

    enum class ToolSize(val size: Int) {
        Fine(1), Small(5), Medium(10), Large(20)
    }

    val roundDrawElement = object : DisplayElement {
        override fun draw(gc: CanvasRenderingContext2D) {
            gc.save()

            gc.beginPath()

            val factor = if (selectedTool == EditTools.Spray) 4 else 1
            val brushSize = ToolSize.valueOf(sizeDropdown.text).size
            val size = brushSize * factor
            val radiusX = size * stepX / 2.0
            val radiusY = size * stepY / 2.0
            gc.ellipse(toCanvasX(simulationX), toCanvasY(simulationY), radiusX, radiusY, 0.0, 0.0, 6.30 /* 2pi */)

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
            gc.stroke()

            gc.restore()
        }
    }

    // sets guide element
    val lineDrawElement = object : DisplayElement {
        override fun draw(gc: CanvasRenderingContext2D) {
            val canvasX = toCanvasX(simulationX)
            val canvasY = toCanvasY(simulationY)
            val canvasSourceX = toCanvasX(simulationSourceX)
            val canvasSourceY = toCanvasY(simulationSourceY)

            gc.save()
            gc.beginPath()
            gc.moveTo(canvasSourceX, canvasSourceY)
            gc.lineTo(canvasX, canvasY)
            gc.lineWidth = 4.0
            gc.strokeStyle = selectedGrainController.data?.color ?: "grey"
            gc.stroke()

            gc.lineWidth = 0.75
            gc.strokeStyle =
                if (simulationX == simulationSourceX || simulationX == simulationSourceY) "blue" else "black"
            gc.strokeText("$simulationSourceX, $simulationSourceY", canvasSourceX, canvasSourceY)
            gc.strokeText("$simulationX, $simulationY", canvasX, canvasY)

            gc.restore()
        }
    }

    override var data: Simulator by Delegates.observable(simulator) { _, old, new ->
        onUpdate(true, new, this)
        selectedGrainController.context = new.model.grains
        if (old.model != new.model) {
            selectedGrainController.data = data.model.grains.firstOrNull()
            stepX = simulationCanvas.root.width / data.simulation.width.toDouble()
            stepY = simulationCanvas.root.height / data.simulation.height.toDouble()
        }
        refresh()
    }

    // simulation content edition
    private var selectedTool: EditTools = EditTools.None

    private var toolElement: DisplayElement? = null

    var drawStep = -1

    var simulationSourceX = -1
    var simulationSourceY = -1

    var simulationX = -1
    var simulationY = -1

    var stepX = simulationCanvas.root.width / data.simulation.width.toDouble()
    var stepY = simulationCanvas.root.height / data.simulation.height.toDouble()

    fun toSimulationX(canvasX: Double) = ((canvasX - 1) / stepX).roundToInt()
    fun toSimulationY(canvasY: Double) = ((canvasY - 1) / stepY).roundToInt()

    fun toCanvasX(simulationX: Int) = simulationX * stepX + stepX / 2.0
    fun toCanvasY(simulationY: Int) = simulationY * stepY + stepY / 2.0

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

    fun drawOnSimulation(step: Int) {
        when (selectedTool) {
            EditTools.Pen -> {
                selectedGrainController.data?.id?.let { idToSet ->
                    circle(simulationX, simulationY) { i, j ->
                        data.setIdAtIndex(
                            data.simulation.toIndex(i, j),
                            idToSet
                        )
                    }
                }
            }
            EditTools.Line -> {
                selectedGrainController.data?.id?.let { idToSet ->
                    if (step == -1) {
                        // draw the line
                        line(simulationSourceX, simulationSourceY, simulationX, simulationY) { i, j ->
                            data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
                        }
                    }
                }

            }
            EditTools.Spray -> {
                val random = Random
                selectedGrainController.data?.id?.let { idToSet ->
                    val sprayDensity = 0.005
                    circle(simulationX, simulationY, 4) { i, j ->
                        if (Random.nextDouble() < sprayDensity) {
                            data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
                        }
                    }
                }
            }
            EditTools.Eraser -> {
                circle(simulationX, simulationY) { i, j ->
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
            drawStep >= 0 -> -1
            else -> null
        }

        val rectangle = simulationCanvas.root.getBoundingClientRect()
        simulationX = toSimulationX(event.clientX - rectangle.left - 4)
        simulationY = toSimulationY(event.clientY - rectangle.top - 4)

        if (newStep != null) {
            if (newStep == 0) {
                simulationSourceX = simulationX
                simulationSourceY = simulationY
            }
            drawStep = newStep

            drawOnSimulation(drawStep)
        }

        toolElement = when {
            selectedTool == EditTools.Pen && selectedGrainController.data != null -> roundDrawElement
            selectedTool == EditTools.Spray && selectedGrainController.data != null -> roundDrawElement
            selectedTool == EditTools.Eraser -> roundDrawElement
            selectedTool == EditTools.Line && drawStep > 0 -> lineDrawElement
            else -> null
        }
        refresh()
    }

    fun selectTool(tool: EditTools) {
        toolButtons.forEach { it.outlined = false }
        toolButtons[tool.ordinal].outlined = true
        selectedTool = tool
    }

    val selectedGrainController =
        GrainSelectController(data.model.grains.firstOrNull(), data.model.grains)

    override var selectedGrain: Grain?
        get() = selectedGrainController.data
        set(value) {
            selectedGrainController.data = value
        }

    val sizeDropdown = Dropdown(text = ToolSize.Fine.name, rounded = true).apply {
        items = ToolSize.values().map { size ->
            DropdownSimpleItem(size.name) {
                this.text = size.name
                this.toggleDropdown()
            }
        }
    }

    val toolButtons = EditTools.values().map { tool ->
        iconButton(
            Icon(tool.icon),
            ElementColor.Primary,
            rounded = true,
            outlined = selectedTool == tool
        ) {
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

    override val container = Div(
        Div(simulationCanvas, classes = "has-text-centered"),
        editToolbar
    )

    init {
        simulationCanvas.root.apply {
            addEventListener("pointerup", { mouseChange(it as PointerEvent) }, false)
            addEventListener("pointerdown", { mouseChange(it as PointerEvent) }, false)
            addEventListener("pointermove", { mouseChange(it as PointerEvent) }, false)
            addEventListener("pointerenter", { mouseChange(it as PointerEvent) }, false)
            addEventListener("pointerout", {
                toolElement = null
                refresh()
            }, false)

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
