package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.GrainModelEditController
import com.centyllion.client.controller.SimulationRunController
import com.centyllion.client.getLocationParams
import com.centyllion.client.updateLocation
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.SimulationDescription
import com.centyllion.model.emptyGrainModelDescription
import com.centyllion.model.emptySimulationDescription
import org.w3c.dom.HTMLElement
import kotlin.js.Promise.Companion.resolve
import kotlin.properties.Delegates.observable

class ModelPage(val context: AppContext) : BulmaElement {

    val api = context.api

    val newIcon = "plus"
    val saveIcon = "cloud-upload-alt"
    val shareIcon = "share"
    val deleteIcon = "minus"

    enum class Status { Saved, Dirty, New }

    var modelStatus: MutableMap<GrainModelDescription, Status> = mutableMapOf()
    var simulationStatus: MutableMap<SimulationDescription, Status> = mutableMapOf()

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

    var models: List<GrainModelDescription> by observable(emptyList())
    { _, old, new ->
        if (old != new) refreshModels()
    }

    var selectedModel: GrainModelDescription by observable(emptyGrainModelDescription)
    { _, old, new ->
        if (old != new) {
            updateLocation(null, mapOf("model" to new.id), false, true)

            refreshSelectedModel()

            // updates simulation only if model id changed
            if (old.id != new.id) {
                // fetches new simulations or return one empty simulation
                val newSimulations = when {
                    selectedModel.id.isNotEmpty() ->
                        api.fetchSimulations(selectedModel.id, false).then {
                            context.message("Simulations for ${selectedModel.model.name} loaded")
                            if (it.isNotEmpty()) it else listOf(emptySimulationDescription)
                        }.catch {
                            context.error(it.message ?: it.toString())
                            listOf(emptySimulationDescription)
                        }
                    else -> resolve(listOf(emptySimulationDescription))
                }

                // updates status and simulations
                newSimulations.then {
                    simulationStatus = it
                        .map { it to if (it.id.isNotEmpty()) Status.Saved else Status.New }
                        .toMap().toMutableMap()
                    simulations = it

                    val simulationId = getLocationParams("simulation")
                    selectedSimulation = simulations.find { it.id == simulationId } ?: simulations.first()
                }
            }
        }
    }

    var simulations: List<SimulationDescription> by observable(emptyList())
    { _, old, new ->
        if (old != new) refreshSimulations()
    }

    var selectedSimulation by observable(emptySimulationDescription)
    { _, old, new ->
        if (old != new) {
            updateLocation(null, mapOf("simulation" to new.id), false, true)
            refreshSelectedSimulation()
        }
    }

    private var undoModel = false
    private var choosingModel = false

    val modelController = GrainModelEditController(selectedModel.model)
    { old, new, _ ->
        if (old != new) {
            if (!choosingModel) {
                // update selected model
                updateModel(selectedModel, selectedModel.copy(model = new))
            } else {
                modelHistory = emptyList()
            }

            // updates the simulation
            simulationController.context = new
        }
    }

    private var undoSimulation = false
    private var choosingSimulation = false

    val simulationController =
        SimulationRunController(selectedSimulation.simulation, selectedModel.model)
        { old, new, _ ->
            if (old != new) {
                if (!choosingSimulation) {
                    // update selected simulation
                    updateSimulation(selectedSimulation, selectedSimulation.copy(simulation = new))
                }
            }
        }

    val modelSelect = Dropdown(rounded = true)

    val newModelButton = iconButton(Icon(newIcon), color = ElementColor.Primary, rounded = true) {
        models += emptyGrainModelDescription
        modelStatus[emptyGrainModelDescription] = Status.New
        selectedModel = models.last()
    }

    val deleteModelButton = iconButton(Icon(deleteIcon), color = ElementColor.Danger, rounded = true) {
        val deletedModel = selectedModel
        if (deletedModel.id.isNotEmpty()) {
            api.deleteGrainModel(deletedModel).then {
                removeModel(deletedModel)
                context.message("Model ${deletedModel.model.name} deleted")
                Unit
            }.catch {
                this.context.error(it)
                Unit
            }
        } else {
            removeModel(deletedModel)
            context.message("Model ${deletedModel.model.name} removed")
        }
        modelStatus.remove(deletedModel)
    }

    val modelField = Field(
        Control(modelSelect), Control(newModelButton), Control(deleteModelButton),
        addons = true
    )

    val simulationSelect = Dropdown(rounded = true)

    val newSimulationButton = iconButton(Icon(newIcon), color = ElementColor.Primary, rounded = true) {
        simulations += emptySimulationDescription
        simulationStatus[emptySimulationDescription] = Status.New
        selectedSimulation = simulations.last()
    }

    val deleteSimulationButton = iconButton(Icon(deleteIcon), color = ElementColor.Danger, rounded = true) {
        val deletedSimulation = selectedSimulation
        if (deletedSimulation.id.isNotEmpty()) {
            api.deleteSimulation(deletedSimulation).then {
                removeSimulation(deletedSimulation)
                context.message("Simulation ${deletedSimulation.simulation.name} deleted")
                Unit
            }.catch {
                this.context.error(it)
                Unit
            }
        } else {
            removeSimulation(deletedSimulation)
            context.message("Simulation ${deletedSimulation.simulation.name} removed")
        }
    }

    val simulationField = Field(
        Control(simulationSelect), Control(newSimulationButton), Control(deleteSimulationButton),
        addons = true
    )

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

    private val modelAndSimulationNotLocal
        get() = selectedModel.id.isEmpty() && selectedSimulation.id.isEmpty()

    private val canPublish
        get() = selectedModel.info.readAccess || selectedSimulation.info.readAccess

    private fun needModelSave() =
        modelStatus.getOrElse(selectedModel) { Status.Saved } != Status.Saved

    private fun needSimulationSave() =
        selectedModel.id.isEmpty() || simulationStatus[selectedSimulation] != Status.Saved

    fun save() {
        if (needModelSave() && selectedModel.id.isEmpty()) {
            // The model needs to be save first
            api.saveGrainModel(selectedModel.model)
                .then { newModel ->
                    updateModel(selectedModel, newModel, false)
                    modelStatus[selectedModel] = Status.Saved
                    api.saveSimulation(newModel.id, selectedSimulation.simulation)
                }.then { newSimulation ->
                    simulationStatus[selectedSimulation] = Status.Saved
                    updateSimulation(selectedSimulation, newSimulation, false)
                    context.message("Model ${selectedModel.model.name} and simulation ${newSimulation.simulation.name} saved")
                    Unit
                }.catch {
                    this.context.error(it)
                    Unit
                }
        } else {

            if (needModelSave()) {
                api.updateGrainModel(selectedModel).then {
                    modelStatus[selectedModel] = Status.Saved
                    refreshModels()
                    refreshSelectedModel()
                    context.message("Model ${selectedModel.model.name} saved")
                    Unit
                }.catch {
                    this.context.error(it)
                    Unit
                }
            }

            if (needSimulationSave()) {
                if (selectedSimulation.id.isEmpty()) {
                    api.saveSimulation(selectedModel.id, selectedSimulation.simulation).then {
                        simulationStatus[selectedSimulation] = Status.Saved
                        updateSimulation(selectedSimulation, it, false)
                        context.message("Simulation ${selectedSimulation.simulation.name} saved")
                        Unit
                    }.catch {
                        this.context.error(it)
                        Unit
                    }
                } else {
                    api.updateSimulation(selectedSimulation).then {
                        simulationStatus[selectedSimulation] = Status.Saved
                        refreshSimulations()
                        refreshSelectedSimulation()
                        context.message("Simulation ${selectedSimulation.simulation.name} saved")
                        Unit
                    }.catch {
                        this.context.error(it)
                        Unit
                    }
                }
            }
        }
    }

    fun togglePublication() {
        val newModelInfo = selectedModel.info.copy(readAccess = canPublish)
        updateModel(selectedModel, selectedModel.copy(info = newModelInfo))

        val newSimulationInfo = selectedSimulation.info.copy(readAccess = canPublish)
        updateSimulation(selectedSimulation, selectedSimulation.copy(info = newSimulationInfo))
        save()
    }

    val rightTools = Field(
        Control(undoModelButton), Control(redoModelButton), Control(saveButton), Control(publishButton),
        grouped = true
    )

    val modelPage = TabPage(TabItem("Model", "boxes"), modelController)
    val simulationPage = TabPage(TabItem("Simulation", "play"), simulationController)

    val tools = Level(center = listOf(modelField, simulationField, rightTools))

    val editionTab = TabPages(modelPage, simulationPage, tabs = Tabs(boxed = true), initialTabIndex = 1) {
        if (it == simulationPage) {
            rightTools.body = listOf(
                Control(undoSimulationButton), Control(redoSimulationButton),
                Control(saveButton), Control(publishButton)
            )
        } else {
            rightTools.body = listOf(
                Control(undoModelButton), Control(redoModelButton),
                Control(saveButton), Control(publishButton)
            )
        }
    }

    val container: BulmaElement = div(tools, editionTab)

    override val root: HTMLElement = container.root

    init {
        undoModelButton.disabled = true
        redoModelButton.disabled = true
        undoSimulationButton.disabled = true
        redoSimulationButton.disabled = true

        refreshModels()
        refreshSelectedModel()
        refreshSimulations()
        refreshSelectedSimulation()

        api.fetchMyGrainModels().then {
            // refreshes modelStatus before models to obtain the correct icons
            val newModels = if (it.isEmpty()) listOf(emptyGrainModelDescription) else it
            modelStatus = newModels
                .map { it to if (it.id.isNotEmpty()) Status.Saved else Status.New }
                .toMap().toMutableMap()
            models = newModels

            val modelId = getLocationParams("model")
            selectedModel = models.find { it.id == modelId } ?: models.first()
            context.message("Models loaded")
            Unit
        }.catch {
            models = listOf(emptyGrainModelDescription)
            selectedModel = models.first()
            context.error(it.message ?: it.toString())
            Unit
        }
    }

    private fun iconForModel(model: GrainModelDescription) = Icon(
        when (modelStatus.getOrElse(model) { Status.New }) {
            Status.New -> "laptop"
            Status.Dirty -> "cloud-upload-alt"
            Status.Saved -> "cloud"
        }
    )

    private fun refreshPublishButton() {
        publishButton.disabled = modelAndSimulationNotLocal
        publishButton.title = if (!canPublish) "Un-Publish" else "Publish"
    }

    private fun refreshModels() {
        modelSelect.items = models.map {
            DropdownSimpleItem(it.model.name, iconForModel(it)) { _ ->
                selectedModel = it
                modelSelect.toggleDropdown()
            }
        }
    }

    private fun refreshSelectedModel() {
        choosingModel = true
        modelController.data = selectedModel.model
        choosingModel = false

        modelSelect.icon = iconForModel(selectedModel)
        modelSelect.text = selectedModel.model.name

        saveButton.disabled = !needModelSave() && !needSimulationSave()
        refreshPublishButton()
    }

    private fun updateModel(
        oldModelDescription: GrainModelDescription, newModelDescription: GrainModelDescription,
        register: Boolean = true
    ) {
        // update selected model
        modelStatus[newModelDescription] =
            if (modelStatus[oldModelDescription] == Status.New) Status.New else Status.Dirty
        selectedModel = newModelDescription

        // updates model list
        val newModels = models.toMutableList()
        newModels[models.indexOf(oldModelDescription)] = newModelDescription
        models = newModels

        if (register) {
            if (undoModel) {
                modelFuture += oldModelDescription
            } else {
                modelHistory += oldModelDescription
                if (modelFuture.lastOrNull() == newModelDescription) {
                    modelFuture = modelFuture.dropLast(1)
                } else {
                    modelFuture = emptyList()
                }
            }
        }
    }

    private fun removeModel(deletedModel: GrainModelDescription) {
        // updates model list
        val newModels = models.toMutableList()
        newModels.remove(deletedModel)
        if (newModels.isEmpty()) newModels.add(emptyGrainModelDescription)
        models = newModels
        selectedModel = models.first()
    }


    private fun iconForSimulation(simulation: SimulationDescription) = Icon(
        when (simulationStatus.getOrElse(simulation) { Status.New }) {
            Status.New -> "laptop"
            Status.Dirty -> "cloud-upload-alt"
            Status.Saved -> "cloud"
        }
    )

    private fun refreshSimulations() {
        simulationSelect.items = simulations.map {
            DropdownSimpleItem(it.simulation.name, iconForSimulation(it)) { _ ->
                selectedSimulation = it
                simulationSelect.toggleDropdown()
            }
        }
        simulationStatus = simulationStatus.filter { simulations.contains(it.key) }.toMutableMap()
    }

    private fun refreshSelectedSimulation() {
        choosingSimulation = true
        simulationController.data = selectedSimulation.simulation
        choosingSimulation = false

        simulationSelect.icon = iconForSimulation(selectedSimulation)
        simulationSelect.text = selectedSimulation.simulation.name

        saveButton.disabled = !needModelSave() && !needSimulationSave()
        refreshPublishButton()
    }

    private fun updateSimulation(
        oldSimulationDescription: SimulationDescription, newSimulationDescription: SimulationDescription,
        register: Boolean = true
    ) {
        // update selected simulation
        simulationStatus[newSimulationDescription] =
            if (simulationStatus[oldSimulationDescription] == Status.New) Status.New else Status.Dirty
        selectedSimulation = newSimulationDescription

        // updates model list
        val newSimulations = simulations.toMutableList()
        newSimulations[simulations.indexOf(oldSimulationDescription)] = newSimulationDescription
        simulations = newSimulations

        if (register) {
            if (undoSimulation) {
                simulationFuture += oldSimulationDescription
            } else {
                simulationHistory += oldSimulationDescription
                if (simulationFuture.lastOrNull() == newSimulationDescription) {
                    simulationFuture = simulationFuture.dropLast(1)
                } else {
                    simulationFuture = emptyList()
                }
            }
        }
    }

    private fun removeSimulation(deletedSimulation: SimulationDescription) {
        // updates simulation list
        val newSimulations = simulations.toMutableList()
        newSimulations.remove(deletedSimulation)
        if (newSimulations.isEmpty()) newSimulations.add(emptySimulationDescription)
        simulations = newSimulations
        selectedSimulation = simulations.first()
    }

}
