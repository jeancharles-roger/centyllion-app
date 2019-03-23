package com.centyllion.client

import KeycloakInstance
import bulma.*
import com.centyllion.client.controller.GrainModelEditController
import com.centyllion.client.controller.SimulationController
import com.centyllion.client.controller.UserController
import com.centyllion.common.adminRole
import com.centyllion.common.modelRole
import com.centyllion.common.simulationRole
import com.centyllion.model.Action
import com.centyllion.model.Simulator
import com.centyllion.model.sample.*
import kotlinx.html.article
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.p
import org.w3c.dom.HTMLElement
import kotlin.browser.document

data class Page(
    val title: String,
    val id: String,
    val role: String,
    val callback: (root: HTMLElement, instance: KeycloakInstance) -> Unit
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = if (role != "") keycloak.hasRealmRole(role) else true
}

const val contentSelector = "section.cent-main"

val pages = listOf(
    Page("Model", "model", modelRole, ::model),
    Page("Simulation", "simulation", simulationRole, ::simulation),
    Page("Profile", "profile", "", ::profile),
    Page("Administration", "administration", adminRole, ::administration)
)

fun profile(root: HTMLElement, instance: KeycloakInstance) {

    val userController = UserController()
    val columns = Columns(Column(userController.container, size = ColumnSize.TwoThirds))
    root.appendChild(columns.root)

    // initialize controller
    fetchUser(instance).then { userController.data = it }

    // sets callbacks for update
    userController.onUpdate = { _, new, _ ->
        if (new != null) saveUser(new, instance) else null
    }

}

fun model(root: HTMLElement, instance: KeycloakInstance) {
    val model = dendriteModel()
    val simulation = dendriteSimulation(100, 100)
    val simulator = Simulator(model, simulation, true)

    val simulationController = SimulationController()
    val modelController = GrainModelEditController(instance) { _, new, _ ->
        simulationController.data = Simulator(new, simulationController.data.simulation, true)
    }
    modelController.data = model
    simulationController.data = simulator

    root.appendChild(div(
        Title("Model"), modelController,
        Title("Simulation"), Box(simulationController)
    ).root)

}

fun simulation(root: HTMLElement, instance: KeycloakInstance) {
    val simulations = listOf(
        Simulator(dendriteModel(), dendriteSimulation(100, 100)),
        Simulator(dendriteModel(), dendriteSimulation(200, 200)),
        Simulator(bacteriaModel(), bacteriaSimulation(100, 100)),
        Simulator(bacteriaModel(), bacteriaSimulation(200, 200)),
        Simulator(immunityModel(), immunitySimulation(100, 100)),
        Simulator(immunityModel(), immunitySimulation(200, 200)),
        Simulator(carModel(), carSimulation(100, 100, 5)),
        Simulator(carModel(), carSimulation(200, 200, 5)),
        Simulator(fishRespirationModel(true), fishRespirationSimulation()),
        Simulator(fishRespirationModel(false), fishRespirationSimulation())
    )

    val controller = SimulationController()
    controller.data = simulations[0]

    val options = simulations.map { Option("${it.model.name} ${it.simulation.width}x${it.simulation.height}") }
    val select = Select(options, rounded = true) { _, o ->
        controller.data = simulations[o.index]
    }
    root.appendChild(div(select, controller).root)
}

fun administration(root: HTMLElement, instance: KeycloakInstance) {
    fetchEvents(instance).then { events ->
        events.forEach {
            val color = when (it.action) {
                Action.Create -> "is-primary"
                Action.Save -> "is-info"
                Action.Delete -> "is-warning"
            }
            root.appendChild(document.create.article("message $color") {
                div("message-header level") {
                    div("level-left") {
                        div("level-item") { +"${it.action} on ${it.collection}" }
                    }
                    div("level-right") {
                        div("level-item") { +it.date }
                    }
                }
                div("message-body") {
                    it.arguments.forEach {
                        p { +it }
                    }
                }
            })
        }
    }
}

