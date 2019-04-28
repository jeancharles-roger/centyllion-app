package com.centyllion.client

import KeycloakInstance
import com.centyllion.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

class Api(val instance: KeycloakInstance?) {

    val finalState: Short = 4
    val successStatus: Short = 200

    private fun fetch(method: String, url: String, bearer: String? = null, content: String? = null): Promise<String> =
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

    private fun <T> executeWithRefreshedIdToken(instance: KeycloakInstance?, block: (bearer: String?) -> Promise<T>) =
        Promise<T> { resolve, reject ->
            val result = when {
                instance == null || !instance.authenticated -> Promise.resolve<String?>(null)
                else -> instance.updateToken(5).then { instance.idToken }
            }
            result.then { bearer -> block(bearer).then(resolve).catch(reject) }
        }


    fun fetchVersion() =
        fetch("GET", "/version.json").then {Json.parse(Version.serializer(), it) }


    fun fetchUser(): Promise<User> =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/me", bearer).then { Json.parse(User.serializer(), it) }
        }

    fun saveUser(user: User) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("PATCH", "/api/me", bearer, Json.stringify(User.serializer(), user))
        }

    fun fetchMyGrainModels() =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/me/model", bearer).then { Json.parse(GrainModelDescription.serializer().list, it) }
        }

    fun fetchPublicGrainModels() =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/model", bearer).then { Json.parse(GrainModelDescription.serializer().list, it) }
        }

    fun fetchGrainModel(modelId: String) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/model/$modelId", bearer).then { Json.parse(GrainModelDescription.serializer(), it) }
        }

    fun saveGrainModel(model: GrainModel) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("POST", "/api/model", bearer, Json.stringify(GrainModel.serializer(), model))
                .then { Json.parse(GrainModelDescription.serializer(), it) }
        }

    fun deleteGrainModel(model: GrainModelDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("DELETE", "/api/model/${model._id}", bearer)
        }

    fun updateGrainModel(model: GrainModelDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch(
                "PATCH",
                "/api/model/${model._id}",
                bearer,
                Json.stringify(GrainModelDescription.serializer(), model)
            )
        }

    fun fetchSimulations(modelId: String, public: Boolean) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val params = if (public) "?public" else ""
            fetch("GET", "/api/model/$modelId/simulation$params", bearer)
                .then { Json.parse(SimulationDescription.serializer().list, it) }
        }

    fun fetchSimulation(simulationId: String) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/simulation/$simulationId", bearer)
                .then { Json.parse(SimulationDescription.serializer(), it) }
        }

    fun saveSimulation(modelId: String, simulation: Simulation) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("POST", "/api/model/$modelId/simulation", bearer, Json.stringify(Simulation.serializer(), simulation))
                .then { Json.parse(SimulationDescription.serializer(), it) }
        }

    fun deleteSimulation(simulation: SimulationDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("DELETE", "/api/simulation/${simulation._id}", bearer)
        }

    fun updateSimulation(simulation: SimulationDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch(
                "PATCH",
                "/api/simulation/${simulation._id}",
                bearer,
                Json.stringify(SimulationDescription.serializer(), simulation)
            )
        }

    fun fetchAllFeatured() =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/featured", bearer).then { Json.parse(FeaturedDescription.serializer().list, it) }
        }

    fun saveFeatured(modelId: String, simulationId: String, authorId: String) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val featured = emptyFeatured(modelId = modelId, simulationId = simulationId, authorId = authorId)
            fetch("POST", "/api/featured", bearer, Json.stringify(FeaturedDescription.serializer(), featured))
                .then { Json.parse(FeaturedDescription.serializer(), it) }
        }


    fun deleteFeatured(featured: FeaturedDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("DELETE", "/api/featured/${featured._id}", bearer)
        }


    fun fetchEvents() = executeWithRefreshedIdToken(instance) { bearer ->
        fetch("GET", "/api/event", bearer).then { Json.parse(Event.serializer().list, it) }
    }
}
