package com.centyllion.client

import com.centyllion.common.*
import org.w3c.dom.HTMLElement
import kotlin.browser.document

@JsName("index")
fun index() {
    console.log("Starting function")

    val a = Grain(0, "a", "red")
    val model = Model(
        "m1", 10, 10, 1, "test model",
        listOf(a), listOf()
    )

    val simulation = Simulation(model)
    simulation.addGrainAtIndex(7, a)
    simulation.addGrainAtIndex(27, a)
    simulation.addGrainAtIndex(75, a)


    val simulator = Simulator(simulation)

    val main = document.querySelector("section.cent-main") as HTMLElement
    val pre = document.createElement("pre") as HTMLElement
    main.appendChild(pre)

    pre.innerText = toString(simulation)

    val step = document.createElement("a") as HTMLElement
    step.classList.add("button", "is-primary")
    step.innerText = "Step"
    step.onclick = {
        simulator.oneStep()
        pre.innerText = toString(simulation)
        true
    }
    main.appendChild(step)

}

fun toString(simulation: Simulation): String {
    val builder = StringBuilder()
    for (i in 0..simulation.model.width) {
        for (j in 0..simulation.model.height) {
            val grain = simulation.grainAtIndex(simulation.model.toIndex(Position(i, j, 0)))
            builder.append(grain?.name ?: "_")
            builder.append("\t")
        }
        builder.append("\n")
    }
    return builder.toString()
}
