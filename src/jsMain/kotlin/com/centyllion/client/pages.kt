package com.centyllion.client

import com.centyllion.common.*
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.random.Random

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
    val c = Grain(2, "c", "yellow", halfLife = 50)
    val d = Grain(3, "d", "blue", halfLife = 100)

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

    val simulation: Simulation = Simulation(model).apply {
        for (i in 0 until model.dataSize) {
            val p = Random.nextDouble()
            when {
                p < 0.01 -> addGrainAtIndex(i, a)
                p < 0.02 -> addGrainAtIndex(i, b)
            }
        }
    }

    val simulator = Simulator(simulation)

    var running = false

    val container: HTMLElement = document.create.div {
        div("level") {
            div("level-left buttons") {
                a(classes = "button is-rounded is-primary cent-run") {
                    +"Run"
                    onClickFunction = {
                        run()
                    }
                }
                a(classes = "button is-rounded is-info cent-step") {
                    +"Step"
                    onClickFunction = {
                        step()
                    }
                }
                a(classes = "button is-rounded is-danger cent-stop") {
                    +"Stop"
                    onClickFunction = {
                        stop()
                    }
                }
            }
            div("level-item cent-stepcount") {
                span("label")
            }
        }
        canvas("cent-rendering") {
            val canvasWidth = (window.innerWidth - 200).coerceAtMost(500)
            width = "$canvasWidth"
            height = "${model.height * canvasWidth / model.width}"
        }
        pre("cent-info")
    }

    val runButton = container.querySelector("a.cent-run") as HTMLAnchorElement
    val stepButton = container.querySelector("a.cent-step") as HTMLAnchorElement
    val stopButton = container.querySelector("a.cent-stop") as HTMLAnchorElement

    val stepCount = container.querySelector(".cent-stepcount > span") as HTMLSpanElement
    val canvas = container.querySelector(".cent-rendering") as HTMLCanvasElement
    val info = container.querySelector(".cent-info") as HTMLElement

    val context = canvas.getContext("2d") as CanvasRenderingContext2D

    init {
        refresh()
    }

    fun run() {
        if (!running) {
            running = true
            runningCallback()
        }
    }

    fun runningCallback() {
        simulator.oneStep()
        refresh()

        if (running) {
            window.setTimeout(this::runningCallback, 1)
        }
    }

    fun step() {
        if (!running) {
            simulator.oneStep()
            refresh()
        }
    }

    fun stop() {
        if (running) {
            running = false
            refresh()
        }
    }

    fun refresh() {

        runButton.classList.toggle("is-loading", running)
        if (running) {
            runButton.setAttribute("disabled", "")
            stepButton.setAttribute("disabled", "")
            stopButton.removeAttribute("disabled")
        } else {
            runButton.removeAttribute("disabled")
            stepButton.removeAttribute("disabled")
            stopButton.setAttribute("disabled", "")

        }

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
    }

}
