package com.centyllion.client.controller.model

import bulma.BulmaElement
import bulma.Button
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Controller
import bulma.ElementColor
import bulma.Field
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.Slider
import bulma.TextSize
import bulma.Title
import bulma.canvas
import bulma.columnsController
import bulma.div
import bulma.iconButton
import bulma.noContextColumnsController
import bulma.wrap
import chartjs.Chart
import chartjs.ChartUpdateConfig
import chartjs.LineChartConfig
import chartjs.LineChartOptions
import chartjs.LineChartPlot
import chartjs.LineDataSet
import chartjs.LinearAxisOptions
import com.centyllion.client.AppContext
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.push
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.Simulation
import com.centyllion.model.Simulator
import kotlin.browser.window
import kotlin.properties.Delegates.observable

class SimulationRunController(
    simulation: Simulation, model: GrainModel, val appContext: AppContext,
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
            selectedGrainController.context = new.grains
            simulator = Simulator(new, data)
            behaviourController.context = simulator
            simulationViewController.data = simulator
            running = false
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            nameController.readOnly = new
            descriptionController.readOnly = new
            simulationViewController = createSimulationViewController()
            simulationViewController.readOnly = new
        }
    }

    private var simulator = Simulator(context, data)

    private var running = false
    private var fps = 50.0
    private var lastStepTimestamp = 0.0
    private var lastRequestSkipped = true
    private var lastFpsColorRefresh = 0
    private var lastChartRefresh = 0

    private var presentCharts = false

    private var view3d = true

    val nameController = EditableStringController(data.name, "Simulation Name")
    { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, "Description")
    { _, new, _ ->
        data = data.copy(description = new)
    }

    // simulation execution controls
    val rewindButton = iconButton(Icon("fast-backward"), ElementColor.Danger, rounded = true) { reset() }
    val runButton = iconButton(Icon("play"), ElementColor.Primary, rounded = true) { run() }
    val stepButton = iconButton(Icon("step-forward"), ElementColor.Primary, rounded = true) { step() }
    val stopButton = iconButton(Icon("stop"), ElementColor.Warning, rounded = true) { stop() }
    val toggleChartsButton = iconButton(Icon("chart-line"), ElementColor.Dark, rounded = true) { toggleCharts() }
    val view3dButton = iconButton(Icon("cubes"), ElementColor.Info, rounded = true) { toggle3d() }

    val fpsSlider = Slider(fps.toString(), "1", "200", "1", color = ElementColor.Info) { _, value ->
        fps = value.toDouble()
        setFpsColor(false)
        fpsLabel.text = if (fps >= 200) "warp" else "$value fps"
    }
    val fpsLabel = Button("$fps fps", rounded = true, color = ElementColor.Info)

    val stepLabel = Label()

    val grainsController =
        noContextColumnsController<Grain, GrainDisplayController>(model.grains)
        { grain, previous ->
            val controller = previous ?: GrainDisplayController(grain)
            controller.body.root.onclick = {
                    simulationViewController.selectedGrain = controller.data
                    Unit
                }
            controller
        }

    val behaviourController =
        columnsController(model.behaviours, simulator)
        { behaviour, previous ->
            previous ?: BehaviourRunController(behaviour, simulator).wrap { controller ->
                controller.onSpeedChange = { behaviour, speed -> simulator.setSpeed(behaviour, speed) }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val selectedGrainController = GrainSelectController(null, model.grains)

    val chartCanvas = canvas {}

    var simulationViewController: SimulatorViewController by observable(createSimulationViewController())
    { _, old, new ->
        if (old != new) {
            simulationView.body = listOf(simulationViewController)
        }
    }

    val simulationView = Column(simulationViewController.container, size = ColumnSize.Full)

    override val container = Columns(
        Column(nameController, size = ColumnSize.OneThird),
        Column(descriptionController, size = ColumnSize.TwoThirds),
        Column(Title("Grains", TextSize.S4), grainsController, desktopSize = ColumnSize.S2),
        Column(
            Columns(
                Column(
                    Level(
                        center = listOf(
                            Field(
                                Control(rewindButton), Control(runButton), Control(stepButton), Control(stopButton),
                                addons = true
                            ),
                            Field(Control(fpsSlider), Control(fpsLabel), grouped = true),
                            stepLabel,
                            toggleChartsButton,
                            view3dButton
                        )
                    ), size = ColumnSize.Full
                ),
                simulationView,
                Column(div(chartCanvas, classes = "has-text-centered"), size = ColumnSize.Full),
                multiline = true
            ),
            desktopSize = ColumnSize.S6
        ),
        Column(Title("Behaviours", TextSize.S4), behaviourController, desktopSize = ColumnSize.S4),
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
        animationCallback(0.0)
        fpsSlider.root.style.width = "200px"
    }

    fun setFpsColor(slowed: Boolean) {
        val color = when {
            fps >= 200.0 -> ElementColor.Success
            slowed -> ElementColor.Warning
            else -> ElementColor.Info
        }
        fpsLabel.color = color
        fpsSlider.color = color
    }

    fun run() {
        if (!running) {
            running = true
            refreshButtons()
        }
    }

    fun animationCallback(timestamp: Double) {
        if (running) {
            val time = 1000 / fps
            val delta = timestamp - lastStepTimestamp

            lastFpsColorRefresh += 1
            if (lastFpsColorRefresh > fps) {
                setFpsColor(delta > time && lastRequestSkipped)
            }

            val refresh = fps >= 200 || lastStepTimestamp == 0.0 || delta >= time
            if (refresh) {
                lastStepTimestamp = timestamp
                executeStep(lastChartRefresh >= 10)
                lastChartRefresh += 1

                // if the document was removed, stop the simulation
                if (container.root.ownerDocument != container.root.getRootNode()) stop()
            }
            lastRequestSkipped = refresh

        }
        simulationViewController.animate()
        window.requestAnimationFrame(this::animationCallback)
    }

    fun step() {
        if (!running) {
            executeStep(true)
            refreshButtons()
        }
    }

    fun stop() {
        if (running) {
            running = false
            lastStepTimestamp = 0.0
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
        val applied = simulator.oneStep()

        simulationViewController.oneStep(applied)
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
            lastChartRefresh = 0
        }
    }

    fun toggleCharts() {
        presentCharts = !presentCharts
        toggleChartsButton.color = if (presentCharts) ElementColor.Info else ElementColor.Dark
        refreshChart()
    }

    fun toggle3d() {
        view3d = !view3d
        view3dButton.color = if (view3d) ElementColor.Info else ElementColor.Dark
        simulationViewController = createSimulationViewController()
    }

    private fun createSimulationViewController(): SimulatorViewController = when {
        view3d -> Simulator3dViewController(simulator, appContext)
        readOnly -> Simulator2dViewController(simulator)
        else -> SimulatorEditController(simulator) { ended, new, _ ->
            updatedSimulatorFromView(ended, new)
        }
    }.apply { refresh() }

    private fun updatedSimulatorFromView(ended: Boolean, new: Simulator) {
        simulator.resetCount()
        refreshCounts()
        if (ended) {
            data = data.copy(agents = new.initialAgents.toList())
        }
    }

    fun refreshButtons() {
        rewindButton.disabled = running
        rewindButton.color = if (simulator.step == 0) ElementColor.Primary else ElementColor.Danger
        runButton.loading = running
        runButton.disabled = running
        stepButton.disabled = running
        stopButton.disabled = !running
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
        simulationViewController.refresh()
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
            lastChartRefresh = 0
        } else {
            chart.data.datasets = emptyArray()
            chart.update()
        }
    }
}
