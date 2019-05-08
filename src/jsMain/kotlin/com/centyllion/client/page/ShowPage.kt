package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.EditableStringController
import com.centyllion.client.controller.GrainModelEditController
import com.centyllion.client.controller.SimulationRunController
import com.centyllion.model.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.properties.Delegates.observable

class ShowPage(val context: AppContext) : BulmaElement {

    val api = context.api

    var model: GrainModelDescription by observable(emptyGrainModelDescription) { _, old, new ->
        if (new != old) {
            val readonly = model.info.userId != context.me?._id
            modelController.readOnly = readonly
            modelController.data = new.model
            modelNameController.disabled = readonly
            modelNameController.data = new.model.name
            modelDescriptionController.disabled = readonly
            modelDescriptionController.data = new.model.description
            simulationController.context = new.model
        }
    }

    val modelNameController = EditableStringController(model.model.name, "Name") { _, new, _ ->
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

    val simulationController = SimulationRunController(emptySimulation, emptyModel)

    val modelPage = TabPage(TabItem("Model", "boxes"), modelController)
    val simulationPage = TabPage(TabItem("Simulation", "play"), simulationController)

    val editionTab = TabPages(modelPage, simulationPage, tabs = Tabs(boxed = true), initialTabIndex = 1)

    val container: BulmaElement = Columns(
        Column(modelNameController, size = ColumnSize.OneThird),
        Column(modelDescriptionController, size = ColumnSize.TwoThirds),
        Column(editionTab, size = ColumnSize.Full),
        multiline = true
    )

    override val root: HTMLElement = container.root

    init {
        val params = URLSearchParams(window.location.search)
        val simulationId = params.get("simulation")
        val modelId = params.get("model")

        // Selects model tab if there no simulation provided
        if (simulationId == null) editionTab.selectPage(modelPage)

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
                    context.api.fetchSimulations(model._id, true).then { simulations ->
                        (simulations.firstOrNull() ?: emptySimulationDescription) to model
                    }
                }.then { it }

            else -> Promise.reject(Exception("No simulation found"))
        }

        result.then {
            model = it.second
            simulationController.data = it.first.simulation
        }.catch {
            context.error(it)
        }
    }

}
