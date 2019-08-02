package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Button
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Div
import bulma.Dropdown
import bulma.DropdownDivider
import bulma.ElementColor
import bulma.FaFlip
import bulma.Field
import bulma.Icon
import bulma.TabItem
import bulma.TabPage
import bulma.TabPages
import bulma.Tabs
import bulma.TextColor
import bulma.iconButton
import bulma.p
import bulma.textButton
import com.centyllion.client.AppContext
import com.centyllion.client.controller.model.GrainModelEditController
import com.centyllion.client.controller.model.SimulationRunController
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.download
import com.centyllion.client.homePage
import com.centyllion.client.stringHref
import com.centyllion.client.toFixed
import com.centyllion.common.adminRole
import com.centyllion.common.apprenticeRole
import com.centyllion.common.creatorRole
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.emptyGrainModelDescription
import com.centyllion.model.emptyModel
import com.centyllion.model.emptySimulation
import com.centyllion.model.emptySimulationDescription
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

/** ShowPage is use to present and edit (if not read-only) a model and a simulation. */
class ShowPage(override val appContext: AppContext) : BulmaPage {

    val saveIcon = "cloud-upload-alt"
    val shareIcon = "share-square"
    val newIcon = "plus"
    val cloneIcon = "clone"
    val downloadIcon = "download"
    val deleteIcon = "trash"

    val api = appContext.api

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
        get() = !appContext.hasRole(apprenticeRole) || (model.id.isNotEmpty() && model.info.userId != appContext.me?.id)

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
        get() = !appContext.hasRole(apprenticeRole) || simulation.id.isNotEmpty() && simulation.info.userId != appContext.me?.id

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

    val simulationController = SimulationRunController(emptySimulation, emptyModel, this, isSimulationReadOnly,
        { behaviour, speed, _ ->
            message("Updates speed for ${behaviour.name} to ${speed.toFixed()}")
            val newBehaviour = behaviour.copy(probability = speed)
            model = model.copy(model = model.model.updateBehaviour(behaviour, newBehaviour))
        },
        { old, new, _ -> if (old != new) simulation = simulation.copy(simulation = new) }
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
        simulationController.data = restoredSimulation.simulation
    }

    val saveButton = Button("Save", Icon(saveIcon), color = ElementColor.Primary, rounded = true) { save() }

    val publishModelItem = createMenuItem(
        "Publish Model", shareIcon, TextColor.Success, creatorRole
    ) { toggleModelPublication() }

    val publishSimulationItem = createMenuItem(
        "Publish Simulation", shareIcon, TextColor.Success, creatorRole
    ) { toggleSimulationPublication() }

    val deleteModelItem = createMenuItem(
        "Delete Model", deleteIcon, TextColor.Danger
    ) { deleteModel() }

    val deleteSimulationItem = createMenuItem(
        "Delete Simulation", deleteIcon, TextColor.Danger
    ) { deleteSimulation() }

    val cloneModelItem = createMenuItem(
        "Clone Model", cloneIcon, TextColor.Primary
    ) { cloneModel() }

    val downloadModelItem = createMenuItem(
        "Download Model", downloadIcon, TextColor.Primary, adminRole
    ) { downloadModel() }

    val newSimulationItem = createMenuItem(
        "New Simulation",newIcon, TextColor.Primary
    ) { newSimulation() }

    val cloneSimulationItem = createMenuItem(
        "Clone Simulation", cloneIcon, TextColor.Primary
    ) { cloneSimulation() }

    val downloadSimulationItem = createMenuItem(
        "Download Simulation", downloadIcon, TextColor.Primary, adminRole
    ) { downloadSimulation() }

    val loadingItem = createMenuItem("Loading simulations", "spinner").apply { itemIcon.spin = true }

    val moreDropdownItems = listOf(
        publishModelItem, publishSimulationItem, DropdownDivider(),
        deleteModelItem, deleteSimulationItem, DropdownDivider(),
        cloneModelItem, downloadModelItem, DropdownDivider(),
        newSimulationItem, cloneSimulationItem, downloadSimulationItem, DropdownDivider()
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
                appContext.api.fetchSimulation(simulationId).then { simulation ->
                    appContext.api.fetchGrainModel(simulation.modelId).then { simulation to it }
                }.then { it }

            // if there is a model id, use it to list all simulation and take the first one
            modelId != null && modelId.isNotEmpty() ->
                appContext.api.fetchGrainModel(modelId).then { model ->
                    appContext.api.fetchSimulations(model.id, false).then { simulations ->
                        (simulations.firstOrNull() ?: emptySimulationDescription) to model
                    }
                }.then { it }

            else -> Promise.resolve(emptySimulationDescription to emptyGrainModelDescription)
        }

        result.then {
            setModel(it.second)
            setSimulation(it.first)
        }.catch {
            error(it)
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
        this.simulation = originalSimulation.cleaned(model)
        this.simulationHistory = emptyList()
        this.simulationFuture = emptyList()
        refreshButtons()
    }

    fun save(after: () -> Unit = {}) {
        val needModelSave = model != originalModel || model.id.isEmpty()
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
                    message("Model ${model.model.name} and simulation ${simulation.simulation.name} saved")
                    after()
                    Unit
                }.catch {
                    this.error(it)
                    Unit
                }
        } else {

            // Save the model if needed
            if (needModelSave) {
                api.updateGrainModel(model).then {
                    originalModel = model
                    refreshButtons()
                    message("Model ${model.model.name} saved")
                    Unit
                }.catch {
                    this.error(it)
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
                        message("Simulation ${simulation.simulation.name} saved")
                        after()
                        Unit
                    }.catch {
                        this.error(it)
                        Unit
                    }
                } else {
                    // saves the simulation
                    api.updateSimulation(simulation).then {
                        originalSimulation = simulation
                        refreshButtons()
                        message("Simulation ${simulation.simulation.name} saved")
                        after()
                        Unit
                    }.catch {
                        this.error(it)
                        Unit
                    }
                }
            } else {
                after()
            }
        }
    }

    fun toggleModelPublication() {
        val readAccess = !model.info.readAccess
        model = model.copy(info = model.info.copy(readAccess = readAccess))
        moreDropdown.active = false
        message("${if (!readAccess) "Un-" else ""}Published model")
        save()
    }

    fun toggleSimulationPublication() {
        val readAccess = !simulation.info.readAccess
        simulation = simulation.copy(info = simulation.info.copy(readAccess = readAccess))
        moreDropdown.active = false
        message("${if (!readAccess) "Un-" else ""}Published simulation")
        save()
    }

    fun cloneModel() {
        // checks if something needs saving before creating a new simulation
        onExit().then {
            if (it) {
                // creates cloned model
                val cloned = emptyGrainModelDescription.copy(
                    model = model.model.copy(name = model.model.name + " cloned")
                )
                setModel(cloned)
                setSimulation(emptySimulationDescription)

                // closes the action
                moreDropdown.active = false
                editionTab.selectedPage = modelPage
                message("Model cloned")
            }
        }
    }

    fun downloadModel() {
        val href = stringHref(Json.stringify(GrainModel.serializer(), model.model))
        download("${model.name}.json", href)
        moreDropdown.active = false
    }

    fun newSimulation() {
        // checks if something needs saving before creating a new simulation
        onExit().then {
            if (it) {
                setSimulation(emptySimulationDescription)
                moreDropdown.active = false
                editionTab.selectedPage = simulationPage
                message("New simulation")
            }
        }
    }

    fun cloneSimulation() {
        // checks if something needs saving before creating a new simulation
        onExit().then {
            if (it) {
                val cloned = emptySimulationDescription.copy(
                    simulation = simulation.simulation.copy(name = simulation.simulation.name + " cloned")
                )
                setSimulation(cloned)

                moreDropdown.active = false
                editionTab.selectedPage = simulationPage
                message("Simulation cloned")
            }
        }
    }

    fun downloadSimulation() {
        val href = stringHref(Json.stringify(Simulation.serializer(), simulation.simulation))
        download("${simulation.name}.json", href)
        moreDropdown.active = false
    }

    fun deleteModel() {
        moreDropdown.active = false

        val modal = modalDialog(
            "Delete model, Are you sure ?",
            Div(
                p("You're about to delete the model '${model.label}' and its simulations."),
                p("This action can't be undone.", "has-text-weight-bold")
            ),
            textButton("Yes", ElementColor.Danger) {
                appContext.api.deleteGrainModel(model).then {
                    appContext.openPage(homePage)
                    message("Model ${model.label} deleted")
                }
            },
            textButton("No")
        )

        modal.active = true
    }

    fun deleteSimulation() {
        moreDropdown.active = false

        val modal = modalDialog(
            "Delete simulation, Are you sure ?",
            Div(
                p("You're about to delete the simulation '${simulation.label}'."),
                p("This action can't be undone.", "has-text-weight-bold")
            ),
            textButton("Yes", ElementColor.Danger) {
                appContext.api.deleteSimulation(simulation).then {
                    message("Simulation ${simulation.label} deleted")
                    appContext.api.fetchSimulations(model.id, false)
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
        saveButton.disabled = model == originalModel && model.id.isNotEmpty() && simulation == originalSimulation && simulation.id.isNotEmpty()
    }

    fun refreshMoreButtons() {
        publishModelItem.disabled = model.id.isEmpty()
        publishModelItem.itemIcon.flip = if (model.info.readAccess) FaFlip.Horizontal else FaFlip.None
        publishModelItem.itemText.text = "${if (model.info.readAccess) "Un-" else ""}Publish Model"

        publishSimulationItem.disabled = simulation.id.isEmpty()
        publishSimulationItem.itemIcon.flip = if (simulation.info.readAccess) FaFlip.Horizontal else FaFlip.None
        publishSimulationItem.itemText.text = "${if (simulation.info.readAccess) "Un-" else ""}Publish Simulation"

        deleteModelItem.disabled = model.id.isEmpty()

        deleteSimulationItem.disabled = simulation.id.isEmpty()

        if (model.id.isNotEmpty()) {
            moreDropdown.items = moreDropdownItems + loadingItem

            appContext.api.fetchSimulations(model.id, false).then {
                moreDropdown.items = moreDropdownItems + it.map { current ->
                    createMenuItem(current.label, current.icon, disabled = current == simulation) {
                        save {
                            setSimulation(current)
                            moreDropdown.active = false
                            editionTab.selectedPage = simulationPage
                        }
                    }
                }
            }.catch { error(it) }
        } else {
            moreDropdown.items = moreDropdownItems
        }
    }


    override fun onExit() = Promise<Boolean> { resolve, _ ->
        if (model != originalModel || simulation != originalSimulation) {
            val model = modalDialog("Modifications not saved, Do you wan't to save ?",
                p("You're about to quit the page and some modifications haven't been saved."),
                textButton("Save", ElementColor.Success) {
                    save { resolve(true) }
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
