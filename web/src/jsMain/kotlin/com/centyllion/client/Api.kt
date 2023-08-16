package com.centyllion.client

import kotlinx.serialization.json.Json
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

const val finalState: Short = 4
const val successStatus: Short = 200

class Api(val baseUrl: String = "") {

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

    private fun serverFetch(
        method: String,
        path: String,
        content: dynamic = null,
        contentType: String? = "application/json"
    ): Promise<String> = fetch(method, "$baseUrl$path", content, contentType)

    fun fetchVersion() =
        fetch("GET", "/version.json").then { Json.decodeFromString(Version.serializer(), it) }

}
