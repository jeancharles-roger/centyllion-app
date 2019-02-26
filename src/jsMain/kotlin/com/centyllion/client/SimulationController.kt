package com.centyllion.client

import chartjs.*
import com.centyllion.common.Simulation
import com.centyllion.common.Simulator
import kotlinx.html.a
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window

class SimulationController : Controller<Simulation> {

    override var data: Simulation = emptyModelAndSimulation()
        set(value) {
            if (value != field) {
                field = value
                grainsController.data = value.model.grains
                behaviourController.data = value.model.behaviours
                simulator = Simulator(data)
                running = false
                refresh()
            }
        }

    inline val model get() = data.model

    var simulator = Simulator(data)

    var running = false

    val grainsController = ListController(
        model.grains, "", size(12)
    ) { _, grain ->
        GrainController().apply { data = grain }
    }

    val behaviourController = ListController(
        model.behaviours, "", size(12)
    ) { _, behaviour ->
        BehaviourController().apply { data = behaviour }
    }

    override val container: HTMLElement = document.create.div {
        columns {
            column(size(3), "cent-grains")
            column(size(6, centered = true)) {
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
                        a(classes = "button is-rounded is-warning cent-reset") {
                            +"Reset"
                            onClickFunction = {
                                reset()
                            }
                        }
                    }
                    div("level-item cent-stepcount") {
                        span("label")
                    }
                }
                div("has-text-centered") {
                    canvas("cent-rendering") {
                        val canvasWidth = (window.innerWidth - 200).coerceAtMost(500)
                        width = "$canvasWidth"
                        height = "${model.height * canvasWidth / model.width}"
                    }
                }
            }
            column(size(3), "cent-relations")
        }
        columns("is-centered") {
            column(size(10)) {
                canvas("cent-graph") {}
            }
        }
    }

    val runButton = container.querySelector("a.cent-run") as HTMLAnchorElement
    val stepButton = container.querySelector("a.cent-step") as HTMLAnchorElement
    val stopButton = container.querySelector("a.cent-stop") as HTMLAnchorElement
    val resetButton = container.querySelector("a.cent-reset") as HTMLAnchorElement

    val stepCount = container.querySelector(".cent-stepcount > span") as HTMLSpanElement
    val canvas = container.querySelector(".cent-rendering") as HTMLCanvasElement

    val context = canvas.getContext("2d") as CanvasRenderingContext2D

    val graphCanvas = container.querySelector("canvas.cent-graph") as HTMLCanvasElement

    val chart = Chart(graphCanvas, LineChartConfig(
            options = LineChartOptions().apply {
                animation.duration = 0
                scales.xAxes = arrayOf(LinearAxisOptions())
                scales.yAxes = arrayOf(LinearAxisOptions())
            }
        )
    )

    init {
        container.querySelector("div.cent-grains")?.appendChild(grainsController.container)
        container.querySelector("div.cent-relations")?.appendChild(behaviourController.container)

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
            window.setTimeout(this::runningCallback, 0)
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

    fun reset() {
        if (!running) {
            simulator.reset()
            refresh()
        }
    }

    override fun refresh() {

        runButton.classList.toggle("is-loading", running)
        if (running) {
            runButton.setAttribute("disabled", "")
            stepButton.setAttribute("disabled", "")
            stopButton.removeAttribute("disabled")
            resetButton.setAttribute("disabled", "")
        } else {
            runButton.removeAttribute("disabled")
            stepButton.removeAttribute("disabled")
            stopButton.setAttribute("disabled", "")
            resetButton.removeAttribute("disabled")
        }

        stepCount.innerText = "${simulator.step}"


        // refreshes simulation view
        val canvasWidth = canvas.width.toDouble()
        val canvasHeight = canvas.height.toDouble()
        val xSize = canvasWidth / model.width
        val xMax = model.width * xSize
        val ySize = canvasHeight / model.height
        context.clearRect(0.0, 0.0, canvasWidth, canvasHeight)
        var currentX = 0.0
        var currentY = 0.0
        for (i in 0 until data.agents.size) {
            val grain = data.grainAtIndex(i)
            if (grain != null) {
                context.fillStyle = grain.color
                context.fillRect(currentX, currentY, xSize, ySize)
            }

            currentX += xSize
            if (currentX >= xMax) {
                currentX = 0.0
                currentY += ySize
            }
        }

        // refreshes charts
        chart.data.datasets = simulator.grainCountHistory.map {
            LineDataSet(
                it.key.name, it.value.mapIndexed { index, i -> LineChartPlot(index, i)}.toTypedArray(),
                borderColor = it.key.color, fill = "false"
            )
        }.toTypedArray()
        chart.update()
    }

}
