package com.centyllion.client

import org.w3c.dom.HTMLElement
import kotlin.browser.document

@JsName("index")
fun index() {
    console.log("Starting function")

    val main = document.querySelector("section.cent-main") as HTMLElement

    val controller = SimulationController()
    controller.data = dendriteSimulation(200, 200)
    main.appendChild(controller.container)
}
