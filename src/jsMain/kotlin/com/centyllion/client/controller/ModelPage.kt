package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.client.*
import com.centyllion.model.*
import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable

class ModelPage(val instance: KeycloakInstance) : BulmaElement {

    val newIcon = "plus"
    val saveIcon = "cloud-upload-alt"
    val deleteIcon = "minus"

    enum class Status { Saved, Dirty, New }

    var modelStatus: MutableMap<GrainModelDescription, Status> = mutableMapOf()
    var simulationStatus: MutableMap<SimulationDescription, Status> = mutableMapOf()

    private var modelHistory: List<GrainModel> by observable(emptyList()) { _, old, new ->
        undoModelButton.disabled = new.isEmpty()
    }

    private var modelFuture: List<GrainModel> by observable(emptyList()) { _, old, new ->
        redoModelButton.disabled = new.isEmpty()
    }

    private var simulationHistory: List<Simulation> by observable(emptyList()) { _, old, new ->
        undoSimulationButton.disabled = new.isEmpty()
    }

    private var simulationFuture: List<Simulation> by observable(emptyList()) { _, old, new ->
        redoSimulationButton.disabled = new.isEmpty()
    }

    var models: List<GrainModelDescription> by observable(emptyList())
    { _, old, new ->
        if (old != new) { refreshModels() }
    }

    var selectedModel: GrainModelDescription by observable(emptyGrainModelDescription)
    { _, old, new ->
        if (old != new) {
            refreshSelectedModel()

            // updates simulation only if model _id changed
            if (old._id != new._id) {
                if (selectedModel._id.isNotEmpty()) {
                    fetchSimulations(selectedModel._id, instance)
                        .then {
                            simulations = if (it.isNotEmpty()) it else listOf(emptySimulationDescription)
                            simulationStatus =
                                simulations.map { it to if (it._id.isNotEmpty()) Status.Saved else Status.New }.toMap()
                                    .toMutableMap()
                            selectedSimulation = simulations.first()
                            message("Simulations for ${selectedModel.model.name} loaded")
                        }
                        .catch {
                            simulations = listOf(emptySimulationDescription)
                            simulationStatus =
                                simulations.map { it to if (it._id.isNotEmpty()) Status.Saved else Status.New }.toMap()
                                    .toMutableMap()
                            selectedSimulation = simulations.first()
                            error(it.message ?: it.toString())
                        }
                } else {
                    simulations = listOf(emptySimulationDescription)
                    simulationStatus =
                        simulations.map { it to if (it._id.isNotEmpty()) Status.Saved else Status.New }.toMap()
                            .toMutableMap()
                    selectedSimulation = simulations.first()
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
        if (old != new) refreshSelectedSimulation()
    }


    private var undoModel = false
    private var choosingModel = false

    val modelController = GrainModelEditController(selectedModel.model)
    { old, new, _ ->
        if (old != new) {
            if (!choosingModel) {
                // update selected model
                val oldModelDescription = selectedModel
                val newModelDescription = selectedModel.copy(model = new)
                modelStatus[newModelDescription] = if (modelStatus[oldModelDescription] == Status.New) Status.New else Status.Dirty
                selectedModel = newModelDescription

                // updates model list
                val newModels = models.toMutableList()
                newModels[models.indexOf(oldModelDescription)] = newModelDescription
                models = newModels

                if (undoModel) {
                    modelFuture += old
                } else {
                    modelHistory += old
                }
            } else {
                modelHistory = emptyList()
            }

            // updates the simulation
            simulationController.context = new
        }
    }

    private var undoSimulation = false
    private var choosingSimulation = false

    val simulationController = SimulationRunController(selectedSimulation.simulation, selectedModel.model)
    { old, new, _ ->
        if (old != new) {
            if (!choosingSimulation) {
                // update selected simulation
                val oldSimulation = selectedSimulation
                val newSimulation = selectedSimulation.copy(simulation = new)
                simulationStatus[newSimulation] = if (simulationStatus[oldSimulation] == Status.New) Status.New else Status.Dirty
                selectedSimulation = newSimulation

                // updates simulation list
                val newSimulations = simulations.toMutableList()
                newSimulations[simulations.indexOf(oldSimulation)] = newSimulation
                simulations = newSimulations

                if (undoSimulation) {
                    simulationFuture += old
                } else {
                    simulationHistory += old
                }
            }
        }
    }

    val modelSelect = Dropdown("", rounded = true)

    val newModelButton = Button("New", Icon(newIcon), color = ElementColor.Primary, rounded = true) {
        models += emptyGrainModelDescription
        modelStatus[emptyGrainModelDescription] = Status.New
        selectedModel = models.last()
    }

    val saveModelButton = Button("Save", Icon(saveIcon), color = ElementColor.Primary, rounded = true) {
        if (selectedModel._id.isEmpty()) {
            saveGrainModel(selectedModel.model, instance)
                .then {
                    val newModels = models.toMutableList()
                    newModels[models.indexOf(selectedModel)] = it
                    modelStatus[it] = Status.Saved
                    models = newModels
                    selectedModel = it
                    message("Model ${it.model.name} saved")
                }
                .catch { error(it) }
        } else {
            updateGrainModel(selectedModel, instance)
                .then {
                    modelStatus[selectedModel] = Status.Saved
                    refreshModels()
                    refreshSelectedModel()
                    message("Model ${selectedModel.model.name} saved")
                }
                .catch { error(it) }
        }
    }

    val deleteModelButton = Button("Delete", Icon(deleteIcon), color = ElementColor.Danger, rounded = true) {
        val deletedModel = selectedModel
        if (deletedModel._id.isNotEmpty()) {
            deleteGrainModel(deletedModel, instance).then {
                removeModel(deletedModel)
                message("Model ${deletedModel.model.name} deleted")
            }.catch { error(it) }
        } else {
            removeModel(deletedModel)
            message("Model ${deletedModel.model.name} removed")
        }
        modelStatus.remove(deletedModel)
    }

    val modelField = Field(
        Control(modelSelect), Control(newModelButton), Control(saveModelButton), Control(deleteModelButton),
        addons = true
    )

    val simulationSelect = Dropdown("", rounded = true)

    val newSimulationButton = Button("New", Icon(newIcon), color = ElementColor.Primary, rounded = true) {
        simulations += emptySimulationDescription
        simulationStatus[emptySimulationDescription] = Status.New
        selectedSimulation = simulations.last()
    }

    val saveSimulationButton = Button("Save", Icon(saveIcon), color = ElementColor.Primary, rounded = true) {
        if (selectedSimulation._id.isEmpty()) {
            saveSimulation(selectedModel._id, selectedSimulation.simulation, instance)
                .then {
                    val newSimulations = simulations.toMutableList()
                    newSimulations[simulations.indexOf(selectedSimulation)] = it
                    simulationStatus[it] = Status.Saved
                    simulations = newSimulations
                    selectedSimulation = it
                    message("Simulation ${it.simulation.name} saved")
                }
                .catch { error(it) }
        } else {
            updateSimulation(selectedModel._id, selectedSimulation, instance)
                .then {
                    simulationStatus[selectedSimulation] = Status.Saved
                    refreshSimulations()
                    refreshSelectedSimulation()
                    message("Simulation ${selectedSimulation.simulation.name} saved")
                }
                .catch { error(it) }
        }
    }

    val deleteSimulationButton = Button("Delete", Icon(deleteIcon), color = ElementColor.Danger, rounded = true) {
        val deletedSimulation = selectedSimulation
        if (deletedSimulation._id.isNotEmpty()) {
            deleteSimulation(selectedModel._id, deletedSimulation, instance).then {
                removeSimulation(deletedSimulation)
                message("Simulation ${deletedSimulation.simulation.name} deleted")
            }.catch { error(it) }
        } else {
            removeSimulation(deletedSimulation)
            message("Simulation ${deletedSimulation.simulation.name} removed")
        }
    }

    val simulationField = Field(
        Control(simulationSelect), Control(newSimulationButton), Control(saveSimulationButton), Control(deleteSimulationButton),
        addons = true
    )

    val undoModelButton = iconButton(Icon("undo"), ElementColor.Primary, rounded = true) {
        val restoredModel = modelHistory.last()
        modelHistory = modelHistory.dropLast(1)
        undoModel = true
        modelController.data = restoredModel
        undoModel = false
    }

    val redoModelButton = iconButton(Icon("redo"), ElementColor.Primary, rounded = true) {
        val restoredModel = modelFuture.last()
        modelFuture = modelFuture.dropLast(1)
        modelController.data = restoredModel
    }

    val undoModelField = Field(Control(undoModelButton), Control(redoModelButton), addons = true)

    val undoSimulationButton = iconButton(Icon("undo"), ElementColor.Primary, rounded = true) {
        val restoredSimulation = simulationHistory.last()
        simulationHistory = simulationHistory.dropLast(1)
        undoSimulation = true
        simulationController.data = restoredSimulation
        undoSimulation = false
    }

    val redoSimulationButton = iconButton(Icon("redo"), ElementColor.Primary, rounded = true) {
        val restoredSimulation = simulationFuture.last()
        simulationFuture = simulationFuture.dropLast(1)
        simulationController.data = restoredSimulation
    }

    val undoSimulationField = Field(Control(undoSimulationButton), Control(redoSimulationButton), addons = true)

    val modelPage = TabPage(TabItem("Model", "boxes"), modelController)
    val simulationPage = TabPage(TabItem("Simulation", "play"), simulationController)

    val editionTab = TabPages(modelPage, simulationPage, tabs = Tabs(boxed = true)) {
        undoModelField.root.classList.toggle("is-hidden", it == simulationPage)
        undoSimulationField.root.classList.toggle("is-hidden", it == modelPage)
    }

    val messageContent = span()
    val message = Message(body = listOf(messageContent), size = Size.Small)

    val container: BulmaElement = div(
        Level(left = listOf(modelField), center = listOf(undoModelField, undoSimulationField), right = listOf(simulationField)),
        message,
        editionTab
    )

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

        fetchGrainModels(instance)
            .then {
                models = if (it.isEmpty()) listOf(emptyGrainModelDescription) else it
                modelStatus = models.map { it to if (it._id.isNotEmpty()) Status.Saved else Status.New }.toMap().toMutableMap()
                selectedModel = models.first()
                message("Models loaded")
            }
            .catch {
                models = listOf(emptyGrainModelDescription)
                selectedModel = models.first()
                error(it.message ?: it.toString())
            }
    }

    fun error(string: String) {
        message.color = ElementColor.Danger
        messageContent.text = string
    }

    fun message(string: String) {
        message.color = ElementColor.None
        messageContent.text = string
    }

    private fun iconForModel(model: GrainModelDescription) = Icon(
        when (modelStatus.getOrElse(model) { Status.New }) {
            Status.New -> "laptop"
            Status.Dirty -> "cloud-upload-alt"
            Status.Saved -> "cloud"
        }
    )

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

        saveModelButton.disabled = modelStatus.getOrElse(selectedModel) { Status.Saved } == Status.Saved
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

        saveSimulationButton.disabled = selectedModel._id.isEmpty() || simulationStatus[selectedSimulation] == Status.Saved
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
