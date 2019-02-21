package com.centyllion.client

import com.centyllion.common.Behaviour
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.js.article
import kotlinx.html.p
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import kotlin.browser.document

class BehaviourController: Controller<Behaviour> {

    override var data: Behaviour = Behaviour()
        set(value) {
            if (value != field) {
                field = value
                refresh()
            }
        }

    override val container: HTMLElement = document.create.article("media") {
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

    val title = container.querySelector("p.cent-title") as HTMLParagraphElement
    val description = container.querySelector("p.cent-description") as HTMLParagraphElement

    override fun refresh() {
        title.innerText = data.name
        description.innerText = data.description
    }

}
