package com.centyllion.client

import bulma.ElementColor
import bulma.Message
import bulma.NavBar
import bulma.Title
import bulma.span
import com.centyllion.client.controller.model.BehaviourRunController
import com.centyllion.client.controller.model.GrainDisplayController
import com.centyllion.client.controller.model.SimulationRunController
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.emptyGrainModelDescription
import com.centyllion.model.emptySimulationDescription
import keycloak.Keycloak
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.js.Promise

fun searchAndCreateGrainController(page: BulmaPage, simulationId: String, controller: SimulationRunController) {
    document.querySelectorAll(".cent-grain[data-for='$simulationId']").asList().map { container ->
        if (container is HTMLElement) {
            val id = container.dataset["id"]
            console.log("Found grain for '#${simulationId}' on $id.")

            val grain = controller.context.grainForId(id?.toIntOrNull() ?: -1)
            if (grain != null) {
                val grainDisplay = GrainDisplayController(page, grain, controller.context)
                container.appendChild(grainDisplay.root)
            } else {
                console.error("Can't find grain $id for simulation $simulationId.")
            }
        }
    }
}

fun searchAndCreateBehaviourController(simulationId: String, controller: SimulationRunController) {
    document.querySelectorAll(".cent-behaviour[data-for='$simulationId']").asList().map { container ->
        if (container is HTMLElement) {
            val id = container.dataset["id"]
            console.log("Found behaviour for '#${simulationId}' on $id.")

            val behaviour = controller.context.behaviours.getOrNull(id?.toIntOrNull() ?: -1)
            if (behaviour != null) {
                val behaviourDisplay = BehaviourRunController(
                    behaviour, controller.simulationViewController.data, { _, _ ->},
                    { _, speed -> controller.simulationViewController.data.setSpeed(behaviour, speed) }
                )
                container.appendChild(behaviourDisplay.root)
            } else {
                console.error("Can't find behaviour $id for simulation $simulationId.")
            }
        }
    }
}

fun createSimulationRun(appContext: AppContext, container: HTMLElement) {
    val simulationId = container.dataset["simulation"]
    val complete = container.dataset["complete"]?.toBoolean() ?: false
    val readonly = container.dataset["readonly"]?.toBoolean() ?: false
    console.log("Found simulation '#${container.id}' on $simulationId (complete=$complete, readonly=$readonly).")

    val result = when {
        // if there is a simulation id, use it to find the model
        simulationId != null && simulationId.isNotEmpty() ->
            appContext.api.fetchSimulation(simulationId).then { simulation ->
                appContext.api.fetchGrainModel(simulation.modelId).then { simulation to it }
            }.then { it }

        else -> Promise.resolve(emptySimulationDescription to emptyGrainModelDescription)
    }

    result.then {
        // creates context
        val page = object : BulmaPage {
            override val appContext = appContext
            override val root: HTMLElement = container
        }
        val simulatorView = SimulationRunController(it.first.simulation, it.second.model, page, readonly)
        simulatorView.hideSides(!complete)
        container.append(simulatorView.root)

        if (container.id.isNotBlank()) {
            searchAndCreateGrainController(page, container.id, simulatorView)
            searchAndCreateBehaviourController(container.id, simulatorView)
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

@JsName("external")
fun external(baseUrl: String = "https://app.centyllion.com") {
    console.log("Run centyllion external")

    // creates keycloak instance
    val keycloak = Keycloak()
    val api = Api(keycloak, baseUrl)
    api.addCss()

    // TODO Keycloak isn't initialized yet
    // val options = KeycloakInitOptions(promiseType = "native", onLoad = "check-sso", timeSkew = 10)

    api.fetchLocales().then {
        val localeName = it.resolve(window.navigator.language)
        console.log("Loading locale $localeName")
        api.fetchLocale(localeName)
    }.then { locale ->
        document.querySelectorAll(".cent-simulation[data-simulation]").asList().map { container ->
            if (container is HTMLElement) {
                val appContext = BrowserContext(locale, NavBar(), keycloak, null, api)
                createSimulationRun(appContext, container)
            }
        }
    }
}
