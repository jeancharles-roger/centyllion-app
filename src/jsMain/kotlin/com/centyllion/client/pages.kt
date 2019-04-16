package com.centyllion.client

import KeycloakInstance
import bulma.*
import com.centyllion.client.controller.GrainModelDisplayController
import com.centyllion.client.controller.ModelPage
import com.centyllion.client.controller.SimulationRunController
import com.centyllion.client.controller.UserController
import com.centyllion.common.adminRole
import com.centyllion.common.modelRole
import com.centyllion.model.Action
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.emptySimulationDescription
import kotlinx.html.article
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.p
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise

data class Page(
    val title: String,
    val id: String,
    val role: String,
    val header: Boolean,
    val callback: (root: HTMLElement, instance: KeycloakInstance) -> Unit
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = if (role != "") keycloak.hasRealmRole(role) else true
}

const val contentSelector = "section.cent-main"

val pages = listOf(
    Page("Explore", "explore", "", true, ::explore),
    Page("Model", "model", modelRole, true, ::model),
    Page("Profile", "profile", "", true, ::profile),
    Page("Administration", "administration", adminRole, true, ::administration),
    Page("Show", "show", "", false, ::show)
)

val mainPage = pages[0]

val showPage = pages.find { it.id == "show" }!!

fun explore(root: HTMLElement, instance: KeycloakInstance) {
    val modelsController = noContextColumnsController<GrainModelDescription, GrainModelDisplayController>(emptyList())
    { index, data, previous ->
        val controller = previous ?: GrainModelDisplayController(data)
        controller.body.root.onclick = { openPage(showPage, instance, mapOf("model" to data._id)) }
        controller
    }
    val page = div(
        Title("Explore models"), modelsController
    )
    root.appendChild(page.root)

    fetchFeaturedGrainModels(instance).then { models -> modelsController.data = models }
}

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
    root.appendChild(ModelPage(instance).root)
}

fun administration(root: HTMLElement, instance: KeycloakInstance) {
    fetchEvents(instance).then { events ->
        events.forEach {
            val color = when (it.action) {
                Action.Create -> "is-primary"
                Action.Save -> "is-info"
                Action.Delete -> "is-warning"
                Action.Error -> "is-danger"
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


fun show(root: HTMLElement, instance: KeycloakInstance) {
    val params = URLSearchParams(window.location.search)
    val simulationId = params.get("simulation")
    val modelId = params.get("model")

    // selects the pair simulation and model to run
    val result = when {
        // if there is a simulation id, use it to find the model
        simulationId != null && simulationId.isNotEmpty() ->
            fetchSimulation(simulationId, instance).then { simulation ->
                fetchGrainModel(simulation.modelId, instance).then { simulation to it }
            }.then { it }

        // if there is a model id, use it to list all simulation and take the first one
        modelId != null && modelId.isNotEmpty() ->
            fetchGrainModel(modelId, instance).then { model ->
                fetchSimulations(model._id, instance).then { simulations ->
                    (simulations.firstOrNull() ?: emptySimulationDescription) to model
                }
            }.then { it }

        else -> Promise.reject(Exception("No simulation found"))
    }

    result.then {
        val controller = SimulationRunController(it.first.simulation, it.second.model, true)
        root.appendChild(controller.root)
    }.catch {
        root.appendChild(Message(
            color = ElementColor.Danger,
            header = listOf(Title("Error: ${it::class}")),
            body = listOf(span(it.message.toString()))
        ).root)
    }


}
