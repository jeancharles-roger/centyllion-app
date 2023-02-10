package com.centyllion.client.controller.model

import bulma.*
import bulma.extension.Slider
import com.centyllion.client.page.BulmaPage
import com.centyllion.client.toggleElementToFullScreen
import com.centyllion.model.*
import kotlinx.browser.window
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import kotlin.properties.Delegates.observable
import bulma.Field as BField

class SimulationRunController(
    simulation: Simulation, model: GrainModel,
    val page: BulmaPage, readOnly: Boolean = false,
    val onStep: (simulator: Simulator) -> Unit = {},
    val onReset: (simulator: Simulator) -> Unit = {},
    val onUpdate: (old: Simulation, new: Simulation, controller: SimulationRunController) -> Unit = { _, _, _ -> }
) : Controller<Simulation, GrainModel, BulmaElement> {

    override var data: Simulation by observable(simulation) { _, old, new ->
        if (old != new) {
            currentSimulator = Simulator(context, new)
            simulationViewController.data = currentSimulator
            running = false
            onUpdate(old, new, this@SimulationRunController)
            refresh()
        }
    }

    override var context: GrainModel by observable(model) { _, old, new ->
        if (old != new) {
            data = data.cleaned(new)
            currentSimulator = Simulator(new, data)
            simulationViewController.data = currentSimulator
            running = false
            refresh()
        }
    }

    override var readOnly: Boolean by observable(readOnly) { _, old, new ->
        if (old != new) {
            simulationViewController.readOnly = new
        }
    }

    private var currentSimulator = Simulator(context, data)

    val simulator get() = currentSimulator

    private var running: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            simulationViewController.running = new
            if (new) {
                refreshButtons()
            } else {
                lastStepTimestamp = 0.0
                refresh()
            }
        }
    }

    val isRunning get() = running

    private var disposed = false

    private var fps = 50.0
    private var lastStepTimestamp = 0.0
    private var lastRequestSkipped = true
    private var lastFpsColorRefresh = 0

    // simulation execution controls
    val rewindButton = iconButton(Icon("fast-backward"), ElementColor.Danger, rounded = true) { reset() }
    val runButton = iconButton(Icon("play"), ElementColor.Primary, rounded = true) { run() }
    val stepButton = iconButton(Icon("step-forward"), ElementColor.Primary, rounded = true) { step() }
    val stopButton = iconButton(Icon("stop"), ElementColor.Warning, rounded = true) { stop() }

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

    var simulationViewController = Simulator3dViewController(
        currentSimulator, page, readOnly,
        onPointerMove = ::pickValuesAt,
        onUpdate = { ended, new, _ -> updatedSimulatorFromView(ended, new) }
    )

    val simulationView = Column(simulationViewController.container, size = ColumnSize.Full)

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
                    BField(Control(resetOrbitButton), Control(fullscreenButton), grouped = true)
                )
            ), size = ColumnSize.Full
        ),
        simulationView,
        multiline = true
    )

    override val container = simulationColumns

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
        }

        if (!disposed) window.requestAnimationFrame(this::animationCallback)
    }

    fun step() {
        if (!running) {
            executeStep()
            simulationViewController.render()
            refreshButtons()
        }
    }

    fun stop() {
        // creates thumbnail with last stop click for better thumbnails.
        running = false
    }

    fun reset() {
        if (!running) {
            currentSimulator.reset()
            refresh()
        }
    }

    private fun executeStep() {
        val (applied, dead) = simulator.oneStep()
        simulationViewController.oneStep(applied, dead)
        stepLabel.text = "${currentSimulator.step}"
        onStep(simulator)
    }


    private fun updatedSimulatorFromView(ended: Boolean, new: Simulator) {
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
    }

    fun pickValuesAt(x: Int, y: Int) {
        // TODO
    }

    override fun refresh() {
        refreshButtons()
        simulationViewController.refresh()
    }

    fun resize() {
        simulationViewController.resize()
    }

    fun dispose() {
        disposed = true
        stop()
        simulationViewController.dispose()
    }
}
