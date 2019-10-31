package com.centyllion.client.controller.model

import bulma.Box
import bulma.BulmaElement
import bulma.Button
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Controller
import bulma.Div
import bulma.ElementColor
import bulma.Field
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.MultipleController
import bulma.TextSize
import bulma.Title
import bulma.WrappedController
import bulma.canvas
import bulma.columnsController
import bulma.extension.Slider
import bulma.iconButton
import bulma.noContextColumnsController
import bulma.textButton
import bulma.wrap
import chartjs.Chart
import chartjs.ChartUpdateConfig
import chartjs.LineChartConfig
import chartjs.LineChartOptions
import chartjs.LineChartPlot
import chartjs.LineDataSet
import chartjs.LinearAxisOptions
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.push
import com.centyllion.client.download
import com.centyllion.client.page.BulmaPage
import com.centyllion.client.stringHref
import com.centyllion.client.toggleElementToFullScreen
import com.centyllion.model.Asset3d
import com.centyllion.model.Behaviour
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.Simulation
import com.centyllion.model.Simulator
import kotlin.browser.window
import kotlin.properties.Delegates.observable

class SimulationRunController(
    simulation: Simulation, model: GrainModel,
    val page: BulmaPage, readOnly: Boolean = false,
    val onChangeSpeed: (behaviour: Behaviour, speed: Double, controller: SimulationRunController) -> Unit =
        { _, _, _ -> },
    val onUpdate: (old: Simulation, new: Simulation, controller: SimulationRunController) -> Unit =
        { _, _, _ -> }
) : Controller<Simulation, GrainModel, BulmaElement> {

    override var data: Simulation by observable(simulation) { _, old, new ->
        if (old != new) {
            nameController.data = new.name
            descriptionController.data = new.description
            currentSimulator = Simulator(context, new)
            simulationViewController.data = currentSimulator
            asset3dController.data = new.assets
            running = false
            onUpdate(old, new, this@SimulationRunController)
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            data = data.cleaned(new)
            grainsController.data = new.grains
            behaviourController.data = new.behaviours
            selectedGrainController.context = new.grains
            selectedGrainController.data = new.grains.firstOrNull()
            currentSimulator = Simulator(new, data)
            behaviourController.context = currentSimulator
            simulationViewController.data = currentSimulator
            running = false
            refresh()
        }
    }

    override var readOnly: Boolean by observable(readOnly) { _, old, new ->
        if (old != new) {
            nameController.readOnly = new
            descriptionController.readOnly = new
            simulationViewController.readOnly = new
            asset3dController.readOnly = new
            assetsColumn.hidden = new
        }
    }

    private var currentSimulator = Simulator(context, data)

    val simulator get() = currentSimulator

    private var running: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            simulationViewController.running = new
            if (new) {
                window.requestAnimationFrame(this::animationCallback)
                refreshButtons()
            } else {
                lastStepTimestamp = 0.0
                refresh()
            }
        }
    }

    val isRunning get() = running

    private var fps = 50.0
    private var lastStepTimestamp = 0.0
    private var lastRequestSkipped = true
    private var lastFpsColorRefresh = 0
    private var lastChartRefresh = 0

    private var presentCharts = false

    val nameController = EditableStringController(data.name, page.i18n("Simulation Name"), readOnly)
    { _, new, _ ->
        data = data.copy(name = new)
    }

    val descriptionController = EditableStringController(data.description, page.i18n("Description"), readOnly)
    { _, new, _ ->
        data = data.copy(description = new)
    }

    // simulation execution controls
    val rewindButton = iconButton(Icon("fast-backward"), ElementColor.Danger, rounded = true) { reset() }
    val runButton = iconButton(Icon("play"), ElementColor.Primary, rounded = true) { run() }
    val stepButton = iconButton(Icon("step-forward"), ElementColor.Primary, rounded = true) { step() }
    val stopButton = iconButton(Icon("stop"), ElementColor.Warning, rounded = true) { stop() }
    val toggleChartsButton = iconButton(Icon("chart-line"), ElementColor.Dark, rounded = true) { toggleCharts() }

    val fpsSlider = Slider(fps.toString(), "1", "200", "1", color = ElementColor.Info, circle = true) { _, value ->
        fps = value.toDouble()
        setFpsColor(false)
        fpsLabel.text = if (fps >= 200) "warp" else "$value fps"
    }.apply {
        root.style.width = "100px"
    }

    val stepLabel = Label()

    val fpsLabel = Button("$fps fps", rounded = true, color = ElementColor.Info)

    val resetOrbitButton = iconButton(Icon("compress-arrows-alt"), rounded = true) {
        simulationViewController.resetCamera()
    }

    val fullscreenButton = iconButton(Icon("expand-arrows-alt"), rounded = true) {
        toggleElementToFullScreen(simulationColumns.root)
    }

    val grainsController: MultipleController<Grain, GrainModel, Columns, Column, WrappedController<Grain, GrainModel, Box, Column>> =
        columnsController(model.grains, model)
        { grain, previous ->
            previous ?: GrainRunController(grain, context).wrap {
                it.container.root.onclick = { _ ->
                    simulationViewController.selectedGrainController.data = it.data
                    Unit
                }
                Column(it.container, size = ColumnSize.Full)
            }
        }

    val behaviourController =
        columnsController(model.behaviours, currentSimulator)
        { behaviour, previous ->
            previous ?: BehaviourRunController(behaviour, currentSimulator).wrap { controller ->
                controller.onValidate = { behaviour, speed ->
                    onChangeSpeed(behaviour, speed, this@SimulationRunController)
                }
                controller.onSpeedChange = { behaviour, speed ->
                    currentSimulator.setSpeed(behaviour, speed)
                }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val selectedGrainController = GrainSelectController(model.grains.firstOrNull(), model.grains, page)

    val chartCanvas = canvas {}

    var exportCsvButton = iconButton(Icon("file-csv"), ElementColor.Info, true, disabled = true) {
        val header = "step,${context.grains.map { if (it.name.isNotBlank()) it.name else it.id }.joinToString(",")}"
        val content = (0 until currentSimulator.step).joinToString("\n") { step ->
            val counts = context.grains.map { currentSimulator.grainCountHistory[it]?.get(step) ?: 0 }.joinToString(",")
            "$step,$counts"
        }
        download("counts.csv", stringHref("$header\n$content"))
    }

    val chartContainer = Div(chartCanvas, Level(center = listOf(exportCsvButton)), classes = "has-text-centered").apply {
        hidden = !presentCharts
    }

    var simulationViewController = Simulator3dViewController(currentSimulator, page, readOnly) { ended, new, _ ->
        updatedSimulatorFromView(ended, new)
    }

    val simulationView = Column(simulationViewController.container, size = ColumnSize.Full)

    val addAsset3dButton = iconButton(Icon("plus"), ElementColor.Primary, true) {
        this.data = data.copy(assets = data.assets + Asset3d(""))
    }

    val settingsButton = Button(null, Icon("cog"), ElementColor.Primary, true) {
        val settingsController = SimulationSettingsController(data.settings, page)
        val okButton = textButton(page.i18n("Save"), ElementColor.Success, disabled = true) {
            if (settingsController.data != data.settings) {
                data = data.copy(settings = settingsController.data)
            }
        }
        settingsController.onUpdate = { old, new, _ ->
            okButton.disabled = new == data.settings
        }

        page.modalDialog(
            page.i18n("Simulation Settings"), listOf(settingsController),
            okButton, textButton(page.i18n("Cancel"))
        )
    }

    val asset3dController =
        noContextColumnsController(simulation.assets) { asset, previous ->
            previous ?: Asset3dEditController(asset, readOnly, page).wrap { controller ->
                controller.onUpdate = { old, new, _ ->
                    if (old != new) data = data.updateAsset(old, new)
                }
                controller.onDelete = { data = data.dropAsset(it) }
                Column(controller, size = ColumnSize.Half)
            }
        }

    val assetsColumn = Column(
        Level(
            left = listOf(Title(page.i18n("Assets"), TextSize.S4)),
            right = listOf(addAsset3dButton, settingsButton),
            mobile = true
        ),
        asset3dController, desktopSize = ColumnSize.Full
    ).apply { hidden = readOnly }

    val simulationColumns: Columns = Columns(
        Column(
            Level(
                center = listOf(
                    Field(
                        Control(rewindButton), Control(runButton), Control(stepButton), Control(stopButton),
                        addons = true
                    ),
                    Control(stepLabel),
                    Field(Control(fpsSlider), Control(fpsLabel), grouped = true)
                ),
                right = listOf(
                    Field(Control(toggleChartsButton), Control(resetOrbitButton), Control(fullscreenButton), grouped = true)
                )
            ), size = ColumnSize.Full
        ),
        simulationView,
        Column(chartContainer, size = ColumnSize.Full),
        multiline = true
    )

    val grainColumn = Column(Title(page.i18n("Grains"), TextSize.S4), grainsController, desktopSize = ColumnSize.S2).apply {
        root.style.height = "80vh"
        root.style.overflowY = "auto"
    }
    val simulationColumn = Column(simulationColumns, desktopSize = ColumnSize.S6)
    val behaviourColumn = Column(Title(page.i18n("Behaviours"), TextSize.S4), behaviourController, desktopSize = ColumnSize.S4).apply {
        root.style.height = "80vh"
        root.style.overflowY = "auto"
    }


    override val container = Columns(
        Column(nameController, size = ColumnSize.OneThird),
        Column(descriptionController, size = ColumnSize.TwoThirds),
        grainColumn,
        simulationColumn,
        behaviourColumn,
        assetsColumn,
        multiline = true, centered = true
    )

    val chart = Chart(chartCanvas.root, LineChartConfig(
        options = LineChartOptions().apply {
            animation.duration = 0
            scales.xAxes = arrayOf(LinearAxisOptions())
            scales.yAxes = arrayOf(LinearAxisOptions())
        }
    ))

    init {
        window.setTimeout({ animationCallback(0.0) }, 250)
    }

    fun hideSides(hidden: Boolean ) {
        grainColumn.hidden = hidden
        simulationColumn.desktopSize = if (hidden) ColumnSize.Full else ColumnSize.S6
        behaviourColumn.hidden = hidden
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
        running = true
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


            }
            lastRequestSkipped = refresh
            window.requestAnimationFrame(this::animationCallback)
        }
    }

    fun step() {
        if (!running) {
            executeStep(true)
            simulationViewController.render()
            refreshButtons()
        }
    }

    fun stop() {
        running = false
    }

    fun reset() {
        if (!running) {
            currentSimulator.reset()
            refresh()
        }
    }

    private fun executeStep(updateChart: Boolean) {
        val (applied, dead) = currentSimulator.oneStep()
        simulationViewController.oneStep(applied, dead)
        refreshCounts()

        // appends data to charts
        if (presentCharts) {
            val counts = currentSimulator.lastGrainsCount().filter { context.doesGrainCountCanChange(it.key) }
            chart.data.datasets.zip(counts.values) { set: LineDataSet, i: Int ->
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

    private fun updatedSimulatorFromView(ended: Boolean, new: Simulator) {
        refreshCounts()
        // only update simulation if initial simulation was modified
        if (currentSimulator.step == 0 && ended) {
            currentSimulator.resetCount()
            data = data.copy(agents = new.initialAgents.toList())
        }
    }

    fun refreshButtons() {
        rewindButton.disabled = running
        rewindButton.color = if (currentSimulator.step == 0) ElementColor.Primary else ElementColor.Danger
        runButton.loading = running
        runButton.disabled = running
        stepButton.disabled = running
        stopButton.disabled = !running

        exportCsvButton.disabled = running || currentSimulator.step <= 0
    }

    fun refreshCounts() {
        // refreshes step count
        stepLabel.text = "${currentSimulator.step}"

        // refreshes grain counts
        val counts = currentSimulator.lastGrainsCount().values
        grainsController.dataControllers.zip(counts) { controller, count ->
            val source = controller.source
            if (source is GrainRunController) source.count = count
        }
    }

    override fun refresh() {
        refreshButtons()
        simulationViewController.refresh()
        refreshCounts()
        refreshChart()
    }

    fun refreshChart() {
        chartContainer.hidden = !presentCharts
        if (presentCharts) {
            // refreshes charts
            val previous = chart.data.datasets.map { it.key to it }.toMap()
            chart.data.datasets = currentSimulator.grainCountHistory.filter { context.doesGrainCountCanChange(it.key) }.map {
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
            lastChartRefresh = 0
        }
        chart.update()
    }

    fun dispose() {
        stop()
        simulationViewController.dispose()
    }
}
