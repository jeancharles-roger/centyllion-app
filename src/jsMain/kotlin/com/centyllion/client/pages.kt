package com.centyllion.client

import com.centyllion.common.*
import org.w3c.dom.HTMLElement
import kotlin.browser.document

@JsName("index")
fun index() {
    console.log("Starting function")

    val a = Grain(0, "a", "red")
    val b = Grain(1, "b", "green")
    val c = Grain(2, "c", "yellow")
    val d = Grain(3, "d", "blue")

    val r1 = Behaviour(
        "r1", "", 0.2,
        mainReaction = Reaction(a.id, c.id), reaction = listOf(Reaction(b.id))
    )
    val r2 = Behaviour(
        "r2", "", 0.6,
        mainReaction = Reaction(b.id, d.id), reaction = listOf(Reaction(a.id))
    )

    val model = Model(
        "m1", 80, 80, 1, "test model",
        listOf(a, b, c, d), listOf(r1, r2)
    )

    val simulation = Simulation(model)
    for (i in 0 until (model.dataSize / 13) - 2) {
        simulation.addGrainAtIndex(i * 13, a)
        simulation.addGrainAtIndex(i * 13 + 2, b)
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
        repeat(500) { simulator.oneStep() }
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
