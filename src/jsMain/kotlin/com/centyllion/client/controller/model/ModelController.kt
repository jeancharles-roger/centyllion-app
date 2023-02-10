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
            }

            if (old.simulation != new.simulation) {
                simulationController.data = data.simulation
            }

            onUpdate(old, new, this@ModelController)
            refresh()
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
            val element = newSelection
            editorController = when (element) {
                is Field -> FieldEditController(element, page) { old, new, _ ->
                    data = data.updateField(old, new)
                }
                is Grain -> GrainEditController(element, data.model, page) { old, new, _ ->
                    data = data.updateGrain(old, new)
                }
                is Behaviour -> BehaviourEditController(element, data.model, page) { old, new, _ ->
                    data = data.updateBehaviour(old, new)
                }
                else -> null
            }
            editorController?.root?.classList?.add("animated", "fadeIn", "faster")
            editorController?.readOnly = this.readOnly
            editorColumn.body = listOf(editorController ?: emptyEditor)
            fieldsController.updateSelection(element)
            grainsController.updateSelection(element)
            behavioursController.updateSelection(element)
        }
    }

    fun scrollToSelected() {
        // TODO
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
    }

    val addGrainButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val grain = data.model.newGrain(page.i18n("Grain"))
        this.data = data.addGrain(grain)
        selected = grain
    }

    val addBehaviourButton = iconButton(Icon("plus"), ElementColor.Primary, true, size = Size.Small) {
        val behaviour = data.model.newBehaviour(page.i18n("Behaviour"))
        this.data = data.addBehaviour(behaviour)
        selected = behaviour
    }

    val fieldsController: MultipleController<Field, Unit, Columns, Column, Controller<Field, Unit, Column>> =
        noContextColumnsController(data.model.fields, onClick = { field, _ -> selected = field })
        { field, previous ->
            previous ?: FieldDisplayController(field).wrap { controller ->
                controller.onDelete = {data = data.dropField(controller.data) }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val grainsController: MultipleController<Grain, GrainModel, Columns, Column, Controller<Grain, GrainModel, Column>> =
        columnsController(data.model.grains, data.model, onClick = { grain, _ -> selected = grain })
        { grain, previous ->
            previous ?: GrainDisplayController(page, grain, data.model).wrap { controller ->
                controller.onDelete = { data = data.dropGrain(controller.data) }
                Column(controller, size = ColumnSize.Full)
            }
        }

    val behavioursController: MultipleController<Behaviour, GrainModel, Columns, Column, Controller<Behaviour, GrainModel, Column>> =
        columnsController(data.model.behaviours, data.model, onClick = { behaviour, _ -> selected = behaviour })
        { behaviour, previous ->
            previous ?: BehaviourDisplayController(page, behaviour, data.model).wrap { controller ->
                controller.onDelete = { data = data.dropBehaviour(controller.data) }
                Column(controller.container, size = ColumnSize.Full)
            }
        }

    val leftColumn = Column(
        searchController,
        Level(
            left = listOf(Icon(fieldIcon), Title(page.i18n("Fields"), TextSize.S4)),
            right = listOf(addFieldButton),
            mobile = true
        ),
        fieldsController,
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

    val editionItem = TabPage(TabItem(page.i18n("Model"), "boxes"), editorColumn)

    val simulationItem = TabPage(TabItem(page.i18n("Simulation"), "play"), simulationController)

    val editionTabs = Tabs(boxed = true)

    val editionTabPages = TabPages(simulationItem, editionItem, tabs = editionTabs) {
        if (it == simulationItem) simulationController.resize()
        refresh()
    }

    val centerColumn = Column(
        editionTabPages,
        size = ColumnSize.S6
    )

    var exportCsvButton = iconButton(Icon("file-csv"), ElementColor.Info, true, disabled = true) {
        val header = "step,${data.model.grains.map { it.name.ifBlank { it.id } }.joinToString(",")}"
        val content = (0 until simulationController.simulator.step).joinToString("\n") { step ->
            val counts = data.model.grains.map { simulationController.simulator.grainCountHistory[it]?.get(step) ?: 0 }.joinToString(",")
            "$step,$counts"
        }
        download("counts.csv", stringHref("$header\n$content"))
    }

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
        createGrainPlots(), size(window.innerWidth/4.0, window.innerHeight/3.0),
        roundPoints = true,
    ).apply {
        push(0, simulationController.simulator.grainsCounts().values.map { it.toDouble() })
    }

    val fieldChart: PlotterController = PlotterController(
        page.i18n("Fields"), page.i18n("Step"),
        createFieldPlots(), size(window.innerWidth/4.0, window.innerHeight/3.0)
    )

    val rightColumn = Column(
        grainChart, fieldChart,
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
            grainChart.size = size(grainParent.offsetWidth - 30.0, window.innerHeight/3.0)
        }

        val fieldParent = fieldChart.root.parentElement
        if (fieldParent is HTMLElement) {
            fieldChart.size = size(fieldParent.offsetWidth - 30.0, window.innerHeight/3.0)
        }
    }

}
