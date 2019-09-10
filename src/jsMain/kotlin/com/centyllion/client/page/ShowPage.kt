package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Button
import bulma.Column
import bulma.ColumnSize
import bulma.Columns
import bulma.Control
import bulma.Div
import bulma.Dropdown
import bulma.ElementColor
import bulma.FaFlip
import bulma.Field
import bulma.Help
import bulma.Icon
import bulma.Level
import bulma.TabItem
import bulma.TabPage
import bulma.TabPages
import bulma.Tabs
import bulma.TextColor
import bulma.p
import bulma.textButton
import com.centyllion.client.AppContext
import com.centyllion.client.controller.model.GrainModelEditController
import com.centyllion.client.controller.model.SimulationRunController
import com.centyllion.client.controller.model.Simulator3dViewController
import com.centyllion.client.controller.model.TagsController
import com.centyllion.client.controller.utils.EditableMarkdownController
import com.centyllion.client.controller.utils.EditableStringController
import com.centyllion.client.controller.utils.UndoRedoSupport
import com.centyllion.client.download
import com.centyllion.client.homePage
import com.centyllion.client.signInPage
import com.centyllion.client.stringHref
import com.centyllion.client.toFixed
import com.centyllion.common.adminRole
import com.centyllion.common.apprenticeRole
import com.centyllion.common.creatorRole
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Simulator
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

    val api = appContext.api

    val isModelReadOnly
        get() = !appContext.hasRole(apprenticeRole) || (model.id.isNotEmpty() &&
                model.info.user?.id != appContext.me?.id)

    var model: GrainModelDescription by observable(emptyGrainModelDescription) { _, old, new ->
        if (new != old) {
            modelUndoRedo.update(old, new)

            val readonly = isModelReadOnly
            modelController.readOnly = readonly
            modelController.data = new.model
            modelNameController.readOnly = readonly
            modelNameController.data = new.model.name
            val user = new.info.user?.let { if (it.id != appContext.me?.id) it.name else "me" } ?: "me"
            userLabel.text = "by $user"

            modelDescriptionController.readOnly = readonly
            modelDescriptionController.data = new.model.description

            tagsController.hidden = !new.info.public
            tagsController.readOnly = readonly
            tagsController.data = new.tags

            simulationController.context = new.model
            refreshButtons()
        }
    }

    private val modelUndoRedo = UndoRedoSupport(model) { model = it }

    val modelNameController = EditableStringController(model.model.name, "Model Name") { _, new, _ ->
        model = model.copy(model = model.model.copy(name = new))
    }

    val userLabel = Help()

    val modelDescriptionController = EditableMarkdownController(model.model.description, "Description") { _, new, _ ->
        model = model.copy(model = model.model.copy(description = new))
    }

    val tagsController = TagsController(model.tags, appContext.api) { old, new, _ -> if (old != new) model = model.copy( tags = new) }

    val modelController = GrainModelEditController(model.model) { old, new, _ ->
        if (old != new) {
            model = model.copy(model = new)
        }
    }

    val isSimulationReadOnly
        get() = !appContext.hasRole(apprenticeRole) || simulation.id.isNotEmpty() &&
                simulation.info.user?.id != appContext.me?.id

    var simulation: SimulationDescription by observable(emptySimulationDescription) { _, old, new ->
        if (new != old) {
            simulationUndoRedo.update(old, new)
            simulationController.readOnly = isSimulationReadOnly
            simulationController.data = new.simulation
            refreshButtons()
        }
    }

    private val simulationUndoRedo = UndoRedoSupport(simulation) { simulation = it }

    val simulationController = SimulationRunController(emptySimulation, emptyModel, this, isSimulationReadOnly,
        { behaviour, speed, _ ->
            message("Updates speed for ${behaviour.name} to ${speed.toFixed()}")
            val newBehaviour = behaviour.copy(probability = speed)
            model = model.copy(model = model.model.updateBehaviour(behaviour, newBehaviour))
        },
        { old, new, _ -> if (old != new) simulation = simulation.copy(simulation = new) }
    )

    val saveButton = Button("Save", Icon("cloud-upload-alt"), color = ElementColor.Primary, rounded = true) { save() }

    val publishModelItem = createMenuItem(
        "Publish Model", "share-square", TextColor.Success, creatorRole
    ) { toggleModelPublication() }

    val publishSimulationItem = createMenuItem(
        "Publish Simulation", "share-square", TextColor.Success, creatorRole
    ) { toggleSimulationPublication() }

    val deleteModelItem = createMenuItem(
        "Delete Model", "trash", TextColor.Danger
    ) { deleteModel() }

    val deleteSimulationItem = createMenuItem(
        "Delete Simulation", "trash", TextColor.Danger
    ) { deleteSimulation() }

    val downloadModelItem = createMenuItem(
        "Download Model", "download", TextColor.Primary, adminRole
    ) { downloadModel() }

    val newSimulationItem = createMenuItem(
        "New Simulation", "plus", TextColor.Primary
    ) { newSimulation() }

    val saveThumbnailItem = createMenuItem(
        "Save state as thumbnail", "image", TextColor.Primary, creatorRole
    ) { saveCurrentThumbnail() }

    val downloadSimulationItem = createMenuItem(
        "Download Simulation", "download", TextColor.Primary, adminRole
    ) { downloadSimulation() }

    val loadingItem = createMenuItem("Loading simulations", "spinner").apply { itemIcon.spin = true }

    val moreDropdownItems = listOfNotNull(
        publishModelItem, publishSimulationItem, createMenuDivider(),
        deleteModelItem, deleteSimulationItem, createMenuDivider(),
        newSimulationItem, saveThumbnailItem, createMenuDivider(),
        downloadModelItem, downloadSimulationItem, createMenuDivider(adminRole)
    )

    val moreDropdown = Dropdown(
        icon = Icon("cog"), color = ElementColor.Primary, right = true, rounded = true
    ) { refreshMoreButtons() }

    val cloneButton = Button(
        "Clone", Icon("clone"), ElementColor.Primary,
        rounded = true, outlined = true
    ) {
        if (appContext.me != null) {
            // a user is logged, just clone the model or simulation
            when (editionTab.selectedPage) {
                modelPage -> cloneModel()
                simulationPage -> cloneSimulation()
            }
        } else {
            // no user logged, propose to log in or register in
            val modal = modalDialog(
                "Join Centyllion",
                Div(p("To clone a model or a simulation, you need to be connected.")),
                textButton("Log In", ElementColor.Primary) {
                    // forces to login
                    window.location.href = appContext.keycloak.createLoginUrl()
                },
                textButton("Register", ElementColor.Success) { appContext.openPage(signInPage) },
                textButton("No, thank you")
            )
            modal.active = true
        }
    }

    val modelPage = TabPage(TabItem("Model", "boxes"), modelController)
    val simulationPage = TabPage(TabItem("Simulation", "play"), simulationController)

    val undoControl = Control(modelUndoRedo.undoButton)
    val redoControl = Control(modelUndoRedo.redoButton)

    val tools = Field(
        undoControl, redoControl, Control(saveButton), Control(moreDropdown),
        grouped = true
    )

    val tabs = Tabs(boxed = true)

    val editionTab = TabPages(modelPage, simulationPage, tabs = tabs, initialTabIndex = 1) {
        refreshButtons()
    }

    val container: BulmaElement = Columns(
        Column(
            Level(
                left = listOf(modelNameController, userLabel),
                center = listOf(cloneButton),
                right = listOf(tools)
            ),
            size = ColumnSize.Full
        ),
        Column(modelDescriptionController, size = ColumnSize.Full),
        Column(tagsController,size = ColumnSize.FourFifths),
        Column(editionTab, size = ColumnSize.Full),
        multiline = true, centered = true
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
        tagsController.hidden = true

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
                    fetchSimulations(model.id).then { simulations ->
                        (simulations.content.firstOrNull() ?: emptySimulationDescription) to model
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
        modelUndoRedo.reset(model)
        this.model = model
        refreshButtons()
    }

    fun setSimulation(simulation: SimulationDescription) {
        val cleaned = simulation.cleaned(model)
        simulationUndoRedo.reset(cleaned)
        this.simulation = cleaned
        refreshButtons()
    }

    fun fetchSimulations(modelId: String, limit: Int = 50) =
        if (appContext.me != null) appContext.api.fetchMySimulations(modelId, limit = limit)
        else appContext.api.fetchPublicSimulations(modelId, limit = limit)

    fun saveInitThumbnail() {
        // creates a new simulator 3d view to create an init view
        val controller = Simulator3dViewController(Simulator(model.model, simulation.simulation), this)
        controller.refresh()
        controller.screenshot().then {
            controller.dispose()
            api.saveSimulationThumbnail(simulation.id, "${simulation.label}.png", it).catch { error(it) }
        }.catch { controller.dispose() }
    }

    fun saveCurrentThumbnail() {
        simulationController.simulationViewController.screenshot().then {
            api.saveSimulationThumbnail(simulation.id, "${simulation.label}.png", it).catch { error(it) }
            message("Current state saved as thumbnail")
        }
    }

    fun save(after: () -> Unit = {}) {
        val needModelSave = modelUndoRedo.changed(model) || model.id.isEmpty()
        if (needModelSave && model.id.isEmpty()) {
            // The model needs to be created first
            api.saveGrainModel(model.model)
                .then { newModel ->
                    setModel(newModel)
                    // Saves the simulation and thumbnail
                    api.saveSimulation(newModel.id, simulation.simulation)
                }.then { newSimulation ->
                    setSimulation(newSimulation)
                    saveInitThumbnail()
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
                    setModel(model)
                    message("Model ${model.model.name} saved")
                    Unit
                }.catch {
                    this.error(it)
                    Unit
                }
            }

            // Save the simulation
            if (simulationUndoRedo.changed(simulation)) {
                if (simulation.id.isEmpty()) {
                    // simulation must be created
                    api.saveSimulation(model.id, simulation.simulation).then { newSimulation ->
                        setSimulation(newSimulation)
                        saveInitThumbnail()
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
                        setSimulation(simulation)
                        saveInitThumbnail()
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
        if (readAccess && !model.info.readAccess) {
            // first ensure that the model is public
            model = model.copy(info = model.info.copy(readAccess = true))
        }
        simulation = simulation.copy(info = simulation.info.copy(readAccess = readAccess))
        moreDropdown.active = false
        message("${if (!readAccess) "Un-" else ""}Published simulation")
        save()
    }

    fun cloneModel() {
        // checks if something needs saving before creating a new simulation
        onExit().then {
            if (it) {
                // creates cloned model and cloned simulation
                val clonedModel = emptyGrainModelDescription.copy(
                    model = model.model.copy(name = model.model.name + " cloned")
                )
                val clonedSimulation = emptySimulationDescription.copy(
                    modelId = clonedModel.id,
                    simulation = simulation.simulation.copy(name = simulation.simulation.name + " cloned")
                )
                setModel(clonedModel)
                setSimulation(clonedSimulation)

                // closes the action
                moreDropdown.active = false
                editionTab.selectedPage = modelPage
                message("Model and simulation cloned")
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
                    modelId = model.id,
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
                    fetchSimulations(model.id)
                }.then {
                    setSimulation(it.content.firstOrNull() ?: emptySimulationDescription)
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
                undoControl.body = modelUndoRedo.undoButton
                redoControl.body = modelUndoRedo.redoButton
            }
            simulationPage -> {
                val readonly = isSimulationReadOnly
                tools.hidden = readonly
                undoControl.body = simulationUndoRedo.undoButton
                redoControl.body = simulationUndoRedo.redoButton
            }
        }

        modelUndoRedo.refresh()
        simulationUndoRedo.refresh()
        saveButton.disabled =
            !modelUndoRedo.changed(model) && model.id.isNotEmpty() &&
            !simulationUndoRedo.changed(simulation) && simulation.id.isNotEmpty()
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

            fetchSimulations(model.id).then {
                moreDropdown.items = moreDropdownItems + it.content.map { current ->
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
        if (modelUndoRedo.changed(model) || simulationUndoRedo.changed(simulation)) {
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
