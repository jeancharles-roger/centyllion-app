package com.centyllion.client

import com.centyllion.common.*
import org.w3c.dom.HTMLElement
import kotlin.browser.document

@JsName("index")
fun index() {
    console.log("Starting function")

    val a = Grain(0, "a", "red")
    val b = Grain(1, "b", "green")
    val c = Grain(2, "c", "yellow", movementProbability =  0.0)

    val r = Behaviour("r", "", 0.1,
        mainReaction = Reaction(a.id, c.id, false),
        reaction = listOf(Reaction(b.id))
    )

    val model = Model(
        "m1", 20, 20, 1, "test model",
        listOf(a,b, c), listOf(r)
    )

    val simulation = Simulation(model)
    for (i in 0 until 100) {
        simulation.addGrainAtIndex(i*4, a)
        simulation.addGrainAtIndex(i*4+2, b)
    }

    val simulator = Simulator(simulation)

    val main = document.querySelector("section.cent-main") as HTMLElement
    val pre = document.createElement("pre") as HTMLElement
    pre.innerText = toString(simulation)

    val stepCount = document.createElement("span") as HTMLElement
    stepCount.innerText = "${simulator.step}"

    val step = document.createElement("a") as HTMLElement
    step.classList.add("button", "is-primary")
    step.innerText = "Step"
    step.onclick = {
        (0 until 1).forEach { simulator.oneStep() }
        stepCount.innerText = "${simulator.step}"
        pre.innerText = toString(simulation)
        true
    }

    main.appendChild(step)
    main.appendChild(stepCount)
    main.appendChild(pre)

}

fun toString(simulation: Simulation): String {
    val builder = StringBuilder()
    for (i in 0 until simulation.model.width) {
        for (j in 0 until simulation.model.height) {
            val position = Position(j, i, 0)
            val index = simulation.model.toIndex(position)
            val grain = simulation.grainAtIndex(index)
            builder.append(grain?.name ?: "_")
            builder.append("\t")
        }
        builder.append("\n")
    }

    builder.append("Grains:\n")
    simulation.countGrains().forEach {
        builder.append("- ${simulation.model.indexedGrains[it.key]?.name} = ${it.value}\n")
    }
    return builder.toString()
}
