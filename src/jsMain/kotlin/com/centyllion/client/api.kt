package com.centyllion.client

import KeycloakInstance
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

const val finalState: Short = 4
const val successStatus: Short = 200

fun fetch(method: String, url: String, bearer: String? = null, content: String? = null): Promise<String> = Promise { resolve, reject ->
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

fun <T> executeWithRefreshedIdToken(instance: KeycloakInstance, block: (bearer: String) -> Promise<T>) =
    Promise<T> { resolve, reject ->
        instance.updateToken(5).success {
            instance.idToken?.let { bearer -> block(bearer).then(resolve) }
        }.error {
            reject(Exception("Keycloak token refresh failed"))
        }
    }