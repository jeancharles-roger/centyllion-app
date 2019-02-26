package com.centyllion.client

import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onChangeFunction
import kotlinx.html.option
import kotlinx.html.select
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import kotlin.browser.document

@JsName("index")
fun index() {
    console.log("Starting function")

    val main = document.querySelector("section.cent-main") as HTMLElement

    val controller = SimulationController()
    val simulations = listOf(
        dendriteSimulation(100, 100),
        dendriteSimulation(200, 200),
        carSimulation(10, 10),
        carSimulation(100, 100, 5),
        carSimulation(200, 200, 5)
    )

    controller.data = simulations[0]

    main.appendChild(document.create.div("select") {
        select {
            simulations.forEach {
                option { +"${it.model.name} ${it.model.width}x${it.model.height}" }
            }
            onChangeFunction = {
                val target = it.target
                if (target is HTMLSelectElement) {
                    controller.data = simulations[target.selectedIndex]
                }
            }
        }
    })
    main.appendChild(controller.container)
}
