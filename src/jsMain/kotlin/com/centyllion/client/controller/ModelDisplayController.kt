package com.centyllion.client.controller

import com.centyllion.model.GrainModelDescription
import com.centyllion.model.sample.emptyGrainModelDescription
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.figure
import kotlinx.html.js.article
import kotlinx.html.p
import kotlinx.html.span
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import kotlin.browser.document
import kotlin.properties.Delegates.observable

class ModelDisplayController: Controller<GrainModelDescription> {

    override var data: GrainModelDescription by observable(emptyGrainModelDescription) { _, old, new ->
        if (old != new) refresh()
    }

    override val container: HTMLElement = document.create.article("media") {
        figure("media-left")
        div("media-content") {
            div("content") {
                p("label cent-title")
                p("cent-description")
            }
        }
        div("media-right") {
            p("cent-dots")
            p("cent-behaviours")
        }
    }

    val title = container.querySelector("p.cent-title") as HTMLParagraphElement
    val description = container.querySelector("p.cent-description") as HTMLParagraphElement
    val dots = container.querySelector("p.cent-dots") as HTMLParagraphElement
    val behaviours = container.querySelector("p.cent-behaviours") as HTMLParagraphElement

    override fun refresh() {
        title.innerText = data.model.name
        description.innerText = data.model.description

        // adds dots
        dots.innerHTML = ""
        data.model.grains.forEach {
            val dot = document.create.span("dot")
            dot.style.backgroundColor = it.color
            dots.appendChild(dot)
        }

        // adds behaviours
        behaviours.innerText = "${data.model.behaviours.size}"
    }

}
