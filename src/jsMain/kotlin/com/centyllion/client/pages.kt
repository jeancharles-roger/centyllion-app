package com.centyllion.client

import org.w3c.dom.HTMLElement
import kotlin.browser.document

@JsName("index")
fun index() {
    console.log("Starting function")

    val main = document.querySelector("section.cent-main") as HTMLElement

    val controller = SimulationController()
    //controller.data = dendriteSimulation(100, 100)
    controller.data = carSimulation(100, 100)
    main.appendChild(controller.container)
}
