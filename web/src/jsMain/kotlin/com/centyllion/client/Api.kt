package com.centyllion.client

import com.centyllion.model.ModelAndSimulation
import kotlinx.serialization.json.Json
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

const val finalState: Short = 4
const val successStatus: Short = 200

class Api(val dbUrl: String) {

    fun translateUrl(url: String) =
        "$dbUrl/${url.substringAfter("/api/")}"


    fun fetch(
        method: String, url: String,
        content: dynamic = null, contentType: String? = "application/json"
    ): Promise<String> = Promise { resolve, reject ->
        val request = XMLHttpRequest()
        request.open(method, url, true)
        contentType?.let { request.setRequestHeader("Content-Type", it) }
        request.onreadystatechange = {
            if (request.readyState == finalState) {
                if (request.status == successStatus) {
                    resolve(request.responseText)
                } else {
                    reject(Throwable("Can't $method '${url}': (${request.status}) ${request.statusText}"))
                }
            }
        }
        request.send(content)
    }

    fun searchQuery(query: String): Promise<List<String>> =
        fetch("GET", "${dbUrl}/search/$query")
            .then { json -> Json.decodeFromString<List<String>>(json) }

    fun simulationGet(id: String): Promise<ModelAndSimulation> =
        fetch("GET", "${dbUrl}/simulation/$id")
            .then { json -> Json.decodeFromString<ModelAndSimulation>(json) }

    fun fetchVersion() =
        fetch("GET", "/version.json").then { Json.decodeFromString(Version.serializer(), it) }

}
