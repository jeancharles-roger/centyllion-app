package com.centyllion.client

import bulma.ElementColor
import bulma.Message
import bulma.NavBar
import bulma.Title
import bulma.span
import com.centyllion.client.controller.model.SimulationRunController
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.emptyGrainModelDescription
import com.centyllion.model.emptySimulationDescription
import keycloak.Keycloak
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise

@JsName("external")
fun external(baseUrl: String = "https://app.centyllion.com") {
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

            val locale = api.fetchLocales().then {
                val localeName = it.resolve(window.navigator.language)
                console.log("Loading locale $localeName")
                api.fetchLocale(localeName)
            }.then { it }

            result.then {
                locale.then {locale ->
                    // creates context
                    val page = object : BulmaPage {
                        override val appContext = BrowserContext(locale, NavBar(), keycloak, null, api)
                        override val root: HTMLElement = container
                    }
                    val simulatorView = SimulationRunController(it.first.simulation, it.second.model, page, true)
                    container.append(simulatorView.root)
                }
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
