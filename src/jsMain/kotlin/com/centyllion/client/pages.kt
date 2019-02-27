package com.centyllion.client

import com.centyllion.common.Simulator
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
        Simulator(dendriteSimulation(100, 100)),
        Simulator(dendriteSimulation(200, 200)),
        Simulator(carSimulation(10, 10)),
        Simulator(carSimulation(100, 100, 5)),
        Simulator(carSimulation(200, 200, 5))
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
