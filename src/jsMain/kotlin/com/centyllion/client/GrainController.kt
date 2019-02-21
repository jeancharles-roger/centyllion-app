package com.centyllion.client

import com.centyllion.common.Grain
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.article
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document

class GrainController: Controller<Grain> {

    override var data: Grain = Grain()
        set(value) {
            if (value != field) {
                field = value
                refresh()
            }
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
            button(classes = "delete")
        }
    }

    val dot = container.querySelector(".dot") as HTMLSpanElement

    val title = container.querySelector("p.cent-title") as HTMLParagraphElement
    val description = container.querySelector("p.cent-description") as HTMLParagraphElement

    override fun refresh() {
        dot.style.backgroundColor = data.color
        title.innerText = data.name
        description.innerText = data.description
    }

}
