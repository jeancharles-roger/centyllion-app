package com.centyllion.client.controller

import bulma.*
import chartjs.*
import com.centyllion.model.Behaviour
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.Simulator
import com.centyllion.model.sample.emptyModel
import com.centyllion.model.sample.emptySimulation
import kotlin.browser.window
import kotlin.properties.Delegates

class SimulationRunController : NoContextController<Simulator, BulmaElement>() {

    override var data: Simulator by Delegates.observable(Simulator(emptyModel, emptySimulation)) { _, old, new ->
        if (old != new) {
            grainsController.data = new.model.grains
            behaviourController.data = new.model.behaviours
            behaviourController.context = new.model
            selectedGrainController.context = new.model.grains
            simulationEditController.data = new.simulation
            simulationEditController.context = new.model
            running = false
            refresh()
        }
    }

    inline val model get() = data.model

    var running = false
    var lastRefresh = 0

    var presentCharts = true

    // simulation execution controls
    val runButton = iconButton(Icon("play"), ElementColor.Primary, rounded = true) { run() }
    val stepButton = iconButton(Icon("step-forward"), ElementColor.Primary, rounded = true) { step() }
    val stopButton = iconButton(Icon("stop"), ElementColor.Danger, rounded = true) { stop() }
    val resetButton = iconButton(Icon("history"), ElementColor.Warning, rounded = true) { reset() }
    val toggleChartsButton = iconButton(Icon("chart-line"), ElementColor.Link, rounded = true) { toggleCharts() }

    val stepLabel = Label()

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

    val selectedGrainController = GrainSelectController(null, model.grains)

    val chartCanvas = canvas {}

    val simulationEditController = SimulationEditController { _, _ ->
        data.resetCount()
        refreshCounts()
    }

    override val container = div(
        Columns(
            Column(
                Level(
                    center = listOf(
                        Field(
                            Control(runButton), Control(stepButton), Control(stopButton), Control(resetButton),
                            addons = true
                        ),
                        stepLabel,
                        toggleChartsButton
                    ),
                    mobile = true
                ),
                simulationEditController.container,
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
        simulationEditController.refresh()
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
            val previous = chart.data.datasets.map { it.key to it }.toMap()
            chart.data.datasets = data.grainCountHistory.map {
                val dataSet = previous.getOrElse(it.key.id) {
                    LineDataSet(key = it.key.id, fill = "false", showLine = true, pointRadius = 1)
                }
                dataSet.apply {
                    label = it.key.label(true)
                    borderColor = it.key.color
                    backgroundColor = it.key.color
                    data = it.value.mapIndexed { index, i -> LineChartPlot(index, i) }.toTypedArray()
                }
            }.toTypedArray()
            chart.update()
            lastRefresh = 0
        } else {
            chart.data.datasets = emptyArray()
            chart.update()
        }
    }
}
