package com.centyllion.client.controller

import bulma.*
import chartjs.*
import com.centyllion.model.*
import kotlin.browser.window
import kotlin.properties.Delegates.observable

class SimulationRunController(
    simulation: Simulation, model: GrainModel,
    readonly: Boolean = false,
    val onUpdate: (old: Simulation, new: Simulation, controller: SimulationRunController) -> Unit =
        { _, _, _ -> }
) : Controller<Simulation, GrainModel, BulmaElement> {

    override var data: Simulation by observable(simulation) { _, old, new ->
        if (old != new) {
            nameController.data = new.name
            descriptionController.data = new.description
            simulator = Simulator(context, new)
            simulationViewController.data = simulator
            running = false
            onUpdate(old, new, this@SimulationRunController)
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            grainsController.data = new.grains
            behaviourController.data = new.behaviours
            behaviourController.context = new
            selectedGrainController.context = new.grains
            simulator = Simulator(new, data)
            simulationViewController.data = simulator
            running = false
            refresh()
        }
    }

    private var simulator = Simulator(context, data)

    private var running = false
    private var lastRefresh = 0

    private var presentCharts = false

    val nameController = EditableStringController(data.name, "Name", readonly)
    { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description", readonly)
    { _, new, _ ->
        data = data.copy(description = new)
    }

    // simulation execution controls
    val runButton = iconButton(Icon("play"), ElementColor.Primary, rounded = true) { run() }
    val stepButton = iconButton(Icon("step-forward"), ElementColor.Primary, rounded = true) { step() }
    val stopButton = iconButton(Icon("stop"), ElementColor.Danger, rounded = true) { stop() }
    val resetButton = iconButton(Icon("history"), ElementColor.Warning, rounded = true) { reset() }
    val toggleChartsButton = iconButton(Icon("chart-line"), ElementColor.Dark, rounded = true) { toggleCharts() }

    val stepLabel = Label()

    val grainsController =
        noContextColumnsController<Grain, GrainDisplayController>(model.grains) { _, grain, previous ->
            previous ?: GrainDisplayController(grain)
        }

    val behaviourController =
        columnsController<Behaviour, GrainModel, BehaviourDisplayController>(
            model.behaviours, model
        ) { _, behaviour, previous ->
            previous ?: BehaviourDisplayController(behaviour, model)
        }

    val selectedGrainController = GrainSelectController(null, model.grains)

    val chartCanvas = canvas {}

    val simulationViewController: SimulatorViewController =
        if (readonly) SimulatorViewController(simulator) else
            SimulatorEditController(simulator) { ended, new, _ ->
                updatedSimulatorFromView(ended, new)
            }

    override val container = Columns(
        Column(nameController, size = ColumnSize.OneThird),
        Column(descriptionController, size = ColumnSize.TwoThirds),
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
            simulationViewController.container,
            div(chartCanvas, classes = "has-text-centered"),
            desktopSize = ColumnSize.TwoThirds
        ),
        Column(
            Title("Grains", TextSize.S4), grainsController,
            Title("Behaviours", TextSize.S4), behaviourController,
            desktopSize = ColumnSize.OneThird
        ),
        multiline = true
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
            simulator.reset()
            refresh()
            refreshChart()
        }
    }

    private fun executeStep(updateChart: Boolean) {
        simulator.oneStep()

        refreshCanvas()
        refreshCounts()

        // appends data to charts
        if (presentCharts) {
            chart.data.datasets.zip(simulator.lastGrainsCount().values) { set: LineDataSet, i: Int ->
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

    private fun updatedSimulatorFromView(ended: Boolean, new: Simulator) {
        simulator.resetCount()
        refreshCounts()
        if (ended) {
            data = data.copy(agents = new.initialAgents.toList())
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
        simulationViewController.refresh()
    }

    fun refreshCounts() {
        // refreshes step count
        stepLabel.text = "${simulator.step}"

        // refreshes grain counts
        val counts = simulator.lastGrainsCount().values
        grainsController.dataControllers.zip(counts) { controller, count -> controller.count = count }
    }

    override fun refresh() {
        refreshButtons()
        refreshCanvas()
        refreshCounts()
        refreshChart()
    }

    fun refreshChart() {
        chartCanvas.hidden = !presentCharts
        if (presentCharts) {
            // refreshes charts
            val previous = chart.data.datasets.map { it.key to it }.toMap()
            chart.data.datasets = simulator.grainCountHistory.map {
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
