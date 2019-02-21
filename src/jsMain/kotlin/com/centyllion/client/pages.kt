package com.centyllion.client

import com.centyllion.common.Position
import com.centyllion.common.Simulation
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

fun toString(simulation: Simulation): String {
    val builder = StringBuilder()
    builder.append("Grains:\n")
    val counts = simulation.countGrains()
    simulation.model.grains.forEach {
        builder.append("- ${it.name} = ${counts[it.id]}\n")
    }
    for (j in 0 until simulation.model.height) {
        for (i in 0 until simulation.model.width) {
            val position = Position(i, j, 0)
            val index = simulation.model.toIndex(position)
            val grain = simulation.grainAtIndex(index)
            builder.append(grain?.name ?: "_")
            builder.append(" ")
        }
        builder.append("\n")
    }


    return builder.toString()
}
