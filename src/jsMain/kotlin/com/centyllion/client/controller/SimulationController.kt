package com.centyllion.client.controller

import bulma.*
import chartjs.*
import com.centyllion.model.Simulator
import com.centyllion.model.sample.emptyModel
import com.centyllion.model.sample.emptySimulation
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.window

class SimulationController : Controller<Simulator, BulmaElement> {

    override var data: Simulator = Simulator(emptyModel, emptySimulation)
        set(value) {
            if (value != field) {
                field = value
                grainsController.data = value.model.grains
                behaviourController.data = value.model.behaviours
                running = false
                refresh()
            }
        }

    inline val model get() = data.model

    inline val simulation get() = data.simulation

    var running = false
    var lastRefresh = 0

    val runButton = Button("Run", ElementColor.Primary, rounded = true) { run() }
    val stepButton = Button("Step", ElementColor.Info, rounded = true) { step() }
    val stopButton = Button("Stop", ElementColor.Danger, rounded = true) { stop() }
    val resetButton = Button("Reset", ElementColor.Warning, rounded = true) { reset() }

    val stepLabel = Label()

    val simulationCanvas: HtmlWrapper = canvas {
        val canvasWidth = (window.innerWidth - 20).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulation.height * canvasWidth / simulation.width}"
    }

    val grainsController =
        ColumnsController(model.grains, columnSize = ColumnSize.Full) { _, grain ->
            GrainDisplayController().apply { data = grain }
        }

    val behaviourController =
        ColumnsController(model.behaviours, columnSize = ColumnSize.Full) { _, behaviour ->
            BehaviourDisplayController(model).apply { data = behaviour }
        }

    val graphCanvas = canvas {}

    override val container = div(
        Columns(
            Column(
                Level(
                    left = listOf(runButton, stepButton, stopButton, resetButton),
                    center = listOf(stepLabel)
                ),
                simulationCanvas,
                desktopSize = ColumnSize.TwoThirds
            ),
            Column(
                Title("Grains"), grainsController,
                Title("Behaviours"), behaviourController,
                desktopSize = ColumnSize.OneThird
            ),
            centered = true
        ),
        Columns(
            Column(graphCanvas),
            centered = true
        )
    )

    val canvas = simulationCanvas.root as HTMLCanvasElement

    val context = canvas.getContext("2d") as CanvasRenderingContext2D

    val chart = Chart(graphCanvas.root as HTMLCanvasElement, LineChartConfig(
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
        chart.data.datasets.zip(data.lastGrainsCount().values) { set: LineDataSet, i: Int ->
            val data = set.data
            if (data != null) {
                val index = if (data.isEmpty()) 0 else data.lastIndex
                data.push(LineChartPlot(index, i))
            }
        }

        if (updateChart) {
            chart.update(ChartUpdateConfig(duration = 0, lazy = true))
            lastRefresh = 0
        }
    }

    fun refreshButtons() {
        runButton.loading = running
        runButton.disabled = running
        stepButton.disabled = running
        stopButton.disabled = !running
        resetButton.disabled = running
    }

    fun refreshCanvas() {
        // refreshes simulation view
        val canvasWidth = canvas.width.toDouble()
        val canvasHeight = canvas.height.toDouble()
        val xSize = canvasWidth / simulation.width
        val xMax = simulation.width * xSize
        val ySize = canvasHeight / simulation.height
        context.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
        var currentX = 0.0
        var currentY = 0.0
        for (i in 0 until simulation.agents.size) {
            val grain = data.grainAtIndex(i)
            if (grain != null) {
                context.fillStyle = grain.color
                context.fillRect(currentX, currentY, xSize, ySize)
            }

            currentX += xSize
            if (currentX >= xMax) {
                currentX = 0.0
                currentY += ySize
            }
        }
    }

    fun refreshCounts() {
        // refreshes step count
        stepLabel.text = "${data.step}"

        // refreshes grain counts
        val counts = data.lastGrainsCount().values
        grainsController.dataControllers.zip(counts) { controller, count ->
            if (controller is GrainDisplayController) {
                controller.count = count
            }
        }
    }

    override fun refresh() {
        refreshButtons()
        refreshCanvas()
        refreshCounts()
        refreshChart()
    }

    fun refreshChart() {
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
    }
}
