package com.centyllion.client

import kotlinx.serialization.Serializable
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.js.Date

@Serializable
data class Version(
    val version: String,
    val build: String,
    val date: String
)

fun showVersion(api: Api) {
    val versionElement = document.getElementById("version")
    val buildElement = document.getElementById("build")
    val dateElement = document.getElementById("date")

    if (versionElement != null || buildElement != null || dateElement != null) {
        api.fetchVersion().then {
            if (versionElement is HTMLElement) versionElement.innerText = it.version
            if (buildElement is HTMLElement) buildElement.innerText = it.build
            if (dateElement is HTMLElement) {

                val date = Date(it.date)
                dateElement.innerText = "${date.toLocaleTimeString()}  ${date.toLocaleDateString()}"
            }
        }.catch {
            if (versionElement is HTMLElement) versionElement.innerText = it.message ?: "error"
            if (buildElement is HTMLElement) buildElement.innerText = it.message ?: "error"
            if (dateElement is HTMLElement) dateElement.innerText = it.message ?: "error"
        }
    }
}
