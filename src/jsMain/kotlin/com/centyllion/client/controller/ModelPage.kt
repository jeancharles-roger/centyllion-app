package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.client.deleteGrainModel
import com.centyllion.client.fetchGrainModels
import com.centyllion.client.saveGrainModel
import com.centyllion.client.updateGrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Simulator
import com.centyllion.model.sample.emptyGrainModelDescription
import com.centyllion.model.sample.emptySimulationDescription
import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable

class ModelPage(val instance: KeycloakInstance) : BulmaElement {

    val newIcon = "plus"
    val saveIcon = "cloud-upload-alt"
    val deleteIcon = "minus"

    enum class Status { Saved, Dirty, New }

    var modelStatus: MutableMap<GrainModelDescription, Status> = mutableMapOf()

    var models: List<GrainModelDescription> by observable(emptyList())
    { _, old, new ->
        if (old != new) {
            refreshModels()
        }
    }

    var selectedModel: GrainModelDescription by observable(emptyGrainModelDescription)
    { _, old, new ->
        if (old != new) {
            refreshSelectedModel()
        }
    }

    var simulations: List<SimulationDescription> by observable(emptyList())
    { _, old, new ->
        if (old != new) {
            simulationSelect.items = new.map {
                DropdownSimpleItem(it.simulation.name, Icon("cloud")) { _ ->
                    selectedSimulation = it
                    simulationSelect.toggleDropdown()
                }
            }
        }
    }

    var selectedSimulation by observable(emptySimulationDescription)
    { _, old, new ->
        if (old != new) {
            simulationController.data = Simulator(simulationController.data.model, new.simulation, true)
            simulationSelect.icon = Icon(
                when {
                    new._id.isEmpty() -> "laptop"
                    else -> "cloud"
                }
            )
            simulationSelect.text = new.simulation.name
        }
    }

    private var choosingModel = false

    val modelController = GrainModelEditController { old, new, _ ->
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
            }

            // updates the simulation
            simulationController.data = Simulator(new, Simulation(), true)
        }
    }

    val simulationController = SimulationRunController()

    val modelSelect = Dropdown("", rounded = true)

    val newModelButton = iconButton(Icon(newIcon), color = ElementColor.Primary, rounded = true) {
        val newModel = emptyGrainModelDescription
        modelStatus[newModel] = Status.New
        models += newModel
        selectedModel = models.last()
    }

    val saveModelButton = iconButton(Icon(saveIcon), color = ElementColor.Primary, rounded = true) {
        if (selectedModel._id.isEmpty()) {
            saveGrainModel(selectedModel.model, instance)
                .then {
                    // TODO replace model with saved one
                    // data = it
                    modelStatus[selectedModel] = Status.Saved
                    refreshSelectedModel()
                    message("Model ${it.model.name} saved")
                }
                .catch { error(it) }
        } else {
            updateGrainModel(selectedModel, instance)
                .then {
                    modelStatus[selectedModel] = Status.Saved
                    refreshSelectedModel()
                    message("Model ${selectedModel.model.name} saved")
                }
                .catch { error(it) }
        }
    }

    val deleteModelButton = iconButton(Icon(deleteIcon), color = ElementColor.Danger, rounded = true) {
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

    }

    val modelField = Field(
        Control(modelSelect), Control(newModelButton), Control(saveModelButton), Control(deleteModelButton),
        addons = true
    )

    val simulationSelect = Dropdown("", rounded = true)

    val newSimulationButton = iconButton(Icon(newIcon), color = ElementColor.Primary, rounded = true) {
        // TODO
    }

    val saveSimulationButton = iconButton(Icon(saveIcon), color = ElementColor.Primary, rounded = true) {
        // TODO
    }

    val deleteSimulationButton = iconButton(Icon(deleteIcon), color = ElementColor.Danger, rounded = true) {
        // TODO
    }

    val simulationField = Field(
        Control(simulationSelect), Control(newSimulationButton), Control(saveSimulationButton), Control(deleteSimulationButton),
        addons = true
    )

    val editionTab = TabPages(
        TabPage(TabItem("Model", "boxes"), modelController),
        TabPage(TabItem("Simulation", "play"), simulationController),
        tabs = Tabs(boxed = true)
    )

    val messageContent = span()
    val message = Message(body = listOf(messageContent), size = Size.Small)

    val container: BulmaElement = div(
        Level(
            center = listOf(modelField, simulationField),
            right = listOf(message)
        ),
        editionTab
    )

    override val root: HTMLElement = container.root

    init {
        refreshModels()
        refreshSelectedModel()

        /*
        modelController.data = selectedModel.model
        simulationController.data = Simulator(modelController.data, Simulation(), true)
        */

        fetchGrainModels(instance)
            .then {
                models = if (it.isEmpty()) listOf(emptyGrainModelDescription) else it
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


    private fun refreshModels() {
        modelSelect.items = models.map {
            DropdownSimpleItem(it.model.name, iconForModel(it)) { _ ->
                selectedModel = it
                modelSelect.toggleDropdown()
            }
        }
        modelStatus = modelStatus.filter { models.contains(it.key) }.toMutableMap()
    }

    private fun iconForModel(model: GrainModelDescription) = Icon(
        when (modelStatus.getOrElse(model) { Status.Saved }) {
            Status.New -> "laptop"
            Status.Dirty -> "cloud-upload-alt"
            Status.Saved -> "cloud"
        }
    )

    private fun refreshSelectedModel() {
        choosingModel = true
        modelController.data = selectedModel.model
        choosingModel = false

        modelSelect.icon = iconForModel(selectedModel)
        modelSelect.text = selectedModel.model.name

        saveModelButton.disabled = modelStatus.getOrElse(selectedModel) { Status.Saved } == Status.Saved

        if (selectedModel._id.isNotEmpty()) {
            /*
                fetchGrainModels(instance)
                    .then {
                        models = if (it.isEmpty()) it else listOf(emptyGrainModelDescription)
                        selectedModel = models.first()
                        message("Models loaded")
                    }
                    .catch {
                        models = listOf(emptyGrainModelDescription)
                        selectedModel = models.first()
                        error(it.message ?: it.toString())
                    }
                 */
        } else {

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

}
