package com.centyllion.client

import KeycloakInstance
import bulma.*
import com.centyllion.client.controller.*
import com.centyllion.common.adminRole
import com.centyllion.common.modelRole
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.emptySimulationDescription
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.window
import kotlin.js.Promise

data class Page(
    val title: String, val id: String, val needUser: Boolean,
    val role: String?, val header: Boolean, val callback: (appContext: AppContext) -> Unit
) {
    fun authorized(keycloak: KeycloakInstance): Boolean = when {
        !needUser -> true
        role == null -> keycloak.authenticated
        else -> keycloak.hasRealmRole(role)
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

fun explore(appContext: AppContext) {
    val featuredController = noContextColumnsController<FeaturedDescription, FeaturedController>(emptyList())
    { index, data, previous ->
        val controller = previous ?: FeaturedController(data)
        controller.body.root.onclick = {
            openPage(showPage, appContext, mapOf("model" to data.modelId, "simulation" to data.simulationId))
        }
        controller.body.root.style.cursor = "pointer"
        controller
    }
    val page = div(
        Title("Explore featured models"), featuredController
    )
    appContext.root.appendChild(page.root)

    appContext.api.fetchAllFeatured().then { models -> featuredController.data = models }
}

fun profile(appContext: AppContext) {
    val userController = UserController()
    val columns = Columns(Column(userController.container, size = ColumnSize.TwoThirds))
    appContext.root.appendChild(columns.root)

    // initialize controller
    appContext.api.fetchUser().then { userController.data = it }

    // sets callbacks for update
    userController.onUpdate = { _, new, _ ->
        if (new != null) appContext.api.saveUser(new) else null
    }
}

fun model(appContext: AppContext) {
    // authorization has been checked
    appContext.root.appendChild(ModelPage(appContext).root)
}

fun administration(appContext: AppContext) {
    // authorization has been checked
    appContext.root.appendChild(AdministrationPage(appContext).root)
}


fun show(appContext: AppContext) {
    val params = URLSearchParams(window.location.search)
    val simulationId = params.get("simulation")
    val modelId = params.get("model")

    // selects the pair simulation and model to run
    val result = when {
        // if there is a simulation id, use it to find the model
        simulationId != null && simulationId.isNotEmpty() ->
            appContext.api.fetchSimulation(simulationId).then { simulation ->
                appContext.api.fetchGrainModel(simulation.modelId).then { simulation to it }
            }.then { it }

        // if there is a model id, use it to list all simulation and take the first one
        modelId != null && modelId.isNotEmpty() ->
            appContext.api.fetchGrainModel(modelId).then { model ->
                appContext.api.fetchSimulations(model._id, true).then { simulations ->
                    (simulations.firstOrNull() ?: emptySimulationDescription) to model
                }
            }.then { it }

        else -> Promise.reject(Exception("No simulation found"))
    }

    result.then {
        val controller = SimulationRunController(it.first.simulation, it.second.model, true)
        appContext.root.appendChild(controller.root)
    }.catch {
        appContext.root.appendChild(
            Message(
                color = ElementColor.Danger,
                header = listOf(Title("Error: ${it::class}")),
                body = listOf(span(it.message.toString()))
            ).root
        )
    }


}
