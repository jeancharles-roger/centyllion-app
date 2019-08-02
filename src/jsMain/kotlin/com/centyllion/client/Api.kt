package com.centyllion.client

import com.centyllion.model.Asset
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ResultPage
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Subscription
import com.centyllion.model.SubscriptionParameters
import com.centyllion.model.User
import com.centyllion.model.emptyFeatured
import keycloak.KeycloakInstance
import kotlinx.html.dom.create
import kotlinx.html.js.link
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import org.w3c.files.Blob
import org.w3c.files.File
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.js.Promise

const val finalState: Short = 4
const val successStatus: Short = 200

class Api(val instance: KeycloakInstance?, val baseUrl: String = "") {

    val json = Json(JsonConfiguration.Companion.Stable)

    private fun <T> executeWithRefreshedIdToken(instance: KeycloakInstance?, block: (bearer: String?) -> Promise<T>) =
        Promise<T> { resolve, reject ->
            val result = when {
                instance == null || !instance.authenticated -> Promise.resolve<String?>(null)
                else -> instance.updateToken(5).then { instance.idToken }
            }
            result.then { bearer -> block(bearer).then(resolve).catch(reject) }.catch(reject)
        }

    fun url(path: String) = "$baseUrl$path"

    fun fetch(method: String, path: String, bearer: String? = null, content: dynamic = null, contentType: String? = "application/json"): Promise<String> =
        Promise { resolve, reject ->
            val request = XMLHttpRequest()
            request.open(method, url(path), true)
            bearer?.let { request.setRequestHeader("Authorization", "Bearer $it") }
            contentType?.let { request.setRequestHeader("Content-Type", it) }
            request.onreadystatechange = {
                if (request.readyState == finalState) {
                    if (request.status == successStatus) {
                        resolve(request.responseText)
                    } else {
                        reject(Throwable("Can't $method '${url(path)}': (${request.status}) ${request.statusText}"))
                    }
                }
            }
            request.send(content)
        }

    /** Fetches css config and includes css files */
    fun addCss() = fetch("GET", "/css/centyllion/css.config.json").then {path ->
            JSON.parse<CssFile>(path).files.forEach {
                document.head?.appendChild(document.create.link(url(it), "stylesheet"))
            }
        }

    fun fetchVersion() =
        fetch("GET", "/version.json").then { json.parse(Version.serializer(), it) }

    fun fetchMe(): Promise<User?> =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/me", bearer).then { json.parse(User.serializer(), it) }.catch { null }
        }

    fun saveMe(user: User) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("PATCH", "/api/me", bearer, json.stringify(User.serializer(), user))
        }

    fun fetchSubscriptionsForMe(all: Boolean = false): Promise<List<Subscription>> =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/me/subscription?all=$all", bearer)
                .then { json.parse(Subscription.serializer().list, it) }
        }


    fun fetchAllUsers(detailed: Boolean = false, offset: Int = 0, limit: Int = 20) =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/user?detailed=$detailed&offset=$offset&limit=$limit", bearer).then {
                json.parse(ResultPage.serializer(User.serializer()), it)
            }
        }

    fun fetchUser(id: String, detailed: Boolean = false): Promise<User?> =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/user/$id?detailed=$detailed", bearer)
                .then { json.parse(User.serializer(), it) }.catch { null }
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

    fun searchModel(query: String, offset: Int = 0, limit: Int = 20) = executeWithRefreshedIdToken(instance) { bearer ->
        val q = encodeURIComponent(query)
        fetch("GET", "/api/model/search?q=$q&offset=$offset&limit=$limit", bearer).then {
            json.parse(ResultPage.serializer(GrainModelDescription.serializer()), it)
        }
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

    fun saveSimulationThumbnail(simulationId: String, name: String, blob: Blob) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val data = FormData()
            data.append("name", name)
            data.append("file", blob)
            fetch("POST", "/api/simulation/$simulationId/thumbnail", bearer, data, null)
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

    fun searchSimulation(query: String, offset: Int = 0, limit: Int = 20) = executeWithRefreshedIdToken(instance) { bearer ->
        val q = encodeURIComponent(query)
        fetch("GET", "/api/simulation/search?q=$q&offset=$offset&limit=$limit", bearer).then {
            json.parse(ResultPage.serializer(SimulationDescription.serializer()), it)
        }
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


    fun fetchSubscriptionsForUser(userId: String): Promise<List<Subscription>> =
        executeWithRefreshedIdToken(instance) { bearer ->
            fetch("GET", "/api/user/$userId/subscription", bearer)
                .then { json.parse(Subscription.serializer().list, it) }
        }

    fun createSubscriptionForUser(userId: String, parameters: SubscriptionParameters) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val content = json.stringify(SubscriptionParameters.serializer(), parameters)
            fetch("POST", "/api/user/$userId/subscription", bearer, content)
                .then { json.parse(Subscription.serializer(), it) }
        }

    fun fetchAllAssets(offset: Int = 0, limit: Int = 20, vararg extensions: String) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val options = listOf("offset=$offset", "limit=$limit") + extensions.map { "extension=$it" }
            val path = "/api/asset?${options.joinToString("&")}"
            fetch("GET", path, bearer).then {
                json.parse(ResultPage.serializer(Asset.serializer()), it)
            }
        }

    fun createAsset(name: String, file: File) =
        executeWithRefreshedIdToken(instance) { bearer ->
            val data = FormData()
            data.append("name", name)
            data.append("file", file)
            fetch("POST", "/api/asset", bearer, data, null)
        }
}
