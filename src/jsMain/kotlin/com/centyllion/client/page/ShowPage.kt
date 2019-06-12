package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.model.GrainModelEditController
import com.centyllion.client.controller.model.SimulationRunController
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.homePage
import com.centyllion.common.creatorRole
import com.centyllion.model.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class ShowPage(val context: AppContext) : BulmaElement {

    val saveIcon = "cloud-upload-alt"
    val shareIcon = "share-square"
    val newIcon = "plus"
    val deleteIcon = "trash"

    val api = context.api

    val hasModelRole = context.keycloak.hasRealmRole(creatorRole)

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

    val isModelReadOnly
        get() = !hasModelRole || (model.id.isNotEmpty() && model.info.userId != context.me?.id)

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

            val readonly = isModelReadOnly
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

    val modelNameController = EditableStringController(model.model.name, "Model Name") { _, new, _ ->
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

    val isSimulationReadOnly
        get() = !hasModelRole || simulation.id.isNotEmpty() && simulation.info.userId != context.me?.id

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

            simulationController.readOnly = isSimulationReadOnly
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
        simulationController.data = restoredSimulation.simulation
    }

    val saveButton = Button("Save", Icon(saveIcon), color = ElementColor.Primary, rounded = true) { save() }

    val publishModelItem = DropdownSimpleItem(
        "Publish Model", Icon(shareIcon, color = TextColor.Success)
    ) { toggleModelPublication() }

    val publishSimulationItem = DropdownSimpleItem(
        "Publish Simulation", Icon(shareIcon, color = TextColor.Success)
    ) { toggleSimulationPublication() }

    val deleteModelItem = DropdownSimpleItem(
        "Delete Model", Icon(deleteIcon, color = TextColor.Danger)
    ) { deleteModel() }

    val deleteSimulationItem = DropdownSimpleItem(
        "Delete Simulation", Icon(deleteIcon, color = TextColor.Danger)
    ) { deleteSimulation() }

    val newSimulationItem =
        DropdownSimpleItem("New Simulation", Icon(newIcon, color = TextColor.Primary)) { newSimulation() }

    val loadingItem = DropdownSimpleItem("Loading simulations", Icon("spinner", spin = true))

    val moreDropdownItems = listOf(
        publishModelItem, publishSimulationItem, DropdownDivider(),
        deleteModelItem, deleteSimulationItem, DropdownDivider(),
        newSimulationItem, DropdownDivider()
    )

    val moreDropdown = Dropdown(
        icon = Icon("cog"), color = ElementColor.Primary, right = true, rounded = true
    ) { refreshMoreButtons() }

    val modelPage = TabPage(TabItem("Model", "boxes"), modelController)
    val simulationPage = TabPage(TabItem("Simulation", "play"), simulationController)

    val undoControl = Control(undoModelButton)
    val redoControl = Control(redoModelButton)

    val tools = Field(
        undoControl, redoControl, Control(saveButton), Control(moreDropdown),
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
        // starts with all readonly
        val modelReadonly = isModelReadOnly
        modelNameController.readOnly = modelReadonly
        modelDescriptionController.readOnly = modelReadonly
        modelController.readOnly = modelReadonly
        simulationController.readOnly = isSimulationReadOnly
        tools.hidden = true

        // retrieves model and simulation to load
        val params = URLSearchParams(window.location.search)
        val simulationId = params.get("simulation")
        val modelId = params.get("model")

        // Selects model tab if there no simulation provided
        if (simulationId == null) editionTab.selectedPage = modelPage

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
                    context.api.fetchSimulations(model.id, false).then { simulations ->
                        (simulations.firstOrNull() ?: emptySimulationDescription) to model
                    }
                }.then { it }

            else -> Promise.resolve(emptySimulationDescription to emptyGrainModelDescription)
        }

        result.then {
            setSimulation(it.first)
            setModel(it.second)
        }.catch {
            context.error(it)
        }
    }

    fun setModel(model: GrainModelDescription) {
        this.originalModel = model
        this.model = originalModel
        this.modelHistory = emptyList()
        this.modelFuture = emptyList()
        refreshButtons()
    }

    fun setSimulation(simulation: SimulationDescription) {
        this.originalSimulation = simulation
        this.simulation = originalSimulation
        this.simulationHistory = emptyList()
        this.simulationFuture = emptyList()
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

    fun toggleModelPublication() {
        val readAccess = !model.info.readAccess
        model = model.copy(info = model.info.copy(readAccess = readAccess))
        moreDropdown.active = false
        context.message("${if (!readAccess) "Un-" else ""}Published model")
        save()
    }

    fun toggleSimulationPublication() {
        val readAccess = !simulation.info.readAccess
        simulation = simulation.copy(info = simulation.info.copy(readAccess = readAccess))
        moreDropdown.active = false
        context.message("${if (!readAccess) "Un-" else ""}Published simulation")
        save()
    }

    fun newSimulation() {
        save()
        setSimulation(emptySimulationDescription)
        moreDropdown.active = false
        editionTab.selectedPage = simulationPage
        context.message("New simulation")
    }

    fun deleteModel() {
        moreDropdown.active = false

        val modal = context.modalDialog(
            "Delete model, Are you sure ?",
            div(
                p("You're about to delete the model '${model.label}' and its simulations."),
                p("This action can't be undone.", "has-text-weight-bold")
            ),
            textButton("Yes", ElementColor.Danger) {
                context.api.deleteGrainModel(model).then {
                    context.openPage(homePage)
                    context.message("Model ${model.label} deleted")
                }
            },
            textButton("No")
        )

        modal.active = true
    }

    fun deleteSimulation() {
        moreDropdown.active = false

        val modal = context.modalDialog(
            "Delete simulation, Are you sure ?",
            div(
                p("You're about to delete the simulation '${simulation.label}'."),
                p("This action can't be undone.", "has-text-weight-bold")
            ),
            textButton("Yes", ElementColor.Danger) {
                context.api.deleteSimulation(simulation).then {
                    context.message("Simulation ${simulation.label} deleted")
                    context.api.fetchSimulations(model.id, false)
                }.then {
                    setSimulation(it.firstOrNull() ?: emptySimulationDescription)
                }
            },
            textButton("No")
        )

        modal.active = true
    }

    fun refreshButtons() {
        when (editionTab.selectedPage) {
            modelPage -> {
                tools.hidden = isModelReadOnly
                undoControl.body = undoModelButton
                redoControl.body = redoModelButton
            }
            simulationPage -> {
                val readonly = isSimulationReadOnly
                tools.hidden = readonly
                undoControl.body = undoSimulationButton
                redoControl.body = redoSimulationButton
            }
        }

        undoModelButton.disabled = modelHistory.isEmpty()
        redoModelButton.disabled = modelFuture.isEmpty()
        undoSimulationButton.disabled = simulationHistory.isEmpty()
        redoSimulationButton.disabled = simulationFuture.isEmpty()
        saveButton.disabled = model == originalModel && simulation == originalSimulation
    }

    fun refreshMoreButtons() {
        publishModelItem.disabled = model.id.isEmpty()
        publishModelItem.icon?.flip = if (model.info.readAccess) FaFlip.Horizontal else FaFlip.None
        publishModelItem.text = "${if (model.info.readAccess) "Un-" else ""}Publish Model"

        publishSimulationItem.disabled = simulation.id.isEmpty()
        publishSimulationItem.icon?.flip = if (simulation.info.readAccess) FaFlip.Horizontal else FaFlip.None
        publishSimulationItem.text = "${if (simulation.info.readAccess) "Un-" else ""}Publish Simulation"

        deleteModelItem.disabled = model.id.isEmpty()

        deleteSimulationItem.disabled = simulation.id.isEmpty()

        if (model.id.isNotEmpty()) {
            moreDropdown.items = moreDropdownItems + loadingItem

            context.api.fetchSimulations(model.id, false).then {
                moreDropdown.items = moreDropdownItems + it.map { current ->
                    DropdownSimpleItem(current.label, Icon(current.icon), current == simulation) {
                        save()
                        setSimulation(current)
                        moreDropdown.active = false
                        editionTab.selectedPage = simulationPage
                    }
                }
            }.catch { context.error(it) }
        } else {
            moreDropdown.items = moreDropdownItems
        }
    }

    fun canExit(context: AppContext) = Promise<Boolean> { resolve, _ ->
        if (model != originalModel || simulation != originalSimulation) {
            val model = context.modalDialog("Modifications not saved, Do you wan't to save ?",
                p("You're about to quit the page and some modifications haven't been saved."),
                textButton("Save", ElementColor.Success) {
                    save()
                    resolve(true)
                },
                textButton("Don't save", ElementColor.Danger) { resolve(true) },
                textButton("Stay here") { resolve(false) }
            )
            model.active = true
        } else {
            resolve(true)
        }
    }
}
