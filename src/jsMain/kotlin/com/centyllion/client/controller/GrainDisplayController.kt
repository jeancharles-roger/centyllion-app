package com.centyllion.client.controller

import com.centyllion.model.Grain
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.figure
import kotlinx.html.js.article
import kotlinx.html.p
import kotlinx.html.span
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document
import kotlin.properties.Delegates.observable

class GrainDisplayController: Controller<Grain> {

    override var data: Grain by observable(Grain()) { _, old, new ->
        if (old != new) refresh()
    }

    var count: Int by observable(-1) { _, _ , new ->
        countP.innerText = if (new < 0) "" else "$new"
    }

    override val container: HTMLElement = document.create.article("media") {
        figure("media-left") {
            span("dot")
        }
        div("media-content") {
            div("content") {
                p("label cent-title")
                p("cent-description")
            }
        }
        div("media-right") {
            p("label cent-count")
        }
    }

    val dot = container.querySelector(".dot") as HTMLSpanElement

    val title = container.querySelector("p.cent-title") as HTMLParagraphElement
    val description = container.querySelector("p.cent-description") as HTMLParagraphElement
    val countP = container.querySelector("p.cent-count") as HTMLParagraphElement

    override fun refresh() {
        dot.style.backgroundColor = data.color
        title.innerText = data.name
        description.innerText = data.description
    }

}
