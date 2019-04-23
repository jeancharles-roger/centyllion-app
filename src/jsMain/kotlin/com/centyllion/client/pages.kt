package com.centyllion.client

import KeycloakInstance
import bulma.*
import com.centyllion.client.controller.*
import com.centyllion.common.adminRole
import com.centyllion.common.modelRole
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.emptySimulationDescription
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise

data class Page(
    val title: String,
    val id: String,
    val needUser: Boolean,
    val role: String?,
    val header: Boolean,
    val callback: (root: HTMLElement, instance: KeycloakInstance?) -> Unit
) {
    fun authorized(keycloak: KeycloakInstance?): Boolean = when {
        !needUser -> true
        role == null -> keycloak?.authenticated ?: false
        else -> keycloak?.hasRealmRole(role) ?: false

    }
}

const val contentSelector = "section.cent-main"

val pages = listOf(
    Page("Explore", "explore", false, null, true, ::explore),
    Page("Model", "model", true, modelRole, true, ::model),
    Page("Profile", "profile", true, null, true, ::profile),
    Page("Administration", "administration", true, adminRole, true, ::administration),
    Page("Show", "show", false, null, false, ::show)
)

val mainPage = pages[0]

val showPage = pages.find { it.id == "show" }!!

fun explore(root: HTMLElement, instance: KeycloakInstance?) {
    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { index, data, previous ->
        val controller = previous ?: FeaturedController(data, instance)
        controller.body.root.onclick = {
            openPage(showPage, instance, mapOf("model" to data.modelId, "simulation" to data.simulationId))
        }
        controller.body.root.style.cursor = "pointer"
        controller
    }
    val page = div(
        Title("Explore featured models"), featuredController
    )
    root.appendChild(page.root)

    fetchAllFeatured(instance).then { models -> featuredController.data = models }
}

fun profile(root: HTMLElement, instance: KeycloakInstance?) {
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

fun model(root: HTMLElement, instance: KeycloakInstance?) {
    // for model, user should be logged in
    if (instance != null) {
        root.appendChild(ModelPage(instance).root)
    }
}

fun administration(root: HTMLElement, instance: KeycloakInstance?) {
    // for admin, user must be logged in
    if (instance != null) {
        root.appendChild(AdministrationPage(instance).root)
    }
}


fun show(root: HTMLElement, instance: KeycloakInstance?) {
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
                fetchSimulations(model._id, true, instance).then { simulations ->
                    (simulations.firstOrNull() ?: emptySimulationDescription) to model
                }
            }.then { it }

        else -> Promise.reject(Exception("No simulation found"))
    }

    result.then {
        val controller = SimulationRunController(it.first.simulation, it.second.model, true)
        root.appendChild(controller.root)
    }.catch {
        root.appendChild(
            Message(
                color = ElementColor.Danger,
                header = listOf(Title("Error: ${it::class}")),
                body = listOf(span(it.message.toString()))
            ).root
        )
    }


}
