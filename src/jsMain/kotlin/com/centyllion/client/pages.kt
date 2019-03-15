package com.centyllion.client

import KeycloakInstance
import com.centyllion.client.controller.*
import com.centyllion.model.Action
import com.centyllion.model.Simulator
import com.centyllion.model.sample.*
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
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
    Page("Model", "model", "", ::model),
    Page("Simulation", "simulation", "", ::simulation),
    Page("Profile", "profile", "", ::profile),
    Page("Administration", "administration", "", ::administration)
)

fun profile(root: HTMLElement, instance: KeycloakInstance) {

    val userController = UserController()
    val container = document.create.columns {
        column(ColumnSize(8), "cent-user")
    }
    container.querySelector(".cent-user")?.appendChild(userController.container)
    root.appendChild(container)

    // initialize controller
    fetchUser(instance).then { userController.data = it }

    // sets callbacks for update
    userController.onUpdate = { _, new, _ ->
        if (new != null) saveUser(new, instance) else null
    }

}

fun model(root: HTMLElement, instance: KeycloakInstance) {
}

fun simulation(root: HTMLElement, instance: KeycloakInstance) {
    val controller = SimulationController()
    val simulations = listOf(
        Simulator(dendriteModel(), dendriteSimulation(100, 100)),
        Simulator(dendriteModel(), dendriteSimulation(200, 200)),
        Simulator(bacteriaModel(), bacteriaSimulation(100, 100)),
        Simulator(bacteriaModel(), bacteriaSimulation(200, 200)),
        Simulator(immunityModel(), immunitySimulation(100, 100)),
        Simulator(immunityModel(), immunitySimulation(200, 200)),
        Simulator(carModel(), carSimulation(100, 100, 5)),
        Simulator(carModel(), carSimulation(200, 200, 5))
    )

    controller.data = simulations[0]

    root.appendChild(document.create.div("select") {
        select {
            simulations.forEach {
                option { +"${it.model.name} ${it.simulation.width}x${it.simulation.height}" }
            }
            onChangeFunction = {
                val target = it.target
                if (target is HTMLSelectElement) {
                    controller.data = simulations[target.selectedIndex]
                }
            }
        }
    })
    root.appendChild(controller.container)
}

fun administration(root: HTMLElement, instance: KeycloakInstance) {
    fetchEvents(instance).then { events ->
        events.forEach {
            val color = when (it.action) {
                Action.Create -> "is-success"
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

