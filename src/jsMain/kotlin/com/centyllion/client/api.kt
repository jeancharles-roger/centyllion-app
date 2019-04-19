package com.centyllion.client

import KeycloakInstance
import com.centyllion.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

const val finalState: Short = 4
const val successStatus: Short = 200

fun fetch(method: String, url: String, bearer: String? = null, content: String? = null): Promise<String> =
    Promise { resolve, reject ->
        val request = XMLHttpRequest()
        request.open(method, url, true)
        bearer?.let { request.setRequestHeader("Authorization", "Bearer $it") }
        request.setRequestHeader("Content-Type", "application/json")
        request.onreadystatechange = {
            if (request.readyState == finalState) {
                if (request.status == successStatus) {
                    resolve(request.responseText)
                } else {
                    reject(Throwable("Can't $method '$url': (${request.status}) ${request.statusText}"))
                }
            }
        }
        request.send(content)
    }

fun <T> executeWithRefreshedIdToken(instance: KeycloakInstance?, block: (bearer: String?) -> Promise<T>) =
    Promise<T> { resolve, reject ->
        val result = instance?.updateToken(5)?.then { instance.idToken } ?: Promise.resolve<String?>(null)
        result.then { bearer -> block(bearer).then(resolve).catch(reject) }
    }

fun fetchUser(instance: KeycloakInstance?): Promise<User> =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("GET", "/api/me", bearer).then { Json.parse(User.serializer(), it) }
    }

fun saveUser(user: User, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("PATCH", "/api/me", bearer, Json.stringify(User.serializer(), user))
    }

fun fetchMyGrainModels(instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("GET", "/api/me/model", bearer).then { Json.parse(GrainModelDescription.serializer().list, it) }
    }

fun fetchPublicGrainModels(instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("GET", "/api/model", bearer).then { Json.parse(GrainModelDescription.serializer().list, it) }
    }

fun fetchGrainModel(modelId: String, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("GET", "/api/model/$modelId", bearer).then { Json.parse(GrainModelDescription.serializer(), it) }
    }

fun saveGrainModel(model: GrainModel, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("POST", "/api/model", bearer, Json.stringify(GrainModel.serializer(), model))
            .then { Json.parse(GrainModelDescription.serializer(), it) }
    }

fun deleteGrainModel(model: GrainModelDescription, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("DELETE", "/api/model/${model._id}", bearer)
    }

fun updateGrainModel(model: GrainModelDescription, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch(
            "PATCH",
            "/api/model/${model._id}",
            bearer,
            Json.stringify(GrainModelDescription.serializer(), model)
        )
    }

fun fetchSimulations(modelId: String, public: Boolean, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        val params = if (public) "?public" else ""
        fetch("GET", "/api/model/$modelId/simulation$params", bearer)
            .then { Json.parse(SimulationDescription.serializer().list, it) }
    }

fun fetchSimulation(simulationId: String, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("GET", "/api/simulation/$simulationId", bearer)
            .then { Json.parse(SimulationDescription.serializer(), it) }
    }

fun saveSimulation(modelId: String, simulation: Simulation, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("POST", "/api/model/$modelId/simulation", bearer, Json.stringify(Simulation.serializer(), simulation))
            .then { Json.parse(SimulationDescription.serializer(), it) }
    }

fun deleteSimulation(simulation: SimulationDescription, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("DELETE", "/api/simulation/${simulation._id}", bearer)
    }

fun updateSimulation(simulation: SimulationDescription, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch(
            "PATCH",
            "/api/simulation/${simulation._id}",
            bearer,
            Json.stringify(SimulationDescription.serializer(), simulation)
        )
    }

fun fetchAllFeatured(instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("GET", "/api/featured", bearer).then { Json.parse(FeaturedDescription.serializer().list, it) }
    }

fun saveFeatured(modelId: String, simulationId: String, authorId: String, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        val featured = emptyFeatured(modelId = modelId, simulationId = simulationId, authorId = authorId)
        fetch("POST", "/api/featured", bearer, Json.stringify(FeaturedDescription.serializer(), featured))
            .then { Json.parse(FeaturedDescription.serializer(), it) }
    }


fun deleteFeatured(featured: FeaturedDescription, instance: KeycloakInstance?) =
    executeWithRefreshedIdToken(instance) { bearer ->
        fetch("DELETE", "/api/featured/${featured._id}", bearer)
    }


fun fetchEvents(instance: KeycloakInstance?) = executeWithRefreshedIdToken(instance) { bearer ->
    fetch("GET", "/api/event", bearer).then { Json.parse(Event.serializer().list, it) }
}
