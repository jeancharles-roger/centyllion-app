package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.EditableStringController
import com.centyllion.client.controller.GrainModelEditController
import com.centyllion.client.controller.SimulationRunController
import com.centyllion.model.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class ShowPage(val context: AppContext) : BulmaElement {

    val saveIcon = "cloud-upload-alt"
    val shareIcon = "share"

    val api = context.api

    private var undoModel = false

    private var modelHistory: List<GrainModelDescription> by observable(emptyList()) { _, _, new ->
        undoModelButton.disabled = new.isEmpty()
    }

    private var modelFuture: List<GrainModelDescription> by observable(emptyList()) { _, _, new ->
        redoModelButton.disabled = new.isEmpty()
    }

    private var simulationHistory: List<SimulationDescription> by observable(emptyList()) { _, _, new ->
        undoSimulationButton.disabled = new.isEmpty()
    }

    private var simulationFuture: List<SimulationDescription> by observable(emptyList()) { _, _, new ->
        redoSimulationButton.disabled = new.isEmpty()
    }

    private var undoSimulation = false

    private var originalModel: GrainModelDescription = emptyGrainModelDescription

    var model: GrainModelDescription by observable(emptyGrainModelDescription) { _, old, new ->
        if (new != old) {
            if (undoModel) {
                modelFuture += old
            } else {
                modelHistory += old
                if (modelFuture.lastOrNull() == new) {
                    modelFuture = modelFuture.dropLast(1)
                } else {
                    modelFuture = emptyList()
                }
            }

            val readonly = model.id.isNotEmpty() && model.info.userId != context.me?.id
            modelController.readOnly = readonly
            modelController.data = new.model
            modelNameController.readOnly = readonly
            modelNameController.data = new.model.name
            modelDescriptionController.readOnly = readonly
            modelDescriptionController.data = new.model.description
            simulationController.context = new.model
            refreshButtons()
        }
    }

    val modelNameController = EditableStringController(model.model.name, "Name") { _, new, _ ->
        model = model.copy(model = model.model.copy(name = new))
    }

    val modelDescriptionController = EditableStringController(model.model.description, "Description") { _, new, _ ->
        model = model.copy(model = model.model.copy(description = new))
    }

    val modelController = GrainModelEditController(model.model) { old, new, _ ->
        if (old != new) {
            model = model.copy(model = new)
        }
    }

    var originalSimulation: SimulationDescription = emptySimulationDescription

    var simulation: SimulationDescription by observable(emptySimulationDescription) { _, old, new ->
        if (new != old) {
            if (undoSimulation) {
                simulationFuture += old
            } else {
                simulationHistory += old
                if (simulationFuture.lastOrNull() == new) {
                    simulationFuture = simulationFuture.dropLast(1)
                } else {
                    simulationFuture = emptyList()
                }
            }

            val readonly = simulation.id.isNotEmpty() && simulation.info.userId != context.me?.id
            simulationController.readOnly = readonly
            simulationController.data = new.simulation
            refreshButtons()
        }
    }

    val simulationController = SimulationRunController(emptySimulation, emptyModel) { old, new, _ ->
        if (old != new) {
            simulation = simulation.copy(simulation = new)
        }
    }

    val undoModelButton = iconButton(Icon("undo"), ElementColor.Primary, rounded = true) {
        val restoredModel = modelHistory.last()
        modelHistory = modelHistory.dropLast(1)
        undoModel = true
        modelController.data = restoredModel.model
        undoModel = false
    }

    val redoModelButton = iconButton(Icon("redo"), ElementColor.Primary, rounded = true) {
        val restoredModel = modelFuture.last()
        modelController.data = restoredModel.model
    }

    val undoSimulationButton = iconButton(Icon("undo"), ElementColor.Primary, rounded = true) {
        val restoredSimulation = simulationHistory.last()
        simulationHistory = simulationHistory.dropLast(1)
        undoSimulation = true
        simulationController.data = restoredSimulation.simulation
        undoSimulation = false
    }

    val redoSimulationButton = iconButton(Icon("redo"), ElementColor.Primary, rounded = true) {
        val restoredSimulation = simulationFuture.last()
        simulationFuture = simulationFuture.dropLast(1)
        simulationController.data = restoredSimulation.simulation
    }

    val saveButton = Button("Save", Icon(saveIcon), color = ElementColor.Primary, rounded = true) { save() }

    val publishButton = Button("Publish", Icon(shareIcon), rounded = true) { togglePublication() }

    val modelPage = TabPage(TabItem("Model", "boxes"), modelController)
    val simulationPage = TabPage(TabItem("Simulation", "play"), simulationController)

    val tools = Field(
        Control(undoModelButton), Control(redoModelButton), Control(saveButton), Control(publishButton),
        grouped = true
    )

    val tabs = Tabs(boxed = true)

    val editionTab = TabPages(modelPage, simulationPage, tabs = tabs, initialTabIndex = 1) {
        refreshButtons()
    }

    val container: BulmaElement = Columns(
        Column(modelNameController, size = ColumnSize.S2),
        Column(modelDescriptionController, size = ColumnSize.S6),
        Column(tools, size = ColumnSize.S4),
        Column(editionTab, size = ColumnSize.Full),
        multiline = true
    )

    override val root: HTMLElement = container.root

    init {
        val params = URLSearchParams(window.location.search)
        val simulationId = params.get("simulation")
        val modelId = params.get("model")

        // Selects model tab if there no simulation provided
        if (simulationId == null) editionTab.selectPage(modelPage)

        // selects the pair simulation and model to run
        val result = when {
            // if there is a simulation id, use it to find the model
            simulationId != null && simulationId.isNotEmpty() ->
                context.api.fetchSimulation(simulationId).then { simulation ->
                    context.api.fetchGrainModel(simulation.modelId).then { simulation to it }
                }.then { it }

            // if there is a model id, use it to list all simulation and take the first one
            modelId != null && modelId.isNotEmpty() ->
                context.api.fetchGrainModel(modelId).then { model ->
                    context.api.fetchSimulations(model.id, true).then { simulations ->
                        (simulations.firstOrNull() ?: emptySimulationDescription) to model
                    }
                }.then { it }

            else -> Promise.reject(Exception("No simulation found"))
        }

        result.then {
            setModelAndSimulation(it.second, it.first)
        }.catch {
            context.error(it)
        }
    }

    fun setModelAndSimulation(model: GrainModelDescription, simulation: SimulationDescription) {
        this.originalSimulation = simulation
        this.simulation = originalSimulation
        this.simulationHistory = emptyList()
        this.simulationFuture = emptyList()

        this.originalModel = model
        this.model = originalModel
        this.modelHistory = emptyList()
        this.modelFuture = emptyList()
        refreshButtons()
    }

    fun save() {
        val needModelSave = model != originalModel
        if (needModelSave && model.id.isEmpty()) {
            // The model needs to be created first
            api.saveGrainModel(model.model)
                .then { newModel ->
                    originalModel = newModel
                    model = newModel
                    // Saves the simulation
                    api.saveSimulation(newModel.id, simulation.simulation)
                }.then { newSimulation ->
                    originalSimulation = newSimulation
                    simulation = newSimulation
                    refreshButtons()
                    context.message("Model ${model.model.name} and simulation ${simulation.simulation.name} saved")
                    Unit
                }.catch {
                    this.context.error(it)
                    Unit
                }
        } else {

            // Save the model if needed
            if (needModelSave) {
                api.updateGrainModel(model).then {
                    originalModel = model
                    refreshButtons()
                    context.message("Model ${model.model.name} saved")
                    Unit
                }.catch {
                    this.context.error(it)
                    Unit
                }
            }

            // Save the simulation
            if (simulation != originalSimulation) {
                if (simulation.id.isEmpty()) {
                    // simulation must be created
                    api.saveSimulation(model.id, simulation.simulation).then { newSimulation ->
                        originalSimulation = newSimulation
                        simulation = newSimulation
                        refreshButtons()
                        context.message("Simulation ${simulation.simulation.name} saved")
                        Unit
                    }.catch {
                        this.context.error(it)
                        Unit
                    }
                } else {
                    // saves the simulation
                    api.updateSimulation(simulation).then {
                        originalSimulation = simulation
                        refreshButtons()
                        context.message("Simulation ${simulation.simulation.name} saved")
                        Unit
                    }.catch {
                        this.context.error(it)
                        Unit
                    }
                }
            }
        }
    }

    private val canPublish get() = model.info.readAccess || simulation.info.readAccess

    fun togglePublication() {
        model = model.copy(info = model.info.copy(readAccess = canPublish))
        simulation = simulation.copy(info = simulation.info.copy(readAccess = canPublish))
        save()
    }

    fun refreshButtons() {
        when (editionTab.selectedPage) {
            modelPage -> {
                val readonly = model.id.isNotEmpty() && model.info.userId != context.me?.id
                tools.body = if (readonly) emptyList() else listOf(
                    Control(undoModelButton),
                    Control(redoModelButton),
                    Control(saveButton),
                    Control(publishButton)
                )
            }
            simulationPage -> {
                val readonly = simulation.id.isNotEmpty() && simulation.info.userId != context.me?.id
                tools.body = if (readonly) emptyList() else listOf(
                    Control(undoSimulationButton),
                    Control(redoSimulationButton),
                    Control(saveButton),
                    Control(publishButton)
                )
            }
        }

        undoModelButton.disabled = modelHistory.isEmpty()
        redoModelButton.disabled = modelFuture.isEmpty()
        undoSimulationButton.disabled = simulationHistory.isEmpty()
        redoSimulationButton.disabled = simulationFuture.isEmpty()
        saveButton.disabled = model == originalModel && simulation == originalSimulation
        publishButton.title = if (!canPublish) "Un-Publish" else "Publish"
    }
}
