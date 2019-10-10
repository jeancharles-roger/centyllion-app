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
import bulma.Help
import bulma.Icon
import bulma.Level
import bulma.Message
import bulma.Size
import bulma.TabItem
import bulma.TabPage
import bulma.TabPages
import bulma.Table
import bulma.TableCell
import bulma.TableHeaderCell
import bulma.TableHeaderRow
import bulma.TableRow
import bulma.Tabs
import bulma.TextColor
import bulma.iconButton
import bulma.p
import bulma.span
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
import com.centyllion.client.register
import com.centyllion.client.stringHref
import com.centyllion.client.toFixed
import com.centyllion.common.adminRole
import com.centyllion.common.apprenticeRole
import com.centyllion.common.creatorRole
import com.centyllion.model.Behaviour
import com.centyllion.model.Field
import com.centyllion.model.Grain
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ModelElement
import com.centyllion.model.Problem
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Simulator
import com.centyllion.model.behaviourIcon
import com.centyllion.model.emptyGrainModelDescription
import com.centyllion.model.emptyModel
import com.centyllion.model.emptySimulation
import com.centyllion.model.emptySimulationDescription
import com.centyllion.model.fieldIcon
import com.centyllion.model.grainIcon
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.properties.Delegates.observable
import bulma.Field as BField

/** ShowPage is use to present and edit (if not read-only) a model and a simulation. */
class ShowPage(override val appContext: AppContext) : BulmaPage {

    val api = appContext.api

    val isModelReadOnly
        get() = !appContext.hasRole(apprenticeRole) || (model.id.isNotEmpty() &&
                model.info.user?.id != appContext.me?.id)

    private var problems: List<Problem> = emptyList()

    var model: GrainModelDescription by observable(emptyGrainModelDescription) { _, old, new ->
        if (new != old) {
            modelUndoRedo.update(old, new)

            val readonly = isModelReadOnly
            modelController.readOnly = readonly
            modelController.data = new.model
            modelNameController.readOnly = readonly
            modelNameController.data = new.model.name
            val user = new.info.user?.let { if (it.id != appContext.me?.id) it.name else null }
            userLabel.text = user?.let { i18n("by %0", it) } ?: i18n("by me")

            modelDescriptionController.readOnly = readonly
            modelDescriptionController.data = new.model.description

            tagsController.readOnly = readonly
            tagsController.data = new.tags

            simulationController.context = new.model

            // handles problems display
            problems = model.model.diagnose(appContext.locale)
            problemIcon.hidden = problems.isEmpty()
            // problems box is visible is there are problems and was already open
            problemsColumn.hidden = problemsColumn.hidden || problems.isEmpty()
            problemsTable.body = problems.map { it.toBulma() }

            refreshButtons()
        }
    }

    private val modelUndoRedo = UndoRedoSupport(model) { model = it }

    val modelNameController = EditableStringController(model.model.name, i18n("Model Name")) { _, new, _ ->
        model = model.copy(model = model.model.copy(name = new))
    }

    val userLabel = Help()

    val modelDescriptionController =
        EditableMarkdownController(model.model.description, i18n("Description")) { _, new, _ ->
            model = model.copy(model = model.model.copy(description = new))
        }

    val tagsController =
        TagsController(model.tags, appContext) { old, new, _ -> if (old != new) model = model.copy(tags = new) }

    val problemIcon = iconButton(
        Icon("exclamation-triangle"), size = Size.Small, color = ElementColor.Danger, rounded = true,
        onClick = { problemsColumn.hidden = !problemsColumn.hidden }
    ).apply {
        hidden = problems.isEmpty()
    }

    val problemsTable = Table(
        head = listOf(TableHeaderRow(
            TableHeaderCell(i18n("Source")), TableHeaderCell(i18n("Message"))
        )),
        fullWidth = true, hoverable = true
    ).apply {
        root.style.backgroundColor = "transparent"
    }

    val problemsColumn = Column(
        Message(body = listOf(problemsTable), color = ElementColor.Danger),
        size = ColumnSize.Full
    ).apply {
        // Problems column is always hidden when starting
        hidden = true
    }

    private val ModelElement.icon get() = when (this) {
        is Field -> fieldIcon
        is Grain -> grainIcon
        is Behaviour -> behaviourIcon
        else -> ""
    }

    private fun Problem.toBulma() = TableRow(
        TableCell(body = *arrayOf(Icon(source.icon), span(source.name))), TableCell(message)
    ).also {
        it.root.onclick = {
            modelController.edited = this.source
            modelController.scrollToEdited()
            Unit
        }
    }

    val modelController = GrainModelEditController(model.model, this) { old, new, _ ->
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
            message("Updated speed for %0 to %1.", behaviour.name, speed.toFixed())
            val newBehaviour = behaviour.copy(probability = speed)
            model = model.copy(model = model.model.updateBehaviour(behaviour, newBehaviour))
        },
        { old, new, _ -> if (old != new) simulation = simulation.copy(simulation = new) }
    )

    val saveButton =
        Button(i18n("Save"), Icon("cloud-upload-alt"), color = ElementColor.Primary, rounded = true) { save() }

    val publishModelItem = createMenuItem(
        i18n("Publish Model"), "share-square", TextColor.Success, creatorRole
    ) { toggleModelPublication() }

    val publishSimulationItem = createMenuItem(
        i18n("Publish Simulation"), "share-square", TextColor.Success, creatorRole
    ) { toggleSimulationPublication() }

    val deleteModelItem = createMenuItem(
        i18n("Delete Model"), "trash", TextColor.Danger
    ) { deleteModel() }

    val deleteSimulationItem = createMenuItem(
        i18n("Delete Simulation"), "trash", TextColor.Danger
    ) { deleteSimulation() }

    val downloadModelItem = createMenuItem(
        i18n("Download Model"), "download", TextColor.Primary, adminRole
    ) { downloadModel() }

    val newSimulationItem = createMenuItem(
        i18n("New Simulation"), "plus", TextColor.Primary
    ) { newSimulation() }

    val saveThumbnailItem = createMenuItem(
        i18n("Save state as thumbnail"), "image", TextColor.Primary, creatorRole
    ) { saveCurrentThumbnail() }

    val downloadSimulationItem = createMenuItem(
        i18n("Download Simulation"), "download", TextColor.Primary, adminRole
    ) { downloadSimulation() }

    val loadingItem = createMenuItem(i18n("Loading simulations"), "spinner").apply { itemIcon.spin = true }

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
        i18n("Clone Simulation"), Icon("clone"), ElementColor.Primary,
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
                textButton("Register", ElementColor.Success) { appContext.openPage(register) },
                textButton("No, thank you")
            )
            modal.active = true
        }
    }

    val modelPage = TabPage(TabItem(i18n("Model"), "boxes"), modelController)
    val simulationPage = TabPage(TabItem(i18n("Simulation"), "play"), simulationController)

    val undoControl = Control(modelUndoRedo.undoButton)
    val redoControl = Control(modelUndoRedo.redoButton)

    val tools = BField(
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
                left = listOf(problemIcon, modelNameController, userLabel),
                center = listOf(cloneButton),
                right = listOf(tools)
            ),
            size = ColumnSize.Full
        ),
        Column(modelDescriptionController, size = ColumnSize.Full),
        Column(tagsController, size = ColumnSize.FourFifths),
        problemsColumn,
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
        if (appContext.me != null) appContext.api.fetchSimulations(null, modelId, limit = limit)
        else appContext.api.fetchSimulations(modelId, limit = limit)

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
            message("Current state saved as thumbnail.")
        }
    }

    val modelNeedsSaving get() = modelUndoRedo.changed(model) || model.id.isEmpty()

    val simulationNeedsSaving get() = simulationUndoRedo.changed(simulation) || simulation.id.isEmpty()

    fun save(after: () -> Unit = {}) {
        when {
            model.id.isEmpty() -> {
                // The model needs to be created first
                api.saveGrainModel(model.model)
                    .then { newModel ->
                        setModel(newModel)
                        // Saves the simulation and thumbnail
                        api.saveSimulation(newModel.id, simulation.simulation)
                    }.then { newSimulation ->
                        setSimulation(newSimulation)
                        saveInitThumbnail()
                        message("Model %0 and simulation %1 saved.", model.model.name, simulation.simulation.name)
                        after()
                        Unit
                    }.catch {
                        this.error(it)
                        Unit
                    }
            }
            else -> {

                // Save the model if needed
                if (modelUndoRedo.changed(model)) {
                    api.updateGrainModel(model).then {
                        setModel(model)
                        message("Model %0 saved.", model.model.name)
                        Unit
                    }.catch {
                        this.error(it)
                        Unit
                    }
                }

                // Save the simulation
                when {
                    simulation.id.isEmpty() -> {
                        // simulation must be created
                        api.saveSimulation(model.id, simulation.simulation).then { newSimulation ->
                            setSimulation(newSimulation)
                            saveInitThumbnail()
                            message("Simulation %0 saved.", simulation.simulation.name)
                            after()
                            Unit
                        }.catch {
                            this.error(it)
                            Unit
                        }
                    }
                    simulationUndoRedo.changed(simulation) -> {
                        // saves the simulation
                        api.updateSimulation(simulation).then {
                            setSimulation(simulation)
                            saveInitThumbnail()
                            refreshButtons()
                            message("Simulation %0 saved.", simulation.simulation.name)
                            after()
                            Unit
                        }.catch {
                            this.error(it)
                            Unit
                        }
                    }
                    else -> after()
                }
            }
        }
    }

    fun toggleModelPublication() {
        val readAccess = !model.info.readAccess
        model = model.copy(info = model.info.copy(readAccess = readAccess))
        moreDropdown.active = false
        message("${if (!readAccess) "Un-" else ""}Published model.")
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
        message("${if (!readAccess) "Un-" else ""}Published simulation.")
        save()
    }

    fun cloneModel() {
        // checks if something needs saving before creating a new simulation
        changeModelOrSimulation {
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
                message("Model and simulation cloned.")
            }
        }
    }

    fun downloadModel() {
        val href = stringHref(Json.stringify(GrainModel.serializer(), model.model))
        download("${model.name}.json", href)
        moreDropdown.active = false
    }

    fun newSimulation() = changeModelOrSimulation {
        if (it) {
            setSimulation(emptySimulationDescription)
            moreDropdown.active = false
            editionTab.selectedPage = simulationPage
            message("New simulation.")
        }
    }

    fun cloneSimulation() = changeModelOrSimulation {
        if (it) {
            val cloned = emptySimulationDescription.copy(
                modelId = model.id,
                simulation = simulation.simulation.copy(name = simulation.simulation.name + " cloned")
            )
            setSimulation(cloned)

            moreDropdown.active = false
            editionTab.selectedPage = simulationPage
            message("Simulation cloned.")
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
            i18n("Delete model. Are you sure ?"),
            Div(
                p(i18n("You're about to delete the model '%0' and its simulations.", model.label)),
                p(i18n("This action can't be undone."), "has-text-weight-bold")
            ),
            textButton(i18n("Yes"), ElementColor.Danger) {
                appContext.api.deleteGrainModel(model).then {
                    appContext.openPage(homePage)
                    message("Model %0 deleted.", model.label)
                }
            },
            textButton(i18n("No"))
        )

        modal.active = true
    }

    fun deleteSimulation() {
        moreDropdown.active = false

        val modal = modalDialog(
            i18n("Delete simulation. Are you sure ?"),
            Div(
                p(i18n("You're about to delete the simulation '%0'.", simulation.label)),
                p(i18n("This action can't be undone."), "has-text-weight-bold")
            ),
            textButton(i18n("Yes"), ElementColor.Danger) {
                appContext.api.deleteSimulation(simulation).then {
                    message("Simulation %0 deleted.", simulation.label)
                    fetchSimulations(model.id)
                }.then {
                    setSimulation(it.content.firstOrNull() ?: emptySimulationDescription)
                }
            },
            textButton(i18n("No"))
        )

        modal.active = true
    }

    fun refreshButtons() {
        when (editionTab.selectedPage) {
            modelPage -> {
                cloneButton.title = i18n("Clone Model")
                tools.hidden = isModelReadOnly
                undoControl.body = modelUndoRedo.undoButton
                redoControl.body = modelUndoRedo.redoButton
            }
            simulationPage -> {
                cloneButton.title = i18n("Clone Simulation")
                val readonly = isSimulationReadOnly
                tools.hidden = readonly
                undoControl.body = simulationUndoRedo.undoButton
                redoControl.body = simulationUndoRedo.redoButton
            }
        }

        modelUndoRedo.refresh()
        simulationUndoRedo.refresh()
        saveButton.disabled = !modelNeedsSaving && !simulationNeedsSaving
    }

    fun refreshMoreButtons() {
        publishModelItem.disabled = model.id.isEmpty()
        publishModelItem.itemIcon.flip = if (model.info.readAccess) FaFlip.Horizontal else FaFlip.None
        publishModelItem.itemText.text = i18n("${if (model.info.readAccess) "Un-" else ""}Publish Model")

        publishSimulationItem.disabled = simulation.id.isEmpty()
        publishSimulationItem.itemIcon.flip = if (simulation.info.readAccess) FaFlip.Horizontal else FaFlip.None
        publishSimulationItem.itemText.text = i18n("${if (simulation.info.readAccess) "Un-" else ""}Publish Simulation")

        deleteModelItem.disabled = model.id.isEmpty()

        deleteSimulationItem.disabled = simulation.id.isEmpty()

        if (model.id.isNotEmpty()) {
            moreDropdown.items = moreDropdownItems + loadingItem

            fetchSimulations(model.id).then {
                moreDropdown.items = moreDropdownItems + it.content.map { current ->
                    createMenuItem(current.label, current.icon, disabled = current == simulation) {
                        changeModelOrSimulation() {
                            if (it) {
                                setSimulation(current)
                                moreDropdown.active = false
                                editionTab.selectedPage = simulationPage
                            }
                        }
                    }
                }
            }.catch { error(it) }
        } else {
            moreDropdown.items = moreDropdownItems
        }
    }

    fun changeModelOrSimulation(dispose: Boolean = false, after: (Boolean) -> Unit = {}) {
        fun conclude(exit: Boolean) {
            if (exit && dispose) simulationController.dispose()
            after(exit)
        }

        if ((modelNeedsSaving && model != emptyGrainModelDescription) || (simulationNeedsSaving && simulation != emptySimulationDescription)) {
            val model = modalDialog(i18n("Modifications not saved. Do you wan't to save ?"),
                p(i18n("You're about to quit the page and some modifications haven't been saved.")),
                textButton(i18n("Save"), ElementColor.Success) { save { conclude(true) } },
                textButton(i18n("Don't save"), ElementColor.Danger) { conclude(true) },
                textButton(i18n("Stay here")) { conclude(false) }
            )
            model.active = true
        } else {
            conclude(true)
        }
    }

    override fun onExit() = Promise<Boolean> { resolve, _ ->
        changeModelOrSimulation(true) { resolve(it) }
    }
}
