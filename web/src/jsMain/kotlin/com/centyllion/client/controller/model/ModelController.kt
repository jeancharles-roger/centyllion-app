package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.client.controller.utils.SearchController
import com.centyllion.client.controller.utils.filtered
import com.centyllion.client.download
import com.centyllion.client.page.BulmaPage
import com.centyllion.client.plotter.Plot
import com.centyllion.client.plotter.PlotterController
import com.centyllion.client.plotter.toRGB
import com.centyllion.client.stringHref
import com.centyllion.model.*
import com.centyllion.model.Field
import io.data2viz.geom.size
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable

class ModelController(
    val page: BulmaPage, model: ModelAndSimulation,
    val onUpdate: (old: ModelAndSimulation, new: ModelAndSimulation, controller: ModelController) -> Unit = { _, _, _ -> }
) : NoContextController<ModelAndSimulation, Columns>() {

    override var data: ModelAndSimulation by observable(model) { _, old, new ->
        if (old != new) {
            if (old.model != new.model) {
                fieldsController.data = data.model.fields.filtered(searchController.data)
                grainsController.context = data.model
                grainsController.data = data.model.grains.filtered(searchController.data)
                behavioursController.context = data.model
                behavioursController.data = data.model.behaviours.filtered(searchController.data)
                simulationController.context = data.model

                grainChart.data = createGrainPlots()
                fieldChart.data = createFieldPlots()

                // handles problems display
                val problems = new.model.diagnose(page.appContext.locale)

                // problems box is visible is there are problems and was already open
                problemsMessage.hidden = problems.isEmpty()
                problemsTable.body = problems.map { it.toBulma() }

                expertMode = expertMode || new.expert
            }

            if (old.simulation != new.simulation) {
                simulationController.data = data.simulation
                settingsController.data = data.simulation.settings
            }

            onUpdate(old, new, this@ModelController)
            refresh()
        }
    }

    var expertMode: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            fieldsHeader.hidden = !new
            fieldsController.hidden = !new
            fieldsChartsHeader.hidden = !new
            exportFieldCsvButton.hidden = !new
            fieldChart.hidden = !new
            resizeCharts()
        }
    }

    override var readOnly: Boolean
        get() = false
        set(_) {}

    private fun MultipleController<*, *, *, *, *>.updateSelection(value: Any?) {
        val found =
            this.dataControllers.find { it.data === value } ?:
            this.dataControllers.find { it.data == value }

        this.dataControllers.forEach {
            it.root.classList.toggle("is-selected", found == it)
        }
    }

    var selected: ModelElement? by observable(null) { _, oldSelection, newSelection ->
        if (oldSelection != newSelection) {
            editorController = when (newSelection) {
                is Field -> FieldEditController(newSelection, data.model, page) { old, new, _ ->
                    data = data.updateField(old, new)
                }

                is Grain -> GrainEditController(newSelection, data.model, page) { old, new, _ ->
                    data = data.updateGrain(old, new)
                }

                is Behaviour -> BehaviourEditController(newSelection, data.model, page) { old, new, _ ->
                    data = data.updateBehaviour(old, new)
                }

                else -> null
            }
            editorController?.root?.classList?.add("animated", "fadeIn", "faster")
            editorController?.readOnly = this.readOnly
            editorColumn.body = listOf(editorController ?: emptyEditor)
            fieldsController.updateSelection(newSelection)
            grainsController.updateSelection(newSelection)
            behavioursController.updateSelection(newSelection)

            // update simulation selected grain
            if (newSelection is Grain) {
                simulationController.simulationViewController.selectedGrain = newSelection
            } else {
                simulationController.simulationViewController.selectedGrain = null
            }

        }
    }

    fun scrollToSelected() {
        // TODO
    }

    val problemsTable = Table(
        head = listOf(TableHeaderRow(
            TableHeaderCell(page.i18n("Source")), TableHeaderCell(page.i18n("Message"))
        )),
        fullWidth = true, hoverable = true
    ).apply {
        root.style.backgroundColor = "transparent"
    }

    val problemsMessage = Message(body = listOf(problemsTable), color = ElementColor.Danger)
        .apply {
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
            selected = this.source
            scrollToSelected()
        }
    }

    val searchController: SearchController = SearchController(page) { _, filter ->
        fieldsController.data = data.model.fields.filtered(filter)
        grainsController.data = data.model.grains.filtered(filter)
        behavioursController.data = data.model.behaviours.filtered(filter)
    }

    val addFieldButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val field = data.model.newField(page.i18n("Field"))
        this.data = data.addField(field)
        selected = field
        editionTabPages.selectedPage = editionItem
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val grain = data.model.newGrain(page.i18n("Grain"))
        this.data = data.addGrain(grain)
        selected = grain
        editionTabPages.selectedPage = editionItem
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val behaviour = data.model.newBehaviour(page.i18n("Behaviour"))
        this.data = data.addBehaviour(behaviour)
        selected = behaviour
        editionTabPages.selectedPage = editionItem
    }

    val grainsController: MultipleController<Grain, GrainModel, Columns, Column, Controller<Grain, GrainModel, Column>> =
        columnsController(
            initialList = data.model.grains,
            initialContext = data.model,
            onClick = { grain, _ -> selected = grain }
        ) { grain, previous ->
            previous ?: GrainDisplayController(page, grain, data.model).wrap { controller ->
                controller.onDelete = {
                    data = data.dropGrain(controller.data)
                    selected = null
                }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val behavioursController: MultipleController<Behaviour, GrainModel, Columns, Column, Controller<Behaviour, GrainModel, Column>> =
        columnsController(data.model.behaviours, data.model, onClick = { behaviour, _ -> selected = behaviour })
        { behaviour, previous ->
            previous ?: BehaviourDisplayController(page, behaviour, data.model).wrap { controller ->
                controller.onDelete = {
                    data = data.dropBehaviour(controller.data)
                    selected = null
                }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val fieldsHeader = Level(
        left = listOf(Icon(fieldIcon), Title(page.i18n("Fields"), TextSize.S4)),
        right = listOf(addFieldButton),
        mobile = true
    ).apply { hidden = !expertMode }

    val fieldsController: MultipleController<Field, Unit, Columns, Column, Controller<Field, Unit, Column>> =
        noContextColumnsController(
            initialList = data.model.fields,
            onClick = { field, _ -> selected = field },
            controllerBuilder = { field, previous ->
                previous ?: FieldDisplayController(field).wrap { controller ->
                    controller.onDelete = {
                        data = data.dropField(controller.data)
                        selected = null
                    }
                    Column(controller.container, size = ColumnSize.Full)
                }
            }
        ).apply { hidden = !expertMode }

    val leftColumn = Column(
        searchController,
        Level(
            left = listOf(Icon(grainIcon), Title(page.i18n("Grains"), TextSize.S4)),
            right = listOf(addGrainButton),
            mobile = true
        ),
        grainsController,
        Level(
            left = listOf(Icon(behaviourIcon), Title(page.i18n("Behaviours"), TextSize.S4)),
            right = listOf(addBehaviourButton),
            mobile = true
        ),
        behavioursController,
        fieldsHeader,
        fieldsController,
        problemsMessage,
        size = ColumnSize.S3
    ).apply {
        root.style.height = "80vh"
        root.style.overflowY = "auto"
    }

    val simulationController = SimulationRunController(
        model.simulation, model.model, page, false,
        onStep = { simulator: Simulator ->
            val grainCounts = simulator.lastGrainsCount()
            grainChart.push(
                simulator.step,
                grainCounts.values.map { it.toDouble() },
            )
            grainChart.renderRequest()

            fieldChart.push(
                simulator.step,
                simulator.lastFieldAmount().values.map { it.toDouble() },
            )
            fieldChart.renderRequest()

            refreshCounts()
        },
        onReset = {  simulator: Simulator ->
            // resets grain chart
            grainChart.reset()
            val grainCounts = simulator.lastGrainsCount()
            grainChart.push(0, grainCounts.values.map { it.toDouble() })
            grainChart.renderRequest()

            // resets field chart
            fieldChart.reset()
            fieldChart.push(0, simulator.lastFieldAmount().values.map { it.toDouble() })
            fieldChart.renderRequest()
        },
        onUpdate = { old, new, _ -> if (old != new) data = data.copy(simulation = new) }
    )


    val emptyEditor = SubTitle(page.i18n("Select a element to edit it"))
        .also { it.root.classList.add("has-text-centered") }

    val editorColumn = Column(emptyEditor, size = ColumnSize.Full)

    var editorController: Controller<*, dynamic, dynamic>? = null

    val settingsController = SimulationSettingsController(model.simulation.settings, page) { _, new, _ ->
        data = data.copy(simulation = data.simulation.updateSettings(new))
    }

    val modelSearchController: ModelSearchController = ModelSearchController(page) {
        loaded -> data = loaded
        editionTabPages.selectedPage = simulationItem
    }

    val editionItem: TabPage = TabPage(TabItem(page.i18n("Model"), "boxes"), editorColumn)
    val simulationItem: TabPage = TabPage(TabItem(page.i18n("Simulation"), "play"), simulationController)
    val environmentItem: TabPage = TabPage(TabItem(page.i18n("Environment"), "cogs"), settingsController)

    val editionTabs = Tabs(fullWidth = true, boxed = true)
    val editionTabPages = TabPages(simulationItem, editionItem, environmentItem, tabs = editionTabs) {
        if (it == simulationItem) simulationController.resize()
        refresh()
    }

    val centerColumn = Column(
        editionTabPages,
        size = ColumnSize.S6
    )

    var exportGrainCsvButton = iconButton(Icon("file-csv"), ElementColor.Info, true) {
        val header = "step,${data.model.grains.map { it.name.ifBlank { it.id } }.joinToString(",")}"
        val content = (0 until simulationController.simulator.step).joinToString("\n") { step ->
            val counts = data.model.grains.map { simulationController.simulator.grainCountHistory[it]?.get(step) ?: 0 }.joinToString(",")
            "$step,$counts"
        }
        download("agents.csv", stringHref("$header\n$content"))
    }

    val fieldsChartsHeader = Label(page.i18n("Fields")).apply {
        hidden = true
    }

    var exportFieldCsvButton = iconButton(Icon("file-csv"), ElementColor.Info, true) {
        val header = "step,${data.model.fields.map { it.name.ifBlank { it.id } }.joinToString(",")}"
        val content = (0 until simulationController.simulator.step).joinToString("\n") { step ->
            val counts = data.model.fields.map { simulationController.simulator.fieldAmountHistory[it]?.get(step) ?: 0f }.joinToString(",")
            "$step,$counts"
        }
        download("fields.csv", stringHref("$header\n$content"))
    }.apply {
        hidden = true
    }

    val chartsHeader = Level(
        center = listOf(
            Label(page.i18n("Grains")), exportGrainCsvButton,
            fieldsChartsHeader, exportFieldCsvButton
        ),
        mobile = true
    )

    private fun createGrainPlots(): List<Plot> = data.model.grains.map {
        Plot(
            label = it.label(true),
            stroke = it.color.toRGB(),
            startHidden = !data.model.doesGrainCountCanChange(it)
        )
    }

    private fun createFieldPlots(): List<Plot> = data.model.fields.map { field ->
        Plot(label = field.label(true), stroke = field.color.toRGB() )
    }

    val grainChart: PlotterController = PlotterController(
        page.i18n("Grains"), page.i18n("Step"),
        createGrainPlots(), size(window.innerWidth/4.0, window.innerHeight * if (expertMode) 0.4 else 0.8),
        roundPoints = true,
    ).apply {
        push(0, simulationController.simulator.grainsCounts().values.map { it.toDouble() })
    }

    val fieldChart: PlotterController = PlotterController(
        page.i18n("Fields"), page.i18n("Step"),
        createFieldPlots(), size(window.innerWidth/4.0, window.innerHeight/3.0)
    ).apply { hidden = !expertMode }

    val rightColumn = Column(
        chartsHeader, grainChart, fieldChart,
        size = ColumnSize.S3
    )

    override val container = Columns(
        leftColumn, centerColumn, rightColumn,
        multiline = true,
    )

    fun refreshCounts() {
        // refreshes grain counts
        /* TODO
        val counts = simulationController.simulator.lastGrainsCount().values
        grainsController.dataControllers.zip(counts) { controller, count ->
            val source = controller.source
            if (source is GrainRunController) source.count = count
        }

        // refreshes field amounts
        val amounts = simulationController.simulator.lastFieldAmount().values
        fieldsController.dataControllers.zip(amounts) { controller, amount ->
            val source = controller.source
            if (source is FieldRunController) source.amount = amount
        }
         */
    }

    override fun refresh() {
        // TODO refresh
        resizeCharts()
    }

    fun dispose() {
        // stop simulation
        simulationController.dispose()
    }

    fun resizeCharts() {
        val grainParent = grainChart.root.parentElement
        if (grainParent is HTMLElement) {
            val ratio = if (expertMode) 0.4 else 0.8
            // prevent resizing if size is < 0
            val x = grainParent.offsetWidth - 30.0
            if (x > 0) grainChart.size = size(x, window.innerHeight * ratio)
        }

        val fieldParent = fieldChart.root.parentElement
        if (fieldParent is HTMLElement) {
            val ratio = 0.4
            // prevent resizing if size is < 0
            val x = fieldParent.offsetWidth - 30.0
            if (x > 0) fieldChart.size = size(x, window.innerHeight * ratio)
        }
    }

}
