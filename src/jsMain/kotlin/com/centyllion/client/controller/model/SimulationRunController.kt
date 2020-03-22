package com.centyllion.client.controller.model

import bulma.Box
import bulma.BulmaElement
import bulma.Button
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Controller
import bulma.ElementColor
import bulma.Icon
import bulma.Label
import bulma.Level
import bulma.MultipleController
import bulma.TextSize
import bulma.Title
import bulma.WrappedController
import bulma.columnsController
import bulma.extension.Slider
import bulma.iconButton
import bulma.noContextColumnsController
import bulma.textButton
import bulma.wrap
import com.centyllion.client.controller.utils.SearchController
import com.centyllion.client.controller.utils.filtered
import com.centyllion.client.download
import com.centyllion.client.page.BulmaPage
import com.centyllion.client.stringHref
import com.centyllion.client.toFixed
import com.centyllion.client.toggleElementToFullScreen
import com.centyllion.model.Asset3d
import com.centyllion.model.Behaviour
import com.centyllion.model.Field
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.Simulation
import com.centyllion.model.Simulator
import com.centyllion.model.behaviourIcon
import com.centyllion.model.fieldIcon
import com.centyllion.model.grainIcon
import org.w3c.dom.HTMLElement
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import kotlin.browser.window
import kotlin.properties.Delegates.observable
import bulma.Field as BField

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
            currentSimulator = Simulator(context, new)
            simulationViewController.data = currentSimulator
            asset3dController.data = new.assets
            running = false
            grainChart.data = createGrainChart()
            fieldChart.data = createFieldChart()
            onUpdate(old, new, this@SimulationRunController)
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            data = data.cleaned(new)
            fieldsController.data = new.fields.filtered(searchController.data)
            grainsController.data = new.grains.filtered(searchController.data)
            behavioursController.data = new.behaviours.filtered(searchController.data)
            selectedGrainController.context = new.grains
            selectedGrainController.data = new.grains.firstOrNull()
            currentSimulator = Simulator(new, data)
            behavioursController.context = currentSimulator
            simulationViewController.data = currentSimulator
            grainChart.data = createGrainChart()
            fieldChart.data = createFieldChart()
            running = false
            refresh()
        }
    }

    override var readOnly: Boolean by observable(readOnly) { _, old, new ->
        if (old != new) {
            simulationViewController.readOnly = new
            asset3dController.readOnly = new
            assetsColumn.hidden = new
        }
    }

    var filterGrainsInChart: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            grainChart.data = createGrainChart()
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

    private var presentCharts = false

    // simulation execution controls
    val rewindButton = iconButton(Icon("fast-backward"), ElementColor.Danger, rounded = true) { reset() }
    val runButton = iconButton(Icon("play"), ElementColor.Primary, rounded = true) { run() }
    val stepButton = iconButton(Icon("step-forward"), ElementColor.Primary, rounded = true) { step() }
    val stopButton = iconButton(Icon("stop"), ElementColor.Warning, rounded = true) { stop() }
    val toggleChartsButton = iconButton(
        Icon("chart-line"), ElementColor.Info, rounded = true, light = presentCharts
    ) { toggleCharts() }

    val fpsSlider = Slider(
        fps.toString(), "1", "200", "1", color = ElementColor.Info, circle = true
    ) { _, value ->
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
        val fullscreen = toggleElementToFullScreen(simulationColumns.root)
        if (fullscreen) {
            simulationViewController.resizeToFullscreen()
        } else {
            // uses a timer to wait for the canvas to be connected to it's parent
            window.setTimeout( { simulationViewController.resize() }, 1000)
        }
    }

    val searchController: SearchController = SearchController(page) { _, filter ->
        fieldsController.data = context.fields.filtered(filter)
        grainsController.data = context.grains.filtered(filter)
        behavioursController.data = context.behaviours.filtered(filter)
    }

    val fieldTitle = Level(
        left = listOf(Icon(fieldIcon), Title(page.i18n("Fields"), TextSize.S4)),
        mobile = true
    )

    val fieldsController: MultipleController<
        Field, Unit, Columns, Column, WrappedController<Field, Unit, Box, Column>
    > = noContextColumnsController(model.fields)
        { field, previous ->
            previous ?: FieldRunController(field).wrap { controller ->
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val grainTitle = Level(
        left = listOf(Icon(grainIcon), Title(page.i18n("Grains"), TextSize.S4)),
        mobile = true
    )

    val grainsController: MultipleController<
            Grain, GrainModel, Columns, Column, WrappedController<Grain, GrainModel, Box, Column>
    > = columnsController(model.grains, model)
        { grain, previous ->
            previous ?: GrainRunController(page, grain, context).wrap {
                it.container.root.onclick = { _ ->
                    simulationViewController.selectedGrainController.data = it.data
                    Unit
                }
                Column(it.container, size = ColumnSize.Full)
            }
        }

    val behaviourTitle = Level(
        left = listOf(Icon(behaviourIcon), Title(page.i18n("Behaviours"), TextSize.S4)),
        mobile = true
    )

    val behavioursController =
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

    var exportCsvButton = iconButton(Icon("file-csv"), ElementColor.Info, true, disabled = true) {
        val header = "step,${context.grains.map { if (it.name.isNotBlank()) it.name else it.id }.joinToString(",")}"
        val content = (0 until currentSimulator.step).joinToString("\n") { step ->
            val counts = context.grains.map { currentSimulator.grainCountHistory[it]?.get(step) ?: 0 }.joinToString(",")
            "$step,$counts"
        }
        download("counts.csv", stringHref("$header\n$content"))
    }

    val grainChart = ChartController(page, page.i18n("Grains"), createGrainChart(), (window.innerWidth/2 to 400))

    val fieldChart = ChartController(page, page.i18n("Fields"), createFieldChart(), (window.innerWidth/2 to 400))

    val chartContainer = Columns(
        Column(grainChart, size = ColumnSize.Half),
        Column(fieldChart, size = ColumnSize.Half),
        Column(Level(center = listOf(exportCsvButton)), size = ColumnSize.Full)
    ).apply { hidden = !presentCharts }

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
        settingsController.onUpdate = { _, new, _ ->
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

    val selectorColumn = Column(
        searchController,
        fieldTitle,
        fieldsController,
        grainTitle,
        grainsController,
        behaviourTitle,
        behavioursController,
        size = ColumnSize.OneThird
    ).apply {
        root.style.height = "80vh"
        root.style.overflowY = "auto"
    }

    val simulationColumns: Columns = Columns(
        Column(
            Level(
                center = listOf(
                    BField(
                        Control(rewindButton), Control(runButton), Control(stepButton), Control(stopButton),
                        addons = true
                    ),
                    Control(stepLabel),
                    BField(Control(fpsSlider), Control(fpsLabel), grouped = true)
                ),
                right = listOf(
                    BField(Control(toggleChartsButton), Control(resetOrbitButton), Control(fullscreenButton), grouped = true)
                )
            ), size = ColumnSize.Full
        ),
        simulationView,
        multiline = true
    )

    val assetsColumn = Column(
        Level(
            left = listOf(Title(page.i18n("Assets"), TextSize.S4)),
            right = listOf(addAsset3dButton, settingsButton),
            mobile = true
        ),
        asset3dController, desktopSize = ColumnSize.Full
    ).apply { hidden = readOnly }

    val simulationColumn = Column(simulationColumns, size = ColumnSize.TwoThirds)

    override val container = Columns(
        selectorColumn,
        simulationColumn,
        Column(chartContainer, size = ColumnSize.Full),
        assetsColumn,
        multiline = true, centered = true
    )

    /** This observable is here to compute the correct size for canvas */
    private val sizeObservable = MutationObserver { _, o ->
        resize()
        o.disconnect()
    }

    init {
        sizeObservable.observe(
            simulationColumns.root,
            MutationObserverInit(childList = true, subtree = true, attributes = true)
        )
        window.setTimeout({ animationCallback(0.0) }, 250)
    }

    fun hideSides(hidden: Boolean ) {
        simulationColumn.size = if (hidden) ColumnSize.Full else ColumnSize.TwoThirds
        selectorColumn.hidden = hidden
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
                executeStep()
            }
            lastRequestSkipped = refresh
            window.requestAnimationFrame(this::animationCallback)
        }
    }

    fun step() {
        if (!running) {
            executeStep()
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
            grainChart.reset()
            fieldChart.reset()
            refresh()
        }
    }

    private fun executeStep() {
        val (applied, dead) = currentSimulator.oneStep()
        simulationViewController.oneStep(applied, dead)

        val grainCounts =
            if (filterGrainsInChart) currentSimulator.lastGrainsCount().filter { context.doesGrainCountCanChange(it.key) }
            else currentSimulator.lastGrainsCount()
        grainChart.push(
            currentSimulator.step,
            grainCounts.values,
            presentCharts
        )
        fieldChart.push(
            currentSimulator.step,
            currentSimulator.lastFieldAmount().values,
            presentCharts
        )

        refreshCounts()
    }

    private fun createGrainChart(): Chart {
        val counts = simulator.grainsCounts()
        val grains =
            if (filterGrainsInChart) context.grains.filter { context.doesGrainCountCanChange(it) }
            else context.grains
        return Chart(
            page.i18n("Step"),
            grains.map { ChartLine(label = it.label(true), color = it.color, initial = counts[it.id] ?: 0) }
        )
    }

    private fun createFieldChart(): Chart {
        val amounts = simulator.fieldAmounts()
        return Chart(
            page.i18n("Step"),
            context.fields.map { field ->
                ChartLine(
                    label = field.label(true), color = field.color,
                    initial = amounts[field.id] ?: 0,
                    value = { it.toFixed(3) }
                )
            }
        )
    }

    fun toggleCharts() {
        presentCharts = !presentCharts
        chartContainer.hidden = !presentCharts
        toggleChartsButton.light = presentCharts
        if (presentCharts) {
            resizeCharts()
            grainChart.refresh()
            fieldChart.refresh()
        }
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

        // refreshes field amounts
        val amounts = currentSimulator.lastFieldAmount().values
            fieldsController.dataControllers.zip(amounts) { controller, amount ->
            val source = controller.source
            if (source is FieldRunController) source.amount = amount
        }
    }

    override fun refresh() {
        fieldTitle.hidden = context.fields.isEmpty()
        grainTitle.hidden = context.grains.isEmpty()
        behaviourTitle.hidden = context.behaviours.isEmpty()
        refreshButtons()
        simulationViewController.refresh()
        refreshCounts()
    }

    fun resize() {
        simulationViewController.resize()
        if (presentCharts) resizeCharts()
    }

    fun resizeCharts() {
        val grainParent = grainChart.root.parentElement
        if (grainParent is HTMLElement) {
            grainChart.size = (grainParent.offsetWidth - 30) to 400
        }

        val fieldParent = fieldChart.root.parentElement
        if (fieldParent is HTMLElement) {
            fieldChart.size = (fieldParent.offsetWidth - 30) to 400
        }
    }

    fun dispose() {
        stop()
        simulationViewController.dispose()
    }
}
