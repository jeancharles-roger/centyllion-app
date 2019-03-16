package com.centyllion.client.controller

import com.centyllion.model.GrainModelDescription
import com.centyllion.model.sample.emptyGrainModelDescription
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.article
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import kotlin.browser.document
import kotlin.properties.Delegates.observable

class ModelDisplayController : Controller<GrainModelDescription> {

    override var data: GrainModelDescription by observable(emptyGrainModelDescription) { _, old, new ->
        if (old != new) refresh()
    }

    override val container: HTMLElement = document.create.article("media cent-model") {
        figure("media-left")
        div("media-content") {
            div("content") {
                p("label cent-title")
                div("level") {
                    div("level-left") {
                        span("level-item cent-description")
                    }
                    div("level-right") {
                        span("level-item cent-dots")
                        span("level-item cent-behaviours")
                    }
                }

            }
        }
        div("media-right") {
            button(classes = "delete")
        }
    }

    val title = container.querySelector("p.cent-title") as HTMLParagraphElement
    val description = container.querySelector(".cent-description") as HTMLElement
    val dots = container.querySelector(".cent-dots") as HTMLElement
    val behaviours = container.querySelector(".cent-behaviours") as HTMLElement

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
