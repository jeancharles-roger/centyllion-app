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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.Promise
import kotlin.properties.Delegates.observable
import bulma.Field as BField

/** ShowPage is used to present and edit (if not read-only) a model and a simulation. */
class ShowPage(override val appContext: AppContext) : BulmaPage {

    private var tutorialLayer: TutorialLayer<ShowPage>? = null

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

    val newControl = Control(Button(
        i18n("New Model"), Icon("plus"), color = ElementColor.Primary
    ) { new() })

    val fileInput = FileInput(label = i18n("Import"), color = ElementColor.Primary, onChange = ::import)

    val importControl = Control(fileInput)

    val exportControl = Control(Button(
        i18n("Export"), Icon("cloud-upload-alt"), color = ElementColor.Link
    ) { export() })

    val tutorialControl = Control(Button(
        i18n("Tutorial"), Icon("question-circle"), color = ElementColor.Link
    ) { startTutorial() })

    val modelPage = TabPage(TabItem(i18n("Model"), "boxes"), modelController)
    val simulationPage = TabPage(TabItem(i18n("Simulation"), "play"), simulationController)

    val undoControl = Control(modelUndoRedo.undoButton)
    val redoControl = Control(modelUndoRedo.redoButton)

    val tools = BField(
        newControl, importControl, undoControl, redoControl, exportControl, tutorialControl,
        grouped = true, groupedMultiline = true
    )

    val tabs = Tabs(boxed = true)

    val editionTab = TabPages(modelPage, simulationPage, tabs = tabs, initialTabIndex = 1) {
        if (it == simulationPage) simulationController.resize()
        refreshButtons()
    }

    val container: BulmaElement = Div(
        Columns(
            Column(Level(center = listOf(tools)), size = ColumnSize.Full),
            problemsColumn,
            Column(editionTab, size = ColumnSize.Full),
            multiline = true, centered = true
        )
    )

    override val root: HTMLElement = container.root

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

    fun downloadScreenshot() {
        simulationController.simulationViewController.screenshotURL().then {
            val name = "${model.label} - ${simulation.label} - screenshot.webp"
            download(name, it)
        }
    }

    fun new() = changeModelOrSimulation {accepted ->
        if (accepted) {
            setModel(emptyGrainModelDescription)
            setSimulation(emptySimulationDescription)
            editionTab.selectedPage = simulationPage
            message("New model.")
        }
    }

    fun import(input: FileInput, files: FileList?) {
        val selectedFile = files?.get(0)
        if (selectedFile != null) {
            val reader = FileReader()
            reader.onload = {
                val text: String = reader.result as String
                val result = Json.decodeFromString<ModelAndSimulation>(text)
                setModel(GrainModelDescription("", DescriptionInfo(), "", result.model))
                setSimulation(SimulationDescription("", DescriptionInfo(), "", null, result.simulation))
            }
            reader.readAsText(selectedFile)
        }
    }

    val modelNeedsSaving get() = modelUndoRedo.changed(model) || model.id.isEmpty()

    val simulationNeedsSaving get() = simulationUndoRedo.changed(simulation) || simulation.id.isEmpty()

    fun export(after: () -> Unit = {}) {
        val exported = ModelAndSimulation(model.model, simulation.simulation)
        val href = stringHref(Json.encodeToString(ModelAndSimulation.serializer(), exported))
        download("model.centyllion", href)
        after()
    }

    fun refreshButtons() {
        when (editionTab.selectedPage) {
            modelPage -> {
                undoControl.body = modelUndoRedo.undoButton
                redoControl.body = modelUndoRedo.redoButton
            }
            simulationPage -> {
                undoControl.body = simulationUndoRedo.undoButton
                redoControl.body = simulationUndoRedo.redoButton
            }
        }

        modelUndoRedo.refresh()
        simulationUndoRedo.refresh()
    }

    fun changeModelOrSimulation(dispose: Boolean = false, after: (Boolean) -> Unit = {}) {
        fun conclude(exit: Boolean) {
            if (exit && dispose) simulationController.dispose()
            after(exit)
        }

        val modelChanged = modelNeedsSaving && model != emptyGrainModelDescription
        val simulationChanged = simulationNeedsSaving && simulation != emptySimulationDescription
        if (modelChanged || simulationChanged) {
            modalDialog(i18n("Modifications not saved. Do you want to save ?"),
                listOf(p(i18n("You're about to quit the page and some modifications haven't been saved."))),
                textButton(i18n("Save"), ElementColor.Success) { export { conclude(true) } },
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
