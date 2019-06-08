package com.centyllion.client

import com.centyllion.model.*
import keycloak.KeycloakInstance
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
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

class Api(val instance: KeycloakInstance?) {

    val json = Json(JsonConfiguration.Companion.Stable)

    private fun <T> executeWithRefreshedIdToken(instance: KeycloakInstance?, block: (bearer: String?) -> Promise<T>) =
        Promise<T> { resolve, reject ->
            val result = when {
                instance == null || !instance.authenticated -> Promise.resolve<String?>(null)
                else -> instance.updateToken(5).then { instance.idToken }
            }
            result.then { bearer -> block(bearer).then(resolve).catch(reject) }
        }


    fun fetchVersion() =
        fetch("GET", "/version.json").then { json.parse(Version.serializer(), it) }


    fun fetchUser(): Promise<User?> =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/me", bearer).then { json.parse(User.serializer(), it) }.catch { null }
        }

    fun saveUser(user: User) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("PATCH", "/api/me", bearer, json.stringify(User.serializer(), user))
        }

    fun fetchMyGrainModels() =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/me/model", bearer)
                .then { json.parse(GrainModelDescription.serializer().list, it) }
        }

    fun fetchPublicGrainModels(offset: Int = 0, limit: Int = 20) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/model?offset=$offset&limit=$limit", bearer)
                .then { json.parse(ResultPage.serializer(GrainModelDescription.serializer()), it) }
        }

    fun fetchGrainModel(modelId: String) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/model/$modelId", bearer)
                .then { json.parse(GrainModelDescription.serializer(), it) }
        }

    fun saveGrainModel(model: GrainModel) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("POST", "/api/model", bearer, json.stringify(GrainModel.serializer(), model))
                .then { json.parse(GrainModelDescription.serializer(), it) }
        }

    fun deleteGrainModel(model: GrainModelDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("DELETE", "/api/model/${model.id}", bearer)
        }

    fun updateGrainModel(model: GrainModelDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch(
                "PATCH",
                "/api/model/${model.id}",
                bearer,
                json.stringify(GrainModelDescription.serializer(), model)
            )
        }

    fun searchModel(query: String) = executeWithRefreshedIdToken(instance) { bearer ->
        // TODO encode query
        fetch("GET", "/api/model/search?q=$query", bearer).then { json.parse(GrainModelDescription.serializer().list, it) }
    }

    fun fetchPublicSimulations(offset: Int = 0, limit: Int = 20) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/simulation?offset=$offset&limit=$limit", bearer)
                .then { json.parse(ResultPage.serializer(SimulationDescription.serializer()), it) }
        }

    fun fetchSimulations(modelId: String, public: Boolean) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val params = if (public) "?public" else ""
            fetch("GET", "/api/model/$modelId/simulation$params", bearer)
                .then { json.parse(SimulationDescription.serializer().list, it) }
        }

    fun fetchSimulation(simulationId: String) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/simulation/$simulationId", bearer)
                .then { json.parse(SimulationDescription.serializer(), it) }
        }

    fun saveSimulation(modelId: String, simulation: Simulation) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("POST", "/api/model/$modelId/simulation", bearer, json.stringify(Simulation.serializer(), simulation))
                .then { json.parse(SimulationDescription.serializer(), it) }
        }

    fun deleteSimulation(simulation: SimulationDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("DELETE", "/api/simulation/${simulation.id}", bearer)
        }

    fun updateSimulation(simulation: SimulationDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch(
                "PATCH",
                "/api/simulation/${simulation.id}",
                bearer,
                json.stringify(SimulationDescription.serializer(), simulation)
            )
        }

    fun searchSimulation(query: String) = executeWithRefreshedIdToken(instance) { bearer ->
        // TODO encode query
        fetch("GET", "/api/simulation/search?q=$query", bearer).then { json.parse(SimulationDescription.serializer().list, it) }
    }

    fun fetchAllFeatured(offset: Int = 0, limit: Int = 20) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/featured?offset=$offset&limit=$limit", bearer).then {
                json.parse(ResultPage.serializer(FeaturedDescription.serializer()), it)
            }
        }

    fun saveFeatured(modelId: String, simulationId: String, authorId: String) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val featured = emptyFeatured(modelId = modelId, simulationId = simulationId, authorId = authorId)
            fetch("POST", "/api/featured", bearer, json.stringify(FeaturedDescription.serializer(), featured))
                .then { json.parse(FeaturedDescription.serializer(), it) }
        }


    fun deleteFeatured(featured: FeaturedDescription) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("DELETE", "/api/featured/${featured.id}", bearer)
        }
}
