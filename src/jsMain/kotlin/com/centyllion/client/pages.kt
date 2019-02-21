package com.centyllion.client

import com.centyllion.common.*
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document
import kotlin.browser.window

@JsName("index")
fun index() {
    console.log("Starting function")

    val main = document.querySelector("section.cent-main") as HTMLElement

    val controller = SimulationController()
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

class SimulationController {

    val a = Grain(0, "a", "red")
    val b = Grain(1, "b", "green")
    val c = Grain(2, "c", "yellow", halfLife = 10)
    val d = Grain(3, "d", "blue", halfLife = 20)

    val r1 = Behaviour(
        "r1", "", 0.2,
        mainReaction = Reaction(a.id, c.id), reaction = listOf(Reaction(b.id))
    )
    val r2 = Behaviour(
        "r2", "", 0.6,
        mainReaction = Reaction(b.id, d.id), reaction = listOf(Reaction(a.id))
    )

    val model = Model(
        "m1", 300, 300, 1, "test model",
        listOf(a, b, c, d), listOf(r1, r2)
    )

    val simulation = Simulation(model).apply {
        for (i in 0 until (model.dataSize / 10) - 2) {
            addGrainAtIndex(i * 10, a)
            addGrainAtIndex(i * 10 + 2, b)
        }
    }

    val simulator = Simulator(simulation)

    val container: HTMLElement = document.create.div {
        div("level") {
            div("level-item") {
                a(classes = "button is-primary") {
                    +"Step"
                    onClickFunction = {
                        step()
                    }
                }
            }
            div("level-item cent-step") {
                span("label") { +"${simulator.step}"}
            }
        }
        canvas("cent-rendering") {
            val canvasWidth = (window.innerWidth - 200).coerceAtMost(500)
            width = "$canvasWidth"
            height = "${model.height*canvasWidth/model.width}"
        }
        pre("cent-info")
    }

    val stepCount = container.querySelector(".cent-step > span") as HTMLSpanElement
    val canvas = container.querySelector(".cent-rendering") as HTMLCanvasElement
    val info = container.querySelector(".cent-info") as HTMLElement

    val context = canvas.getContext("2d") as CanvasRenderingContext2D

    fun step() {
        //if (simulator.step < 500) {
            repeat(1) { simulator.oneStep() }
            stepCount.innerText = "${simulator.step}"


            val canvasWidth = canvas.width.toDouble()
            val canvasHeight = canvas.height.toDouble()
            val xSize = canvasWidth / model.width
            val ySize = canvasHeight / model.height
            context.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
            for (i in 0 until simulation.agents.size) {
                val grain = simulation.grainAtIndex(i)
                if (grain != null) {
                    val position = model.toPosition(i)
                    context.fillStyle = grain.color
                    context.fillRect(position.x.toDouble() * xSize, position.y.toDouble() * ySize, xSize, ySize)
                }
            }


            val builder = StringBuilder()
            builder.append("Grains:\n")
            val counts = simulation.countGrains()
            simulation.model.grains.forEach {
                builder.append("- ${it.name} = ${counts[it.id]}\n")
            }
            info.innerText = builder.toString()

            //window.setTimeout(this::step, 10)
        //}
    }

}
