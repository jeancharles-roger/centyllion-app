package com.centyllion.client

import com.centyllion.i18n.Locale
import com.centyllion.i18n.Locales
import kotlinx.serialization.json.Json
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

const val finalState: Short = 4
const val successStatus: Short = 200

class Api(val baseUrl: String = "") {

    private fun url(path: String) = "$baseUrl$path"

    private fun fetch(
        method: String, path: String,
        content: dynamic = null, contentType: String? = "application/json"
    ): Promise<String> = Promise { resolve, reject ->
            val request = XMLHttpRequest()
            request.open(method, url(path), true)
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

    fun fetchVersion() =
        fetch("GET", "/version.json").then { Json.decodeFromString(Version.serializer(), it) }

    fun fetchLocales() =
        fetch("GET", "/locales/locales.json").then { Json.decodeFromString(Locales.serializer(), it) }

    fun fetchLocale(locale: String) =
        fetch("GET", "/locales/$locale.json").then { Json.decodeFromString(Locale.serializer(), it) }

}
