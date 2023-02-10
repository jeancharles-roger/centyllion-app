package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.model.ModelController
import com.centyllion.client.controller.utils.UndoRedoSupport
import com.centyllion.client.download
import com.centyllion.client.stringHref
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

    var model: ModelAndSimulation by observable(emptyModelAndSimulation) { _, old, new ->
        if (new != old) {
            undoRedo.update(old, new)
            modelController.data = new

            // handles problems display
            problems = model.model.diagnose(appContext.locale)
            problemIcon.hidden = problems.isEmpty()
            // problems box is visible is there are problems and was already open
            problemsColumn.hidden = problemsColumn.hidden || problems.isEmpty()
            problemsTable.body = problems.map { it.toBulma() }
            refreshButtons()
        }
    }

    private val undoRedo = UndoRedoSupport(model) { model = it }

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
            modelController.selected = this.source
            modelController.scrollToSelected()
        }
    }

    val modelController = ModelController(this, model) { _, new, _ -> model = new }

    val newControl = Control(Button(
        i18n("New Model"), Icon("plus"), color = ElementColor.Link
    ) { new() })

    val fileInput = FileInput(label = i18n("Import"), color = ElementColor.Link, onChange = ::import)

    val exportControl = Control(Button(
        i18n("Export"), Icon("cloud-upload-alt"), color = ElementColor.Link
    ) { export() })

    val tutorialControl = Control(Button(
        i18n("Tutorial"), Icon("question-circle"), color = ElementColor.Link
    ) { startTutorial() })

    val tools = BField(
        newControl,
        Control(fileInput),
        Control(undoRedo.undoButton), Control(undoRedo.redoButton),
        exportControl, tutorialControl,
        grouped = true, groupedMultiline = true
    )


    val container: BulmaElement = Div(
        Columns(
            Column(Level(center = listOf(tools)), size = ColumnSize.Full),
            problemsColumn,
            Column(modelController, size = ColumnSize.Full),
            multiline = true, centered = true
        )
    )

    override val root: HTMLElement = container.root

    fun startTutorial() {
        // Activates tutorial
        tutorialLayer = TutorialLayer(BacteriasTutorial(this)) { tutorialLayer = null }
        tutorialLayer?.start()
    }

    fun setModel(model: ModelAndSimulation) {
        undoRedo.reset(model)
        this.model = model
        refreshButtons()
    }

    fun new() = changeModelOrSimulation {accepted ->
        if (accepted) {
            setModel(emptyModelAndSimulation)
            message("New model.")
        }
    }

    fun import(input: FileInput, files: FileList?) {
        val selectedFile = files?.get(0)
        if (selectedFile != null) {
            val reader = FileReader()
            reader.onload = {
                val text: String = reader.result as String
                setModel(Json.decodeFromString(text))
            }
            reader.readAsText(selectedFile)
        }
    }

    val needsSaving get() = undoRedo.changed(model)

    private val jsonExporter = Json { prettyPrint = true }

    fun export(after: () -> Unit = {}) {
        val json = jsonExporter.encodeToString(ModelAndSimulation.serializer(), model)
        val href = stringHref(Json.encodeToString(ModelAndSimulation.serializer(), model))
        download("model.centyllion", href)
        after()
    }

    fun refreshButtons() {
        undoRedo.refresh()
    }

    fun changeModelOrSimulation(dispose: Boolean = false, after: (Boolean) -> Unit = {}) {
        fun conclude(exit: Boolean) {
            if (exit && dispose) modelController.dispose()
            after(exit)
        }

        if (needsSaving) {
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
