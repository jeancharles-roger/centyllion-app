package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.model.GrainModelEditController
import com.centyllion.client.controller.model.SimulationRunController
import com.centyllion.client.controller.utils.UndoRedoSupport
import com.centyllion.client.download
import com.centyllion.client.stringHref
import com.centyllion.client.toFixed
import com.centyllion.client.tutorial.BacteriasTutorial
import com.centyllion.client.tutorial.TutorialLayer
import com.centyllion.model.*
import com.centyllion.model.Field
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.js.Promise
import kotlin.properties.Delegates.observable
import bulma.Field as BField

/** ShowPage is use to present and edit (if not read-only) a model and a simulation. */
class ShowPage(override val appContext: AppContext) : BulmaPage {

    private var tutorialLayer: TutorialLayer<ShowPage>? = null

    val api = appContext.api

    private var problems: List<Problem> = emptyList()

    var model: GrainModelDescription by observable(emptyGrainModelDescription) { _, old, new ->
        if (new != old) {
            modelUndoRedo.update(old, new)
            modelController.readOnly = false
            modelController.data = new.model
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

   var simulation: SimulationDescription by observable(emptySimulationDescription) { _, old, new ->
        if (new != old) {
            simulationUndoRedo.update(old, new)
            simulationController.readOnly = false
            simulationController.data = new.simulation
            refreshButtons()
        }
    }

    private val simulationUndoRedo = UndoRedoSupport(simulation) { simulation = it }

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
        TableCell(body = arrayOf(Icon(source.icon), span(source.name))), TableCell(message)
    ).also {
        it.root.onclick = {
            modelController.edit(this.source)
            modelController.scrollToEdited()
        }
    }

    val modelController = GrainModelEditController(this, model.model) { old, new, _ ->
        if (old != new) {
            model = model.copy(model = new)
        }
    }

    val simulationController = SimulationRunController(emptySimulation, emptyModel, this, false,
        { behaviour, speed, _ ->
            message("Updated speed for %0 to %1.", behaviour.name, speed.toFixed())
            val newBehaviour = behaviour.copy(probability = speed)
            model = model.copy(model = model.model.updateBehaviour(behaviour, newBehaviour))
        },
        { old, new, _ -> if (old != new) simulation = simulation.copy(simulation = new) }
    )

    val saveButton = Button(
        i18n("Save"), Icon("cloud-upload-alt"), color = ElementColor.Primary, rounded = true
    ) { save() }

    val saveControl = Control(saveButton).apply {
        // adds a message when no user is logged-in
        if (appContext.me == null) root.appendChild(Help(i18n("Log-in to save")).root)
    }

    val moreDropdown = Dropdown(
        icon = Icon("cog"), color = ElementColor.Primary, right = true, rounded = true
    ) { refreshMoreButtons() }

    val moreControl = Control(moreDropdown)

    val downloadModelItem = createMenuItem(
        moreDropdown, i18n("Download Model"), "download", TextColor.Primary
    ) { downloadModel() }

    val newSimulationItem = createMenuItem(
        moreDropdown, i18n("New Simulation"), "plus", TextColor.Primary
    ) { newSimulation() }

    val saveThumbnailItem = createMenuItem(
        moreDropdown, i18n("Save state as thumbnail"), "image", TextColor.Primary
    ) { saveCurrentThumbnail() }

    val downloadScreenshotItem = createMenuItem(
        moreDropdown, i18n("Download screenshot"), "image", TextColor.Primary
    ) { downloadScreenshot() }

    val simulationDivider = createMenuDivider()

    val downloadSimulationItem = createMenuItem(
        moreDropdown, i18n("Download Simulation"), "download", TextColor.Primary
    ) { downloadSimulation() }

    val loadingItem = createMenuItem(
        moreDropdown, i18n("Loading simulations"), "spinner"
    ).apply { icon?.spin = true }

    val moreDropdownItems = listOfNotNull(
        newSimulationItem, saveThumbnailItem, downloadScreenshotItem, simulationDivider,
        downloadModelItem, downloadSimulationItem
    )

    val modelPage = TabPage(TabItem(i18n("Model"), "boxes"), modelController)
    val simulationPage = TabPage(TabItem(i18n("Simulation"), "play"), simulationController)

    val undoControl = Control(modelUndoRedo.undoButton)
    val redoControl = Control(modelUndoRedo.redoButton)

    private val readOnlyTools = listOf(
        moreControl,
        newSimulationItem, downloadScreenshotItem, simulationDivider,
        downloadModelItem, downloadSimulationItem
    )

    val tools = BField(
        undoControl, redoControl, saveControl, moreControl,
        grouped = true, groupedMultiline = true
    )

    val tabs = Tabs(boxed = true)

    val editionTab = TabPages(modelPage, simulationPage, tabs = tabs, initialTabIndex = 1) {
        if (it == simulationPage) simulationController.resize()
        refreshButtons()
    }

    val container: BulmaElement = Columns(

        Column(Level(center = listOf(tools)), size = ColumnSize.Full),
        problemsColumn,
        Column(editionTab, size = ColumnSize.Full),
        multiline = true, centered = true
    )

    override val root: HTMLElement = container.root

    init {
        modelController.readOnly = false
        simulationController.readOnly = false

        // retrieves model and simulation to load
        val params = URLSearchParams(window.location.search)
        val simulationId = params.get("simulation")
        val modelId = params.get("model")

        // Selects model tab if there no simulation provided
        if (simulationId == null) editionTab.selectedPage = modelPage

        // selects the pair simulation and model to run
        val result = when {
            // if there is a simulation id, use it to find the model
            !simulationId.isNullOrEmpty() ->
                appContext.api.fetchSimulation(simulationId).then { simulation ->
                    appContext.api.fetchGrainModel(simulation.modelId).then { simulation to it }
                }.then { it }

            // if there is a model id, use it to list all simulation and take the first one
            !modelId.isNullOrEmpty() ->
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

    fun startTutorial() {
        if (tutorialLayer == null) {
            // Activates tutorial
            tutorialLayer = TutorialLayer(BacteriasTutorial(this)) { tutorialLayer = null }
            tutorialLayer?.start()
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

    fun saveCurrentThumbnail() {
        val thumbnail =
            simulationController.currentThumbnail?.let { Promise.resolve(it) } ?:
            simulationController.simulationViewController.thumbnail()

        thumbnail.then {
            // saves thumbnail retrieved on last stop click if exists.
            api.saveSimulationThumbnail(simulation.id, "${simulation.label}.webp", it)
                .then { message("Current state saved as thumbnail.") }
                .catch { error(it) }
        }
    }

    fun downloadScreenshot() {
        simulationController.simulationViewController.screenshotURL().then {
            val name = "${model.label} - ${simulation.label} - screenshot.webp"
            download(name, it)
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
                        saveCurrentThumbnail()
                        message("Model %0 and simulation %1 saved.", model.model.name, simulation.simulation.name)
                        after()
                    }.catch {
                        this.error(it)
                    }
            }
            else -> {

                // Save the model if needed
                if (modelUndoRedo.changed(model)) {
                    api.updateGrainModel(model).then {
                        setModel(model)
                        message("Model %0 saved.", model.model.name)
                    }.catch {
                        this.error(it)
                    }
                }

                // Save the simulation
                when {
                    simulation.id.isEmpty() -> {
                        // simulation must be created
                        api.saveSimulation(model.id, simulation.simulation).then { newSimulation ->
                            setSimulation(newSimulation)
                            saveCurrentThumbnail()
                            message("Simulation %0 saved.", simulation.simulation.name)
                            after()
                        }.catch {
                            this.error(it)
                        }
                    }
                    simulationUndoRedo.changed(simulation) -> {
                        // saves the simulation
                        api.updateSimulation(simulation).then {
                            setSimulation(simulation)
                            saveCurrentThumbnail()
                            refreshButtons()
                            message("Simulation %0 saved.", simulation.simulation.name)
                            after()
                        }.catch {
                            this.error(it)
                        }
                    }
                    else -> after()
                }
            }
        }
    }


    fun downloadModel() {
        val href = stringHref(Json.encodeToString(GrainModel.serializer(), model.model))
        download("${model.name}.json", href)
    }

    fun newSimulation() = changeModelOrSimulation {
        if (it) {
            setSimulation(emptySimulationDescription)
            editionTab.selectedPage = simulationPage
            message("New simulation.")
        }
    }

    fun downloadSimulation() {
        val href = stringHref(Json.encodeToString(Simulation.serializer(), simulation.simulation))
        download("${simulation.name}.json", href)
    }

    fun refreshButtons() {
        when (editionTab.selectedPage) {
            modelPage -> {
                setReadonlyControls(false)
                undoControl.body = modelUndoRedo.undoButton
                redoControl.body = modelUndoRedo.redoButton
            }
            simulationPage -> {
                setReadonlyControls(false)
                undoControl.body = simulationUndoRedo.undoButton
                redoControl.body = simulationUndoRedo.redoButton
            }
        }

        modelUndoRedo.refresh()
        simulationUndoRedo.refresh()
        saveButton.disabled = appContext.me == null || (!modelNeedsSaving && !simulationNeedsSaving)
    }

    private fun setReadonlyControls(readOnly: Boolean) {
        tools.body.forEach { it.hidden = if (readOnlyTools.contains(it) ) false else readOnly }
        moreDropdownItems.forEach { it.hidden = if (readOnlyTools.contains(it) ) false else readOnly }
    }

    fun refreshMoreButtons() {
        saveThumbnailItem.disabled = appContext.me == null

        if (model.id.isNotEmpty()) {
            moreDropdown.items = moreDropdownItems + loadingItem

            fetchSimulations(model.id).then {
                if (it.content.isNotEmpty()) {
                    moreDropdown.items = moreDropdownItems + createMenuDivider() + it.content.map { current ->
                        createMenuItem(moreDropdown, current.label, current.icon, disabled = current == simulation) {
                            changeModelOrSimulation {
                                if (it) {
                                    setSimulation(current)
                                    editionTab.selectedPage = simulationPage
                                }
                            }
                        }
                    }
                } else {
                    moreDropdown.items = moreDropdownItems
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

        val modelChanged = modelNeedsSaving && model != emptyGrainModelDescription
        val simulationChanged = simulationNeedsSaving && simulation != emptySimulationDescription
        if (appContext.me != null && (modelChanged || simulationChanged)) {
            modalDialog(i18n("Modifications not saved. Do you wan't to save ?"),
                listOf(p(i18n("You're about to quit the page and some modifications haven't been saved."))),
                textButton(i18n("Save"), ElementColor.Success) { save { conclude(true) } },
                textButton(i18n("Don't save"), ElementColor.Danger) { conclude(true) },
                textButton(i18n("Stay here")) { conclude(false) }
            )
        } else {
            conclude(true)
        }
    }

    override fun onExit() = Promise { resolve, _ ->
        changeModelOrSimulation(true) {
            if (it) tutorialLayer?.stop()
            resolve(it)
        }
    }

}
