package com.centyllion.client

import bulma.ElementColor
import bulma.Message
import bulma.NavBar
import bulma.Title
import bulma.span
import com.centyllion.client.controller.model.SimulationRunController
import com.centyllion.model.emptyGrainModelDescription
import com.centyllion.model.emptySimulationDescription
import keycloak.Keycloak
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.browser.document
import kotlin.js.Promise

@JsName("external")
fun external(baseUrl: String = "https://beta.centyllion.com") {
    console.log("Run centyllion external")

    // creates keycloak instance
    val keycloak = Keycloak()
    val api = Api(keycloak, baseUrl)
    api.addCss()


    // TODO Keycloak isn't initialized yet
    // val options = KeycloakInitOptions(promiseType = "native", onLoad = "check-sso", timeSkew = 10)

    document.querySelectorAll(".cent-simulation[data-simulation]").asList().forEach { container ->
        if (container is HTMLElement) {
            val simulationId = container.dataset["simulation"]
            console.log("Found simulation '#${container.id}' on $simulationId")

            val result = when {
                // if there is a simulation id, use it to find the model
                simulationId != null && simulationId.isNotEmpty() ->
                    api.fetchSimulation(simulationId).then { simulation ->
                        api.fetchGrainModel(simulation.modelId).then { simulation to it }
                    }.then { it }

                else -> Promise.resolve(emptySimulationDescription to emptyGrainModelDescription)
            }

            result.then {
                // creates context
                val context = BrowserContext(NavBar(), container, keycloak, null, api)
                val simulatorView = SimulationRunController(it.first.simulation, it.second.model, context, true)
                container.append(simulatorView.root)
            }.catch {
                val message = Message(
                    header = listOf(Title("Error")),
                    body = listOf(span(it.toString())),
                    color = ElementColor.Danger
                )
                container.append(message.root)
            }
        }
    }
}
