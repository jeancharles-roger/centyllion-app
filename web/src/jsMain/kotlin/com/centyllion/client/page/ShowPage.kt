package com.centyllion.client.page

import bulma.*
import bulma.extension.Switch
import com.centyllion.client.*
import com.centyllion.client.controller.model.ModelController
import com.centyllion.client.controller.utils.UndoRedoSupport
import com.centyllion.client.tutorial.BacteriasTutorial
import com.centyllion.client.tutorial.TutorialLayer
import com.centyllion.model.ModelAndSimulation
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import org.w3c.files.FileList
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.Promise
import kotlin.properties.Delegates.observable
import bulma.Field as BField

/** ShowPage is used to present and edit (if not read-only) a model and a simulation. */
class ShowPage(override val appContext: AppContext) : BulmaPage {

    private var tutorialLayer: TutorialLayer<ShowPage>? = null

    var model: ModelAndSimulation by observable(ModelAndSimulation.empty) { _, old, new ->
        if (new != old) {
            undoRedo.update(old, new)
            modelController.data = new

            expertModeSwitch.disabled = new.expert

            refreshButtons()
            saveToStorage(model)
        }
    }

    var expertMode: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            expertModeSwitch.checked = new
            modelController.expertMode = new
        }
    }

    private val undoRedo = UndoRedoSupport(model) { model = it }

    val modelController = ModelController(this, model) { _, new, _ -> model = new }

    val simulationController get() = modelController.simulationController

    val newControl = Control(Button(
        i18n("New Model"), Icon("plus"), color = ElementColor.Primary
    ) { new() })

    val fileInput = FileInput(label = i18n("Import"), color = ElementColor.Primary, onChange = ::import)

    val exportControl = Control(Button(
        i18n("Export"), Icon("download"), color = ElementColor.Primary
    ) { export() })

    val tutorialControl = Control(Button(
        i18n("Tutorial"), Icon("question-circle"), color = ElementColor.Primary
    ) { startTutorial() })

    val expertModeSwitch = Switch(
        text = i18n("Expert Mode"),
        color =  ElementColor.Primary,
        rounded = true,
        checked = expertMode
    ) { _, value -> expertMode = value }

    val tools = BField(
        newControl,
        Control(fileInput),
        exportControl,
        tutorialControl,
        Control(undoRedo.undoButton), Control(undoRedo.redoButton),
        // TODO hide tutorial for now tutorialControl,
        grouped = true, groupedMultiline = true
    )


    private val currentVersion = Help("")

    val container: BulmaElement = Div(
        Columns(
            Column(
                Level(
                    left = listOf(
                        Label("NetBioDyn", size = Size.Large),
                        currentVersion
                    ),
                    center = listOf(tools),
                    right = listOf(expertModeSwitch)
                ),
                size = ColumnSize.Full
            ),
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
            setModel(ModelAndSimulation.empty)
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

    fun export(after: () -> Unit = {}) {
        val href = stringHref(Json.encodeToString(ModelAndSimulation.serializer(), model))
        download("model.netbiodyn", href)
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


    init {
        // retrieves parameters
        val params = URLSearchParams(window.location.search)
        params.get("expert")?.let { expertMode = it.toBoolean()}


        val modelUrl = params.get("model")
        if (modelUrl != null) {
            appContext.api.fetch("GET", modelUrl)
                .then(
                    onFulfilled = { setModel(Json.decodeFromString(it)) },
                    onRejected = { notification("Can't load model at $modelUrl", ElementColor.Danger) },
                )
        } else {
            val storage = loadFromStorage()
            if (storage != null) model = storage
        }

        appContext.api.fetchVersion().then {
            currentVersion.text = i18n("version of %0", it.date)
        }
    }
}
