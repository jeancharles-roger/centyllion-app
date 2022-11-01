package com.centyllion.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposeWindow
import com.centyllion.i18n.loadLocales
import com.centyllion.model.*
import com.centyllion.ui.dialog.Dialog
import com.centyllion.ui.tabs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.math.roundToLong
import kotlin.system.measureTimeMillis

class AppState(
    override val window: ComposeWindow,
    override val scope: CoroutineScope,
    private val pathState: MutableState<Path?>,
) : AppContext {

    override val theme: AppTheme = AppTheme()

    val locales = loadLocales()

    // TODO search local from system
    private val localeState = mutableStateOf(locales.locale(locales.system))
    override var locale get() = localeState.value
        set(value) { localeState.value = value }

    /** Logs state (in first place to be ready if any log is provided during init) */
    private val logsState = mutableStateOf(listOf<AppLog>())
    override val logs: List<AppLog> get() = logsState.value

    override val path: Path?
        get() = pathState.value

    fun newModel(model: GrainModel = GrainModel.new(locale.i18n("Model"))) {
        pathState.value = null
        this.model = model
        selection = listOf(model)
        clearPastAndFuture()
        updateWindowName()
    }

    fun openPath(path: Path) {
        // TODO implement specific file for model+simulation
        importModel(path)
    }

    fun setPath(path: Path) {
        pathState.value = path
        // keep model as is to be saved in new path
        syncModelWithFile()
        updateWindowName()
    }

    override fun importModelAndSimulation(model: GrainModel, simulation: Simulation) {
        pathState.value = null
        this.model = model
        this.simulation = simulation
        selection = listOf(model)
        clearPastAndFuture()
        updateWindowName()
    }

    fun importModel(path: Path) {
        pathState.value = path
        model = loadModel(path)
        selection = listOf(model)
        clearPastAndFuture()
        updateWindowName()
    }

    fun importSimulation(path: Path) {
        simulation = loadSimulation(path)
    }

    private var past: List<GrainModel> = emptyList()
    private val canUndoState = mutableStateOf(false)
    val canUndo get() = canUndoState.value

    private var future: List<GrainModel> = emptyList()
    private val canRedoState = mutableStateOf(false)
    val canRedo get() = canRedoState.value

    private var lastModelModification: Long = Long.MIN_VALUE
    private val modelState = mutableStateOf(path?.let { loadModel(it) } ?: GrainModel.new(locale.i18n("Model")))

    override var model: GrainModel
        get() = modelState.value
        set(value) {
            // checks if model need updating
            // synchronizes model update
            synchronized(this) {
                // store previous model in past
                past = past + model
                canUndoState.value = past.isNotEmpty()
                // reset future (comparing previous value with current can be really costly)
                future = emptyList()

                canRedoState.value = future.isNotEmpty()

                // sets values
                modelState.value = value

                // updates selection
                selection = selection.mapNotNull { value.findWithUuid(it.uuid) }

                simulator = Simulator(model, simulation)

                refresh()
            }
        }

    private val simulationState = mutableStateOf(Simulation.empty)
    override var simulation: Simulation
        get() = simulationState.value
        set(value) {
            simulationState.value = value
            simulator = Simulator(model, value)
            stepState.value = 0
            grainCountsState.value = simulator.grainsCounts()
            fieldAmountsState.value = simulator.fieldAmounts()
        }

    private val runningState = mutableStateOf(false)
    override var running get() = runningState.value
        set(value) { runningState.value = value }

    private val stepState = mutableStateOf(0)
    override val step get() = stepState.value

    private val speedState = mutableStateOf(.75f)
    var speed get() = speedState.value
        set(value) { speedState.value = value }

    private val simulatorState = mutableStateOf(Simulator(model, simulation))
    override var simulator: Simulator
        get() = simulatorState.value
        set(value) {
            running = false
            simulatorState.value = value
        }

    private val grainCountsState = mutableStateOf(simulator.grainsCounts())
    val grainCounts get() = grainCountsState.value

    private val fieldAmountsState = mutableStateOf(simulator.fieldAmounts())
    val fieldAmounts get() = fieldAmountsState.value

    fun startStopSimulation() {
        if (running) stopSimulation()
        else startSimulation()
    }

    fun startSimulation() {
        if (!running) {
            running = true
            scope.launch(Dispatchers.IO) {
                while (running) {
                    step()
                    if (speed < 1f) delay((250*(1f-speed)).roundToLong().coerceAtMost(250))
                }
            }
        }
    }

    fun step() {
        simulator.oneStep()
        grainCountsState.value = simulator.grainsCounts()
        fieldAmountsState.value = simulator.fieldAmounts()
        stepState.value += 1
    }

    fun stopSimulation() {
        running = false
    }

    fun resetSimulation() {
        stepState.value = 0
        simulator.reset()
        grainCountsState.value = simulator.grainsCounts()
        fieldAmountsState.value = simulator.fieldAmounts()
    }
    /** Load model for given path. */
    private fun loadModel(path: Path): GrainModel =
        try {
            val source = path.readText(Charsets.UTF_8)
            Json.decodeFromString(GrainModel.serializer(), source)
        }
        catch (e: Throwable) {
            alert("Couldn't load model: $e")
            GrainModel.new(locale.i18n("Model"))
        }

    /** Load model for given path. */
    private fun loadSimulation(path: Path): Simulation =
        try {
            val source = path.readText(Charsets.UTF_8)
            Json.decodeFromString(Simulation.serializer(), source)
        }
        catch (e: Throwable) {
            alert("Couldn't load simulation: $e")
            Simulation.empty
        }

    private fun syncModelWithFile() {
        // saves value to file
        path?.let { destination ->
            lastModelModification = System.currentTimeMillis()
            scope.launch(context = Dispatchers.IO) {
                val waitTime = 1000L
                delay(waitTime)
                val time = System.currentTimeMillis()
                if (time > lastModelModification + waitTime) {
                    measureTimeMillis {
                        val target = Json.encodeToString(GrainModel.serializer(), model)
                        destination.writeText(target)
                    }.let { log("Saving model to ${destination.fileName} in $it ms.") }
                }
            }
        }
    }

    fun clearPastAndFuture() {
        past = emptyList()
        canUndoState.value = false
        future = emptyList()
        canRedoState.value = false
    }

    fun undo() {
        if (past.isNotEmpty()) {
            // retrieves model to restore
            val restored = past.last()
            // drops it
            past = past.dropLast(1)
            canUndoState.value = past.isNotEmpty()

            // saves current model in future
            future = future + model
            canRedoState.value = future.isNotEmpty()

            // set new model (not using property set the manages undo/redo
            modelState.value = restored

            refresh()
        }
    }

    fun redo() {
        if (future.isNotEmpty()) {
            // retrieves model to restore
            val restored = future.last()
            // drops it
            future = future.dropLast(1)
            canRedoState.value = future.isNotEmpty()

            // saves current model in future
            past = past + model
            canUndoState.value = past.isNotEmpty()

            // set new model (not using property set the manages undo/redo
            modelState.value = restored

            refresh()
        }
    }

    val selectionState = mutableStateOf<List<ModelElement>>(listOf())
    override var selection
        get() = selectionState.value
        set(value) {
            if (selectionState.value != value) {
                selectionState.value = value
            }
        }

    private val problemsState = mutableStateOf<List<Problem>>(emptyList())
    override val problems: List<Problem>
        get() = problemsState.value

    private val selectedProblemState = mutableStateOf<Problem?>(null)
    override var selectedProblem: Problem?
        get() = selectedProblemState.value
        set(value) { selectedProblemState.value = value }

    private fun updateProblems() {
        problemsState.value = model.diagnose(locale)
    }

    override fun showPrimarySelection() {
        selection.firstOrNull()?.let { primary ->
            TODO("Not implemented yet")
            /*
            componentExpanded = componentExpanded.toMutableSet().also { new ->
                model.parentsFor(primary).forEach {
                    if (!new.contains(it.id)) new.add(it.id)
                }
            }
             */
            // TODO needs to scroll
        }
    }

    override fun log(message: String) = addLog(message, Severity.Info)
    override fun warn(message: String) = addLog(message, Severity.Warning)
    override fun alert(message: String) = addLog(message, Severity.Severe)

    private fun addLog(message: String, severity: Severity) {
        val timestamp = System.currentTimeMillis()
        logsState.value += AppLog(timestamp, message, severity)
        // TODO log tab
        //if (southSelectedTab != LogTab) unseenLogsState.value += 1
    }

    fun clearLogs() {
        logsState.value = emptyList()
    }

    private fun updateWindowName() {
        window.title = buildString {
            append("Centyllion - ")
            if (path != null) append(path?.fileName)
            else append("<not saved>")
        }
    }

    override val centerTabs = listOf(PropertiesTab, SimulationTab, PlotterTab, LogsTab)

    private val centerSelectedTabState = mutableStateOf<Tab>(PropertiesTab)
    override var centerSelectedTab: Tab
        get() = centerSelectedTabState.value
        set(value) { centerSelectedTabState.value = value }

    private val currentDialogState = mutableStateOf<Dialog?>(null)
    override var currentDialog get() = currentDialogState.value
        set(value) { currentDialogState.value = value }

    private fun refresh() {
        updateProblems()
        syncModelWithFile()
    }

    init {
        updateWindowName()
    }
}
