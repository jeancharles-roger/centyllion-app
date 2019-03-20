package com.centyllion.client.controller

import bulma.*
import bulma.Controller
import com.centyllion.model.Grain
import kotlinx.html.p
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLSpanElement
import kotlin.properties.Delegates.observable

class GrainDisplayController: Controller<Grain, Media> {

    override var data: Grain by observable(Grain()) { _, old, new ->
        if (old != new) refresh()
    }

    var count: Int by observable(-1) { _, _ , new ->
        countLabel.text = if (new < 0) "" else "$new"
    }

    val countLabel = Label()

    override val container = Media().apply {
        left = listOf(span(classes="dot"))
        center = listOf(Content {
            p("label cent-title")
            p("cent-description")
        })
        right = listOf(countLabel)
    }

    val dot = container.root.querySelector(".dot") as HTMLSpanElement

    val title = container.root.querySelector("p.cent-title") as HTMLParagraphElement
    val description = container.root.querySelector("p.cent-description") as HTMLParagraphElement

    override fun refresh() {
        dot.style.backgroundColor = data.color
        title.innerText = data.name
        description.innerText = data.description
    }

}
