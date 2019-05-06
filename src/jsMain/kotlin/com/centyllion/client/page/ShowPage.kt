package com.centyllion.client.page

import bulma.*
import com.centyllion.client.AppContext
import com.centyllion.client.controller.GrainModelEditController
import com.centyllion.client.controller.SimulationRunController
import com.centyllion.model.emptyModel
import com.centyllion.model.emptySimulation
import com.centyllion.model.emptySimulationDescription
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise

class ShowPage(val context: AppContext) : BulmaElement {

    val api = context.api

    val modelController = GrainModelEditController(emptyModel)

    val simulationController = SimulationRunController(emptySimulation, emptyModel)

    val modelPage = TabPage(TabItem("Model", "boxes"), modelController)
    val simulationPage = TabPage(TabItem("Simulation", "play"), simulationController)

    val editionTab = TabPages(modelPage, simulationPage, tabs = Tabs(boxed = true), initialTabIndex = 1)

    val container: BulmaElement = editionTab

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
            modelController.data = it.second.model
            simulationController.context = it.second.model
            simulationController.data = it.first.simulation
        }.catch {
            context.error(it)
        }
    }

}
