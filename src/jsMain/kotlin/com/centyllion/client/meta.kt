package com.centyllion.client

import bulma.NavBarImageItem
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.js.img
import kotlinx.serialization.Serializable
import org.w3c.dom.HTMLElement
import kotlin.js.Date

@Serializable
data class Version(
    val version: String,
    val build: String,
    val sha: String,
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

fun showInfo(context: AppContext) {
    context.api.fetchInfo().then {
        val brand = context.navBar.brand.firstOrNull()
        if (it.dry && brand is NavBarImageItem) {
            val image = document.create.img(
                src = "https://www.centyllion.com/assets/images/beta-white-2by1.png"
            )
            image.style.maxHeight = "1rem"
            image.style.marginLeft = "0.4rem"
            brand.root.appendChild(image)
        }
    }
}
