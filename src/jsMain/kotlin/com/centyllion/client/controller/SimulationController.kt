package com.centyllion.client.controller

import bulma.*
import chartjs.*
import com.centyllion.model.Behaviour
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.Simulator
import com.centyllion.model.sample.emptyModel
import com.centyllion.model.sample.emptySimulation
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseMoveFunction
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import kotlin.browser.window
import kotlin.math.roundToInt

class SimulationController : NoContextController<Simulator, BulmaElement>() {

    enum class EditTools(val icon: String) {
        Pen("pen"), Line("pencil-ruler"), Spray("spray-can"), Eraser("eraser")
    }

    override var data: Simulator = Simulator(emptyModel, emptySimulation)
        set(value) {
            if (value != field) {
                field = value
                grainsController.data = value.model.grains
                behaviourController.data = value.model.behaviours
                behaviourController.context = value.model
                running = false
                refresh()
            }
        }

    inline val model get() = data.model

    inline val simulation get() = data.simulation

    var running = false
    var lastRefresh = 0

    var presentCharts = true

    // simulation execution controls
    val runButton = iconButton(Icon("play"), ElementColor.Primary, rounded = true) { run() }
    val stepButton = iconButton(Icon("step-forward"), ElementColor.Info, rounded = true) { step() }
    val stopButton = iconButton(Icon("stop"), ElementColor.Danger, rounded = true) { stop() }
    val resetButton = iconButton(Icon("history"), ElementColor.Warning, rounded = true) { reset() }
    val toggleChartsButton = iconButton(Icon("chart-line"), ElementColor.Link, rounded = true) { toggleCharts() }

    val stepLabel = Label()

    fun drawOnSimulation(sourceX: Int, sourceY: Int, x: Int, y: Int, step: Int) {
        val idToSet = 1
        val spraySize = 50
        val sprayDensity = 0.2

        when (selectedTool) {
            EditTools.Pen -> {
                simulation.setIdAtIndex(simulation.toIndex(x, y), idToSet)
            }
            EditTools.Line -> {
                // TODO
            }
            EditTools.Spray -> {

            }
            EditTools.Eraser -> {
                simulation.resetIdAtIndex(simulation.toIndex(x, y))
            }
        }

        refreshCanvas()
        data.resetCount()
        refreshCounts()

        if (step == -1) {
            simulation.saveState()
        }
    }

    var drawStep = -1
    var sourceX = -1
    var sourceY = -1

    private fun mouseChange(event: MouseEvent) {
        if (running) return

        val clicked = event.buttons.toInt() == 1
        val newStep = when {
            clicked && drawStep >= 0 -> drawStep + 1
            clicked -> 0
            drawStep > 0 -> -1
            else -> null
        }
        if (newStep != null) {
            val rectangle = simulationCanvas.root.getBoundingClientRect()
            val canvasX = event.clientX - rectangle.left
            val canvasY = event.clientY - rectangle.top

            val x = ((canvasX / simulationCanvas.root.width) * simulation.width).roundToInt()
            val y = ((canvasY / simulationCanvas.root.height) * simulation.height).roundToInt()

            if (newStep == 0) {
                sourceX = x
                sourceY = y
            }
            drawStep = newStep
            drawOnSimulation(sourceX, sourceY, x, y, drawStep)
        }
    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas {
        val canvasWidth = (window.innerWidth - 20).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulation.height * canvasWidth / simulation.width}"

        onClickFunction = {
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

    val grainsController =
        noContextColumnsController<Grain, GrainDisplayController>(model.grains) { _, grain, previous ->
            previous ?: GrainDisplayController().apply { data = grain }
        }

    val behaviourController =
        ColumnsController<Behaviour, GrainModel, BehaviourDisplayController>(
            model.behaviours, model
        ) { _, behaviour, previous ->
            previous ?: BehaviourDisplayController(model).apply { data = behaviour }
        }

    // simulation content edition
    private var selectedTool: EditTools? = null

    fun selectTool(tool: EditTools) {
        toolButtons.forEach { it.outlined = false }
        toolButtons[tool.ordinal].outlined = true
        selectedTool = tool
    }

    val toolButtons = EditTools.values().map { tool ->
        iconButton(Icon(tool.icon), ElementColor.Primary, rounded = true) { selectTool(tool) }
    }

    val editToolbar = Level(center = listOf(Field(grouped = true).apply { body = toolButtons.map { Control(it) } }))

    val chartCanvas = canvas {}

    override val container = div(
        Columns(
            Column(
                Level(
                    left = listOf(
                        Field(
                            Control(runButton), Control(stepButton), Control(stopButton), Control(resetButton),
                            grouped = true
                        )
                    ),
                    center = listOf(stepLabel),
                    right = listOf(toggleChartsButton),
                    mobile = true
                ),
                div(simulationCanvas, classes = "has-text-centered"),
                editToolbar,
                div(chartCanvas, classes = "has-text-centered"),
                desktopSize = ColumnSize.TwoThirds
            ),
            Column(
                Title("Grains", TextSize.S4), grainsController,
                Title("Behaviours", TextSize.S4), behaviourController,
                desktopSize = ColumnSize.OneThird
            )
        )
    )

    val simulationContext = simulationCanvas.root.getContext("2d") as CanvasRenderingContext2D

    val chart = Chart(chartCanvas.root, LineChartConfig(
        options = LineChartOptions().apply {
            animation.duration = 0
            scales.xAxes = arrayOf(LinearAxisOptions())
            scales.yAxes = arrayOf(LinearAxisOptions())
        }
    ))

    init {
        refresh()
    }

    fun run() {
        if (!running) {
            running = true
            refreshButtons()
            runningCallback()
        }
    }

    fun runningCallback() {
        if (running) {
            executeStep(lastRefresh >= 10)
            lastRefresh += 1
            window.setTimeout(this::runningCallback, 0)
        }
    }

    fun step() {
        if (!running) {
            executeStep(true)
        }
    }

    fun stop() {
        if (running) {
            running = false
            refresh()
        }
    }

    fun reset() {
        if (!running) {
            data.reset()
            refresh()
            refreshChart()
        }
    }

    private fun executeStep(updateChart: Boolean) {
        data.oneStep()
        refreshCanvas()
        refreshCounts()

        // appends data to charts
        if (presentCharts) {
            chart.data.datasets.zip(data.lastGrainsCount().values) { set: LineDataSet, i: Int ->
                val data = set.data
                if (data != null) {
                    val index = if (data.isEmpty()) 0 else data.lastIndex
                    data.push(LineChartPlot(index, i))
                }
            }
        }

        if (updateChart) {
            chart.update(ChartUpdateConfig(duration = 0, lazy = true))
            lastRefresh = 0
        }
    }

    fun toggleCharts() {
        presentCharts = !presentCharts
        toggleChartsButton.color = if (presentCharts) ElementColor.Info else ElementColor.Dark
        refresh()
    }

    fun refreshButtons() {
        runButton.loading = running
        runButton.disabled = running
        stepButton.disabled = running
        stopButton.disabled = !running
        resetButton.disabled = running
    }

    fun refreshCanvas() {
        val scale = 0.1

        // refreshes simulation view
        val canvasWidth = simulationCanvas.root.width.toDouble()
        val canvasHeight = simulationCanvas.root.height.toDouble()
        val xStep = canvasWidth / simulation.width
        val xMax = simulation.width * xStep
        val yStep = canvasHeight / simulation.height

        val xSize = xStep * (1.0 + scale)
        val xDelta = xStep * (scale / 2.0)
        val ySize = xStep * (1.0 + scale)
        val yDelta = xStep * (scale / 2.0)

        simulationContext.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
        var currentX = 0.0
        var currentY = 0.0
        for (i in 0 until simulation.agents.size) {
            val grain = data.grainAtIndex(i)
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

    fun refreshCounts() {
        // refreshes step count
        stepLabel.text = "${data.step}"

        // refreshes grain counts
        val counts = data.lastGrainsCount().values
        grainsController.dataControllers.zip(counts) { controller, count -> controller.count = count }
    }

    override fun refresh() {
        refreshButtons()
        refreshCanvas()
        refreshCounts()
        refreshChart()
    }

    fun refreshChart() {
        chartCanvas.root.classList.toggle("is-hidden", !presentCharts)
        if (presentCharts) {
            // refreshes charts
            chart.data.datasets = data.grainCountHistory.map {
                LineDataSet(
                    it.key.description, it.value
                        .mapIndexed { index, i -> LineChartPlot(index, i) }
                        .toTypedArray(),
                    borderColor = it.key.color, backgroundColor = it.key.color, fill = "false",
                    showLine = true, pointRadius = 1
                )
            }.toTypedArray()
            chart.update()
            lastRefresh = 0
        } else {
            chart.data.datasets = emptyArray()
            chart.update()
        }
    }
}
