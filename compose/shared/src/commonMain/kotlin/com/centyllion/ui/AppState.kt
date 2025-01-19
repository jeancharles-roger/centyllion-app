package com.centyllion.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.centyllion.i18n.Locales
import com.centyllion.model.*
import com.centyllion.ui.dialog.Dialog
import com.centyllion.ui.tabs.*
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.math.roundToLong

open class AppState(
    val scope: CoroutineScope,
    private val pathState: MutableState<PlatformFile?>,
) : AppContext {

    // TODO search local from system
    private val localeState = mutableStateOf(Locales.default)
    override var locale get() = localeState.value
        set(value) { localeState.value = value }

    /** Logs state (in first place to be ready if any log is provided during init) */
    private val logsState = mutableStateOf(listOf<AppLog>())
    override val logs: List<AppLog> get() = logsState.value

    override val path: PlatformFile?
        get() = pathState.value
       
    open fun updateName() { }
    
    fun newModel(model: ModelAndSimulation = emptyModel()) {
        pathState.value = null
        this.modelAndSimulation = model
        selection = emptyList()
        clearPastAndFuture()
        updateName()
    }

    suspend fun openPath(path: PlatformFile) {
        importModel(path)
    }

    fun setPath(path: PlatformFile) {
        pathState.value = path
        // keep model as is to be saved in new path
        //syncModelWithFile()
        updateName()
    }

    override fun importModelAndSimulation(model: ModelAndSimulation) {
        pathState.value = null
        this.modelAndSimulation = model
        selection = listOf()
        clearPastAndFuture()
        updateName()
    }

    suspend fun importModel(path: PlatformFile) {
        pathState.value = path
        modelAndSimulation = loadModel(path)
        selection = listOf()
        clearPastAndFuture()
        updateName()
    }

    private var past: List<ModelAndSimulation> = emptyList()
    private val canUndoState = mutableStateOf(false)
    val canUndo get() = canUndoState.value

    private var future: List<ModelAndSimulation> = emptyList()
    private val canRedoState = mutableStateOf(false)
    val canRedo get() = canRedoState.value

    private var lastModelModification: Long = Long.MIN_VALUE
    private val modelState = mutableStateOf(path?.let {
        runBlocking { loadModel(it) }
    } ?: emptyModel())

    override var modelAndSimulation: ModelAndSimulation
        get() = modelState.value
        set(value) {
            // checks if model need updating
            // synchronizes model update
            synchronized(this) {
                // store previous model in past
                past = past + modelAndSimulation
                canUndoState.value = past.isNotEmpty()
                // reset future (comparing previous value with current can be really costly)
                future = emptyList()

                canRedoState.value = future.isNotEmpty()

                // sets values
                modelState.value = value

                // updates selection
                selection = selection.mapNotNull { value.model.findElement(it.uuid) }

                simulator = Simulator(value.model, value.simulation)
                refresh()
            }
        }


    override var model: GrainModel
        get() = modelAndSimulation.model
        set(value) {
            modelAndSimulation = modelAndSimulation.updateModel(value)
        }

    override var simulation: Simulation
        get() = modelAndSimulation.simulation
        set(value) {
            modelAndSimulation = modelAndSimulation.updateSimulation(value)
        }

    override var expertMode: Boolean by mutableStateOf(false)

    override var running: Boolean by mutableStateOf(false)
    override var step: Int by mutableStateOf(0)
    var speed: Float by mutableStateOf(.75f)

    private val simulatorState = mutableStateOf(Simulator(modelAndSimulation.model, modelAndSimulation.simulation))
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
                    if (speed < 1f) delay((250 * (1f - speed)).roundToLong().coerceAtMost(250))
                }
            }
        }
    }

    fun step() {
        try {
            simulator.oneStep()
            grainCountsState.value = simulator.grainsCounts()
            fieldAmountsState.value = simulator.fieldAmounts()
            step += 1
        } catch (t: Throwable) {
            // TODO localize
            alert(t.message ?: "Unexpected error occurred")
        }

    }

    fun stopSimulation() {
        running = false
    }

    fun resetSimulation() {
        step = 0
        simulator.reset()
        grainCountsState.value = simulator.grainsCounts()
        fieldAmountsState.value = simulator.fieldAmounts()
    }

    fun emptyModel() = ModelAndSimulation(GrainModel.new(locale.i18n("Model")), Simulation.empty)

    /** Load model for given path. */
    private suspend fun loadModel(path: PlatformFile): ModelAndSimulation =
        try {
            val source = path.readBytes().toString(Charsets.UTF_8)
            Json.decodeFromString(ModelAndSimulation.serializer(), source)
        } catch (e: Throwable) {
            alert("Couldn't load model: $e")
            emptyModel()
        }


    /** Load model for given path. */
    private suspend fun loadSimulation(path: PlatformFile): Simulation =
        try {
            val source = path.readBytes().toString(Charsets.UTF_8)
            Json.decodeFromString(Simulation.serializer(), source)
        }
        catch (e: Throwable) {
            alert("Couldn't load simulation: $e")
            Simulation.empty
        }

    /* TODO support export file depending on target
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
                        val target = Json.encodeToString(ModelAndSimulation.serializer(), modelAndSimulation)
                        destination.writeText(target)
                    }.let { log("Saving model to ${destination.fileName} in $it ms.") }
                }
            }
        }
    }
    */

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
            future = future + modelAndSimulation
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
            past = past + modelAndSimulation
            canUndoState.value = past.isNotEmpty()

            // set new model (not using property set the manages undo/redo
            modelState.value = restored

            refresh()
        }
    }

    private val selectionState = mutableStateOf<List<ModelElement>>(listOf())

    override var selection: List<ModelElement>
        get() = selectionState.value
        set(value) {
            if (selectionState.value != value) {
                selectionState.value = value
            }
        }

    private val toolActiveState = mutableStateOf(false)
    var toolActive: Boolean
        get() = toolActiveState.value
        set(value) { toolActiveState.value = value }

    private val toolState = mutableStateOf(EditTool.Line)
    var tool: EditTool
        get() = toolState.value
        set(value) { toolState.value = value }

    private val toolSizeState = mutableStateOf(ToolSize.Small)
    var toolSize: ToolSize
        get() = toolSizeState.value
        set(value) { toolSizeState.value = value }

    private val problemsState = mutableStateOf<List<Problem>>(emptyList())
    override val problems: List<Problem>
        get() = problemsState.value

    private val selectedProblemState = mutableStateOf<Problem?>(null)
    override var selectedProblem: Problem?
        get() = selectedProblemState.value
        set(value) { selectedProblemState.value = value }

    private fun updateProblems() {
        problemsState.value = modelAndSimulation.model.diagnose(locale)
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

    override val centerTabs = listOf(ModelTab, SimulationTab, EnvironmentTab)

    private val centerSelectedTabState = mutableStateOf<Tab>(ModelTab)
    override var centerSelectedTab: Tab
        get() = centerSelectedTabState.value
        set(value) { centerSelectedTabState.value = value }

    override val eastTabs = listOf(PlotterTab, LogsTab)

    private val eastSelectedTabState = mutableStateOf<Tab>(PlotterTab)
    override var eastSelectedTab: Tab
        get() = eastSelectedTabState.value
        set(value) { eastSelectedTabState.value = value }

    private val currentDialogState = mutableStateOf<Dialog?>(null)
    override var currentDialog get() = currentDialogState.value
        set(value) { currentDialogState.value = value }

    private fun refresh() {
        updateProblems()
        //syncModelWithFile()
    }
}
